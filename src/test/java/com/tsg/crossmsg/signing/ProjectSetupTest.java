package com.tsg.crossmsg.signing;

import com.tsg.crossmsg.signing.model.SignatureStrategy;
import com.tsg.crossmsg.signing.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test class to verify project setup and dependencies.
 */
@Tag("unit")
public class ProjectSetupTest {
    private KeyPair keyPair;
    private SignatureStrategy signatureStrategy;

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
    void testSignatureStrategies() throws Exception {
        String testXml = "<test>Hello World</test>";
        String testJson = "{\"message\":\"Hello World\"}";

        // Test XML signature
        String signedXml = signatureStrategy.signXml(testXml, keyPair.getPrivate(), MessageType.PACS_008);
        assertTrue(signatureStrategy.verifyXml(signedXml, keyPair.getPublic(), MessageType.PACS_008), 
            "XML signature verification failed");

        // Test JSON signature
        String signedJson = signatureStrategy.signJson(testJson, keyPair.getPrivate(), MessageType.PACS_008);
        assertTrue(signatureStrategy.verifyJson(signedJson, keyPair.getPublic(), MessageType.PACS_008), 
            "JSON signature verification failed");

        // Test detached hash
        String hash = signatureStrategy.createDetachedHash(testXml, MessageType.PACS_008);
        assertNotNull(hash, "Detached hash should not be null");
        assertTrue(hash.length() > 0, "Detached hash should not be empty");
    }

    @Test
    void testMessageFormatConversion() {
        // This test is now handled in MessageConverterTest
        assertTrue(true);
    }
} 