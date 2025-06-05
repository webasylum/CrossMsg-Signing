package com.tsg.crossmsg.signing.jws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class JsonJwsSignatureStrategyTest {
    private static final String TEST_JSON = """
    {
      "AppHdr": {
        "MsgId": "TEST123456789",
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

    private KeyPair keyPair;
    private KeyPair wrongKeyPair;
    private JsonJwsSignatureStrategy strategy;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        wrongKeyPair = keyGen.generateKeyPair();
        strategy = new JsonJwsSignatureStrategy();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Test JWS sign and verify (valid)")
    void testSignAndVerify() throws Exception {
        JsonNode json = objectMapper.readTree(TEST_JSON);
        String jws = strategy.sign(json, keyPair.getPrivate());
        assertTrue(strategy.verify(json, jws, keyPair.getPublic()));
    }

    @Test
    @DisplayName("Test JWS verify with wrong key (should fail)")
    void testVerifyWithWrongKey() throws Exception {
        JsonNode json = objectMapper.readTree(TEST_JSON);
        String jws = strategy.sign(json, keyPair.getPrivate());
        assertFalse(strategy.verify(json, jws, wrongKeyPair.getPublic()));
    }

    @Test
    @DisplayName("Test JWS verify with modified JSON (should fail)")
    void testVerifyWithModifiedJson() throws Exception {
        JsonNode json = objectMapper.readTree(TEST_JSON);
        String jws = strategy.sign(json, keyPair.getPrivate());
        // Modify JSON
        ObjectNode modified = (ObjectNode) json.deepCopy();
        ((ObjectNode) modified.get("AppHdr")).put("MsgId", "MODIFIED");
        assertFalse(strategy.verify(modified, jws, keyPair.getPublic()));
    }

    @Test
    @DisplayName("Test AppHdr signature embed/extract helpers")
    void testAppHdrSignatureHelpers() throws Exception {
        JsonNode json = objectMapper.readTree(TEST_JSON);
        String jws = strategy.sign(json, keyPair.getPrivate());
        // Embed signature
        ObjectNode withSig = strategy.embedSignatureInAppHdr((ObjectNode) json.deepCopy(), jws);
        assertEquals(jws, withSig.get("AppHdr").get("Signature").asText());
        // Extract signature
        String extracted = strategy.extractSignatureFromAppHdr(withSig);
        assertEquals(jws, extracted);
        // Remove signature for verification
        ObjectNode noSig = strategy.removeSignatureFromAppHdr(withSig);
        assertNull(noSig.get("AppHdr").get("Signature"));
    }
} 