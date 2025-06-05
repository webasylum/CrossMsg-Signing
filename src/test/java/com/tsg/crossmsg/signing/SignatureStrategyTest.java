package com.tsg.crossmsg.signing;

import com.tsg.crossmsg.signing.model.SignatureStrategy;
import com.tsg.crossmsg.signing.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
public class SignatureStrategyTest {
    private KeyPair keyPair;
    private SignatureStrategy signatureStrategy;
    private static final String TEST_XML = "<test><message>Hello World</message></test>";
    private static final String TEST_JSON = "{\"message\":\"Hello World\"}";

    @BeforeEach
    void setUp() throws Exception {
        // Generate a test key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        
        // Initialize signature strategy
        signatureStrategy = new SignatureStrategy();
    }

    @Test
    void testXmlSignature() throws Exception {
        String signedXml = signatureStrategy.signXml(TEST_XML, keyPair.getPrivate(), MessageType.PACS_008);
        assertTrue(signatureStrategy.verifyXml(signedXml, keyPair.getPublic(), MessageType.PACS_008), 
            "XML signature verification should succeed");
    }

    @Test
    void testJsonSignature() throws Exception {
        String signedJson = signatureStrategy.signJson(TEST_JSON, keyPair.getPrivate(), MessageType.PACS_008);
        assertTrue(signatureStrategy.verifyJson(signedJson, keyPair.getPublic(), MessageType.PACS_008),
            "JSON signature verification should succeed");
    }

    @Test
    void testDetachedHash() throws Exception {
        String hash1 = signatureStrategy.createDetachedHash(TEST_XML, MessageType.PACS_008);
        String hash2 = signatureStrategy.createDetachedHash(TEST_XML, MessageType.PACS_008);
        assertEquals(hash1, hash2, "Detached hashes should be identical for the same content");
    }

    @Test
    void testJsonCanonicalization() throws Exception {
        String unorderedJson = "{\"c\":3,\"b\":2,\"a\":1}";
        String orderedJson = "{\"a\":1,\"b\":2,\"c\":3}";
        
        String hash1 = signatureStrategy.createDetachedHash(unorderedJson, MessageType.PACS_008);
        String hash2 = signatureStrategy.createDetachedHash(orderedJson, MessageType.PACS_008);
        
        assertEquals(hash1, hash2, "Hashes should be identical regardless of JSON property order");
    }

    @Test
    void testFormatConversion() throws Exception {
        // Sign XML
        String signedXml = signatureStrategy.signXml(TEST_XML, keyPair.getPrivate(), MessageType.PACS_008);
        assertTrue(signatureStrategy.verifyXml(signedXml, keyPair.getPublic(), MessageType.PACS_008),
            "Original XML signature should be valid");
        
        // Sign JSON
        String signedJson = signatureStrategy.signJson(TEST_JSON, keyPair.getPrivate(), MessageType.PACS_008);
        assertTrue(signatureStrategy.verifyJson(signedJson, keyPair.getPublic(), MessageType.PACS_008),
            "Original JSON signature should be valid");
    }
} 