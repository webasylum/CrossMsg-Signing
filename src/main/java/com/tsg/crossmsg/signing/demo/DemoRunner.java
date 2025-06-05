package com.tsg.crossmsg.signing.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tsg.crossmsg.signing.hybrid.HybridDetachedHashStrategy;
import com.tsg.crossmsg.signing.jws.JsonJwsSignatureStrategy;
import com.tsg.crossmsg.signing.xmlsig.XmlSignatureException;
import com.tsg.crossmsg.signing.xmlsig.XmlSignatureStrategy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Scanner;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DemoRunner {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nSelect signature strategy:");
            System.out.println("1. XML C14N + XMLDSig");
            System.out.println("2. JSON Canonicalization + JWS");
            System.out.println("3. Hybrid/Detached Hash");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = scanner.nextLine();
            if (choice.equals("0")) break;
            switch (choice) {
                case "1" -> runXmlSignatureDemo();
                case "2" -> runJsonJwsDemo();
                case "3" -> runHybridDetachedHashDemo();
                default -> System.out.println("Invalid choice.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void runXmlSignatureDemo() throws Exception {
        System.out.println("\n--- XML C14N + XMLDSig Demo ---");
        String xml = """
            <Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09\">
              <AppHdr/>
              <FIToFICstmrCdtTrf>
                <GrpHdr>
                  <MsgId>TEST123</MsgId>
                </GrpHdr>
              </FIToFICstmrCdtTrf>
            </Document>
            """;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        X509Certificate cert = null; // For demo, pass null or use your test cert logic
        XmlSignatureStrategy strategy = new XmlSignatureStrategy(keyPair.getPrivate(), cert);
        Document signedDoc = strategy.sign(doc);
        String signatureXml = extractSignatureElement(signedDoc);
        boolean valid = strategy.verify(signedDoc, keyPair.getPublic());
        System.out.println("Signature valid? " + valid);
        System.out.println("Signature XML: " + signatureXml);
    }

    private static String extractSignatureElement(Document doc) throws Exception {
        NodeList sigNodes = doc.getElementsByTagNameNS(
            "http://www.w3.org/2000/09/xmldsig#", "Signature"
        );
        if (sigNodes.getLength() == 0) return null;
        Element sigElem = (Element) sigNodes.item(0);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(sigElem), new StreamResult(writer));
        return writer.toString();
    }

    private static void runJsonJwsDemo() throws Exception {
        System.out.println("\n--- JSON Canonicalization + JWS Demo ---");
        String json = """
        {
          "AppHdr": {
            "MsgId": "TEST123",
            "CreDtTm": "2024-03-21T10:00:00"
          },
          "Document": {
            "FIToFICstmrCdtTrf": {
              "GrpHdr": {
                "NbOfTxs": "1"
              }
            }
          }
        }
        """;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        JsonJwsSignatureStrategy strategy = new JsonJwsSignatureStrategy();
        String jws = strategy.sign(jsonNode, keyPair.getPrivate());
        ObjectNode signedJson = strategy.embedSignatureInAppHdr((ObjectNode) jsonNode.deepCopy(), jws);
        String extractedJws = strategy.extractSignatureFromAppHdr(signedJson);
        ObjectNode noSigJson = strategy.removeSignatureFromAppHdr(signedJson);
        boolean valid = strategy.verify(noSigJson, extractedJws, keyPair.getPublic());
        System.out.println("Signature valid? " + valid);
        System.out.println("JWS proof: " + extractedJws);
    }

    private static void runHybridDetachedHashDemo() throws Exception {
        System.out.println("\n--- Hybrid/Detached Hash Demo ---");
        String jsonHeader = "{\"MsgId\":\"TEST123\",\"CreDtTm\":\"2024-03-21T10:00:00\"}";
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        HybridDetachedHashStrategy strategy = new HybridDetachedHashStrategy();
        byte[] canonicalBytes = jsonHeader.getBytes();
        byte[] digest = strategy.computeDigest(canonicalBytes);
        byte[] signature = strategy.signDigest(digest, keyPair.getPrivate());
        String base64Signature = strategy.encodeBase64(signature);
        String headerWithDigest = strategy.embedDigestInHeader(jsonHeader, strategy.encodeBase64(digest));
        String extractedDigestBase64 = strategy.extractDigestFromHeader(headerWithDigest);
        byte[] extractedDigest = strategy.decodeBase64(extractedDigestBase64);
        boolean valid = strategy.verifyDigestSignature(extractedDigest, signature, keyPair.getPublic());
        System.out.println("Signature valid? " + valid);
        System.out.println("Digest (base64): " + extractedDigestBase64);
        System.out.println("Signature (base64): " + base64Signature);
    }
} 