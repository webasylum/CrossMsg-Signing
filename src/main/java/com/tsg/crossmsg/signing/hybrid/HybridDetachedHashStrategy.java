package com.tsg.crossmsg.signing.hybrid;

import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

/**
 * Implements the Hybrid/Detached Hash signature strategy:
 * 1. Canonicalize message (XML or JSON)
 * 2. Compute digest (SHA-256)
 * 3. Sign digest with private key (RSA)
 * 4. Embed signed digest in header
 * 5. Verify by recomputing digest and verifying signature
 */
public class HybridDetachedHashStrategy {
    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * Computes the SHA-256 digest of the canonicalized bytes
     */
    public byte[] computeDigest(byte[] canonicalBytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        return md.digest(canonicalBytes);
    }

    /**
     * Signs the digest with the private key (RSA)
     */
    public byte[] signDigest(byte[] digest, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(digest);
        return signature.sign();
    }

    /**
     * Verifies the signed digest with the public key (RSA)
     */
    public boolean verifyDigestSignature(byte[] digest, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(digest);
        return signature.verify(signatureBytes);
    }

    /**
     * Encodes the digest or signature as a base64 string for header embedding
     */
    public String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Decodes a base64 string to bytes
     */
    public byte[] decodeBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    /**
     * Embeds the signed digest in a JSON header (AppHdr.MsgDgst)
     */
    public String embedDigestInHeader(String jsonAppHdr, String base64Digest) {
        // Simple string replace for demo; use a JSON library for production
        if (jsonAppHdr.contains("\"MsgDgst\"")) {
            return jsonAppHdr.replaceAll("\\\"MsgDgst\\\"\\s*:\\s*\\\".*?\\\"", "\"MsgDgst\":\"" + base64Digest + "\"");
        } else {
            return jsonAppHdr.replaceFirst("\\{", "{\"MsgDgst\":\"" + base64Digest + "\", ");
        }
    }

    /**
     * Extracts the base64 digest from a JSON header (AppHdr.MsgDgst)
     */
    public String extractDigestFromHeader(String jsonAppHdr) {
        // Simple regex for demo; use a JSON library for production
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\\"MsgDgst\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(jsonAppHdr);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public String getStrategyLabel() {
        return "Hybrid (Detached Hash + Signature)";
    }
} 