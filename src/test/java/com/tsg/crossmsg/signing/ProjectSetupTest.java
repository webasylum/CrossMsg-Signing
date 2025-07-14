package com.tsg.crossmsg.signing;

import com.tsg.crossmsg.signing.util.TestKeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("Project Setup Test")
class ProjectSetupTest {
    
    private KeyPair keyPair;
    
    @BeforeEach
    void setUp() throws Exception {
        // Use TestKeyManager for key generation
        keyPair = TestKeyManager.getRsaKeyPair();
    }
    
    @Test
    @DisplayName("Test basic project setup and key generation")
    void testProjectSetup() {
        assertNotNull(keyPair, "KeyPair should be generated successfully");
        assertNotNull(keyPair.getPrivate(), "Private key should not be null");
        assertNotNull(keyPair.getPublic(), "Public key should not be null");
        assertEquals("RSA", keyPair.getPrivate().getAlgorithm(), "Should be RSA algorithm");
        assertEquals("RSA", keyPair.getPublic().getAlgorithm(), "Should be RSA algorithm");
    }
} 