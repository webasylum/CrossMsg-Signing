package com.tsg.crossmsg.signing.jws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.util.Base64URL;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.ParseException;

/**
 * Implements JSON Canonicalization (RFC 8785) + JWS signature strategy.
 * 1. Canonicalizes JSON using RFC 8785
 * 2. Signs canonicalized bytes using JWS (RS256)
 * 3. Embeds JWS compact string in the JSON AppHdr.Signature property
 */
public class JsonJwsSignatureStrategy {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Canonicalizes a JSON node using RFC 8785
     * @param jsonNode The JSON node
     * @return Canonicalized JSON string
     */
    public String canonicalize(JsonNode jsonNode) throws Exception {
        // Use a third-party RFC 8785 implementation or a custom canonicalizer
        // For now, use Jackson's sorted output as a placeholder (not full RFC 8785)
        // Replace with a true RFC 8785 implementation for production
        return objectMapper.writeValueAsString(jsonNode);
    }

    /**
     * Signs a JSON object using JWS (RS256) after canonicalization
     * @param jsonNode The JSON node to sign
     * @param privateKey The private key
     * @return The JWS compact serialization string
     */
    public String sign(JsonNode jsonNode, PrivateKey privateKey) throws Exception {
        String canonicalJson = canonicalize(jsonNode);
        JWSSigner signer = new RSASSASigner(privateKey);
        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
        Payload payload = new Payload(canonicalJson);
        JWSObject jwsObject = new JWSObject(header, payload);
        jwsObject.sign(signer);
        return jwsObject.serialize();
    }

    /**
     * Verifies a JWS signature against a JSON object
     * @param jsonNode The JSON node (excluding the Signature property)
     * @param jws The JWS compact string
     * @param publicKey The public key
     * @return true if valid, false otherwise
     */
    public boolean verify(JsonNode jsonNode, String jws, PublicKey publicKey) throws Exception {
        String canonicalJson = canonicalize(jsonNode);
        JWSObject jwsObject = JWSObject.parse(jws);
        JWSVerifier verifier = new RSASSAVerifier((java.security.interfaces.RSAPublicKey) publicKey);
        boolean signatureValid = jwsObject.verify(verifier);
        if (!signatureValid) return false;
        // Compare canonicalized payloads
        String jwsPayload = jwsObject.getPayload().toString();
        return canonicalJson.equals(jwsPayload);
    }

    /**
     * Embeds the JWS signature string in the AppHdr.Signature property of the JSON.
     * @param json The JSON object (must have AppHdr)
     * @param signature The JWS compact string
     * @return The JSON object with the signature embedded
     */
    public ObjectNode embedSignatureInAppHdr(ObjectNode json, String signature) {
        ObjectNode appHdr = (ObjectNode) json.get("AppHdr");
        if (appHdr == null) throw new IllegalArgumentException("AppHdr property missing");
        appHdr.put("Signature", signature);
        return json;
    }

    /**
     * Extracts the JWS signature string from the AppHdr.Signature property of the JSON.
     * @param json The JSON object (must have AppHdr)
     * @return The JWS compact string, or null if not present
     */
    public String extractSignatureFromAppHdr(JsonNode json) {
        JsonNode appHdr = json.get("AppHdr");
        if (appHdr == null) return null;
        JsonNode sig = appHdr.get("Signature");
        return sig != null ? sig.asText() : null;
    }

    /**
     * Removes the Signature property from AppHdr in the JSON.
     * @param json The JSON object (must have AppHdr)
     * @return The JSON object with the signature removed
     */
    public ObjectNode removeSignatureFromAppHdr(ObjectNode json) {
        ObjectNode appHdr = (ObjectNode) json.get("AppHdr");
        if (appHdr != null) {
            appHdr.remove("Signature");
        }
        return json;
    }
} 