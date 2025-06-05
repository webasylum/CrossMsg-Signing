package com.tsg.crossmsg.signing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tsg.crossmsg.signing.jws.JsonJwsSignatureStrategy;
import com.tsg.crossmsg.signing.model.MessageConverter;
import com.tsg.crossmsg.signing.xmlsig.XmlSignatureStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class SignaturePersistenceIntegrationTest {
    private static final String XML_SAMPLE = "/iso/SinglePriority_Inbound_pacs.008.xml";
    private static final String JSON_SAMPLE = "/iso/SinglePriority_Inbound-pacs008.json";
    private MessageConverter converter;
    private ObjectMapper objectMapper;
    private KeyPair keyPair;
    private X509Certificate cert; // For demo, can be null or a test cert

    @BeforeEach
    void setUp() throws Exception {
        converter = new MessageConverter();
        objectMapper = new ObjectMapper();
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        cert = null; // Use null or a test cert as needed
    }

    @Test
    void testXmlSignConvertToJsonVerifyInJson() throws Exception {
        // 1. Load XML
        String xml = readResource(XML_SAMPLE);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        // 2. Sign XML
        XmlSignatureStrategy xmlStrategy = new XmlSignatureStrategy(keyPair.getPrivate(), cert);
        Document signedDoc = xmlStrategy.sign(doc);
        String signedXml = documentToString(signedDoc);

        // 3. Convert signed XML to JSON
        String json = converter.xmlToJson(signedXml);
        JsonNode jsonNode = objectMapper.readTree(json);

        // 4. Extract signature from JSON and verify
        JsonJwsSignatureStrategy jsonStrategy = new JsonJwsSignatureStrategy();
        String jws = jsonStrategy.extractSignatureFromAppHdr((ObjectNode) jsonNode);
        ObjectNode noSigJson = jsonStrategy.removeSignatureFromAppHdr((ObjectNode) jsonNode);
        boolean valid = jsonStrategy.verify(noSigJson, jws, keyPair.getPublic());
        assertTrue(valid, "Signature should be valid after XML→JSON conversion");
    }

    @Test
    void testJsonSignConvertToXmlVerifyInXml() throws Exception {
        // 1. Load JSON
        String json = readResource(JSON_SAMPLE);
        JsonNode jsonNode = objectMapper.readTree(json);

        // 2. Sign JSON
        JsonJwsSignatureStrategy jsonStrategy = new JsonJwsSignatureStrategy();
        String jws = jsonStrategy.sign(jsonNode, keyPair.getPrivate());
        ObjectNode signedJson = jsonStrategy.embedSignatureInAppHdr((ObjectNode) jsonNode.deepCopy(), jws);
        String signedJsonString = objectMapper.writeValueAsString(signedJson);

        // 3. Convert signed JSON to XML
        String xml = converter.jsonToXml(signedJsonString);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        // 4. Extract signature from XML and verify
        XmlSignatureStrategy xmlStrategy = new XmlSignatureStrategy(keyPair.getPrivate(), cert);
        boolean valid = xmlStrategy.verify(doc, keyPair.getPublic());
        assertTrue(valid, "Signature should be valid after JSON→XML conversion");
    }

    private String readResource(String resourcePath) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalArgumentException("Resource not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(writer));
        return writer.toString();
    }
} 