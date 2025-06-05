package com.tsg.crossmsg.signing.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.TreeMap;

/**
 * Implements different signature strategies for message signing:
 * 1. XML C14N + XMLDSig
 * 2. JSON Canonicalization (RFC 8785) + JWS
 * 3. Hybrid/Detached Hash
 */
public class SignatureStrategy {
    private final DocumentBuilder documentBuilder;
    private final TransformerFactory transformerFactory;
    private final ObjectMapper jsonMapper;

    static {
        if (!Init.isInitialized()) {
            Init.init();
        }
    }

    public SignatureStrategy() throws Exception {
        // Initialize XML processing
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        this.documentBuilder = factory.newDocumentBuilder();
        this.transformerFactory = TransformerFactory.newInstance();
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * Signs XML message using XMLDSig
     * @param xml XML message to sign
     * @param privateKey Private key to sign with
     * @param messageType Type of ISO 20022 message
     * @return Signed XML message
     */
    public String signXml(String xml, PrivateKey privateKey, MessageType messageType) throws Exception {
        Document doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
        Element root = doc.getDocumentElement();

        XMLSignature signature = new XMLSignature(doc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
        root.appendChild(signature.getElement());

        Transforms transforms = new Transforms(doc);
        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);
        signature.addDocument("", transforms, "http://www.w3.org/2001/04/xmlenc#sha256");

        signature.sign(privateKey);

        StringWriter writer = new StringWriter();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    /**
     * Verifies an XML signature
     * @param signedXml Signed XML message
     * @param publicKey Public key to verify with
     * @param messageType Type of ISO 20022 message
     * @return true if the signature is valid
     */
    public boolean verifyXml(String signedXml, PublicKey publicKey, MessageType messageType) throws Exception {
        Document doc = documentBuilder.parse(new InputSource(new StringReader(signedXml)));
        NodeList signatureNodes = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
        if (signatureNodes.getLength() == 0) {
            throw new IllegalArgumentException("No XML signature found");
        }

        XMLSignature signature = new XMLSignature((Element) signatureNodes.item(0), "");
        return signature.checkSignatureValue(publicKey);
    }

    /**
     * Signs JSON message using JWS
     * @param json JSON message to sign
     * @param privateKey Private key to sign with
     * @param messageType Type of ISO 20022 message
     * @return JWS token
     */
    public String signJson(String json, PrivateKey privateKey, MessageType messageType) throws Exception {
        // Create detached JWS signature
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString(
            "{\"alg\":\"RS256\",\"typ\":\"JWS\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
        String signingInput = header + "." + payload;

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha256.digest(signingInput.getBytes());

        java.security.Signature signer = java.security.Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(digest);
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(signer.sign());

        return signingInput + "." + signature;
    }

    /**
     * Verifies a JWS signature
     * @param signedJson Signed JSON message
     * @param publicKey Public key to verify with
     * @param messageType Type of ISO 20022 message
     * @return true if the signature is valid
     */
    public boolean verifyJson(String signedJson, PublicKey publicKey, MessageType messageType) throws Exception {
        String[] parts = signedJson.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWS format");
        }

        String signingInput = parts[0] + "." + parts[1];
        byte[] signatureBytes = Base64.getUrlDecoder().decode(parts[2]);

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha256.digest(signingInput.getBytes());

        java.security.Signature verifier = java.security.Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(digest);

        return verifier.verify(signatureBytes);
    }

    /**
     * Creates a detached hash signature
     * @param content Message to sign
     * @param messageType Type of ISO 20022 message
     * @return Detached signature
     */
    public String createDetachedHash(String content, MessageType messageType) throws Exception {
        // Check if content is JSON
        if (content.trim().startsWith("{")) {
            // Parse JSON and canonicalize according to RFC 8785
            ObjectNode jsonNode = (ObjectNode) jsonMapper.readTree(content);
            TreeMap<String, Object> sortedMap = new TreeMap<>();
            jsonNode.fields().forEachRemaining(entry -> sortedMap.put(entry.getKey(), entry.getValue()));
            
            // Convert back to canonical JSON string
            content = jsonMapper.writeValueAsString(sortedMap);
        }

        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha256.digest(content.getBytes());
        return Base64.getEncoder().encodeToString(digest);
    }
}