package com.tsg.crossmsg.signing.hybrid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HybridDetachedHashStrategyTest {
    private static final String TEST_JSON_HEADER = "{\"MsgId\":\"TEST123\",\"CreDtTm\":\"2024-03-21T10:00:00\"}";
    private HybridDetachedHashStrategy strategy;
    private KeyPair keyPair;
    private KeyPair wrongKeyPair;

    @BeforeEach
    void setUp() throws Exception {
        strategy = new HybridDetachedHashStrategy();
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
        wrongKeyPair = keyGen.generateKeyPair();
    }

    @Test
    @DisplayName("Test digest computation, sign, and verify (valid)")
    void testDigestSignVerify() throws Exception {
        byte[] canonicalBytes = TEST_JSON_HEADER.getBytes();
        byte[] digest = strategy.computeDigest(canonicalBytes);
        byte[] signature = strategy.signDigest(digest, keyPair.getPrivate());
        assertTrue(strategy.verifyDigestSignature(digest, signature, keyPair.getPublic()));
    }

    @Test
    @DisplayName("Test verify with wrong key (should fail)")
    void testVerifyWithWrongKey() throws Exception {
        byte[] canonicalBytes = TEST_JSON_HEADER.getBytes();
        byte[] digest = strategy.computeDigest(canonicalBytes);
        byte[] signature = strategy.signDigest(digest, keyPair.getPrivate());
        assertFalse(strategy.verifyDigestSignature(digest, signature, wrongKeyPair.getPublic()));
    }

    @Test
    @DisplayName("Test verify with modified digest (should fail)")
    void testVerifyWithModifiedDigest() throws Exception {
        byte[] canonicalBytes = TEST_JSON_HEADER.getBytes();
        byte[] digest = strategy.computeDigest(canonicalBytes);
        byte[] signature = strategy.signDigest(digest, keyPair.getPrivate());
        // Modify digest
        byte[] modifiedDigest = Arrays.copyOf(digest, digest.length);
        modifiedDigest[0] ^= 0xFF;
        assertFalse(strategy.verifyDigestSignature(modifiedDigest, signature, keyPair.getPublic()));
    }

    @Test
    @DisplayName("Test header embed/extract helpers")
    void testHeaderEmbedExtractHelpers() throws Exception {
        byte[] canonicalBytes = TEST_JSON_HEADER.getBytes();
        byte[] digest = strategy.computeDigest(canonicalBytes);
        String base64Digest = strategy.encodeBase64(digest);
        String headerWithDigest = strategy.embedDigestInHeader(TEST_JSON_HEADER, base64Digest);
        assertTrue(headerWithDigest.contains(base64Digest));
        String extracted = strategy.extractDigestFromHeader(headerWithDigest);
        assertEquals(base64Digest, extracted);
        byte[] decoded = strategy.decodeBase64(extracted);
        assertArrayEquals(digest, decoded);
    }
} 