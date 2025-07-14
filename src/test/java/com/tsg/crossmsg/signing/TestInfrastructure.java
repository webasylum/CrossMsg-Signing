package com.tsg.crossmsg.signing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tsg.crossmsg.signing.hybrid.HybridDetachedHashStrategy;
import com.tsg.crossmsg.signing.jws.JsonJwsSignatureStrategy;
import com.tsg.crossmsg.signing.xmlsig.XmlSignatureStrategy;
import com.tsg.crossmsg.signing.util.TestKeyManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Test Infrastructure Singleton providing shared methods for granular signature strategy tests.
 * 
 * This class bridges the gap between test expectations and actual signature strategy implementations,
 * following the Singleton Principle to provide consistent test infrastructure across all granular tests.
 * 
 * Responsibilities:
 * 1. XML C14N + XMLDSig strategy testing methods
 * 2. JSON Canonicalization + JWS strategy testing methods  
 * 3. Hybrid/Detached Hash strategy testing methods
 * 4. Shared utility methods for test data processing
 */
public class TestInfrastructure {
    
    private static TestInfrastructure instance;
    private final ObjectMapper objectMapper;
    private final DocumentBuilder documentBuilder;
    
    // Strategy instances (lazy initialization)
    private JsonJwsSignatureStrategy jwsStrategy;
    private XmlSignatureStrategy xmlStrategy;
    private HybridDetachedHashStrategy hybridStrategy;
    
    private TestInfrastructure() throws Exception {
        this.objectMapper = new ObjectMapper();
        
        // Initialize XML document builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        this.documentBuilder = factory.newDocumentBuilder();
    }
    
    /**
     * Get the singleton instance of TestInfrastructure
     */
    public static synchronized TestInfrastructure getInstance() throws Exception {
        if (instance == null) {
            instance = new TestInfrastructure();
        }
        return instance;
    }
    
    // ============================================================================
    // XML C14N + XMLDSig Strategy Methods
    // ============================================================================
    
    /**
     * Canonicalize XML using C14N 1.1
     */
    public String canonicalizeXml(String xml) throws Exception {
        if (xmlStrategy == null) {
            xmlStrategy = new XmlSignatureStrategy();
        }
        
        // Parse XML to Document
        Document doc = documentBuilder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
        
        // Use Apache Santuario for canonicalization
        org.apache.xml.security.c14n.Canonicalizer canon = 
            org.apache.xml.security.c14n.Canonicalizer.getInstance(
                org.apache.xml.security.transforms.Transforms.TRANSFORM_C14N11_OMIT_COMMENTS);
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        canon.canonicalizeSubtree(doc.getDocumentElement(), baos);
        return baos.toString("UTF-8");
    }
    
    /**
     * Sign XML using XMLDSig
     */
    public String signXml(String canonicalXml, PrivateKey privateKey) throws Exception {
        if (xmlStrategy == null) {
            xmlStrategy = new XmlSignatureStrategy();
        }
        
        // Parse canonicalized XML back to Document
        Document doc = documentBuilder.parse(new ByteArrayInputStream(canonicalXml.getBytes("UTF-8")));
        
        // Load certificate from test-keys directory
        X509Certificate cert = TestKeyManager.getRsaCertificate();
        
        // Sign the document
        Document signedDoc = xmlStrategy.sign(doc, privateKey, cert);
        
        // Convert back to string
        return documentToString(signedDoc);
    }
    
    /**
     * Verify XMLDSig signature
     */
    public boolean verifyXmlSignature(String signedXml, PublicKey publicKey) throws Exception {
        if (xmlStrategy == null) {
            xmlStrategy = new XmlSignatureStrategy();
        }
        
        // Parse signed XML to Document
        Document doc = documentBuilder.parse(new ByteArrayInputStream(signedXml.getBytes("UTF-8")));
        
        // Verify the signature
        return xmlStrategy.verify(doc, publicKey);
    }
    
    // ============================================================================
    // JSON Canonicalization + JWS Strategy Methods
    // ============================================================================
    
    /**
     * Canonicalize JSON using RFC 8785
     */
    public String canonicalizeJson(String json) throws Exception {
        if (jwsStrategy == null) {
            jwsStrategy = new JsonJwsSignatureStrategy();
        }
        
        // Parse JSON to JsonNode
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // Use the strategy's canonicalization method
        return jwsStrategy.canonicalize(jsonNode);
    }
    
    /**
     * Sign JSON using JWS
     */
    public String signJson(String canonicalJson, PrivateKey privateKey) throws Exception {
        if (jwsStrategy == null) {
            jwsStrategy = new JsonJwsSignatureStrategy();
        }
        
        // Parse canonicalized JSON to JsonNode
        JsonNode jsonNode = objectMapper.readTree(canonicalJson);
        
        // Sign using JWS
        return jwsStrategy.sign(jsonNode, privateKey);
    }
    
    /**
     * Verify JWS signature
     */
    public boolean verifyJsonSignature(String jwsToken, PublicKey publicKey) throws Exception {
        if (jwsStrategy == null) {
            jwsStrategy = new JsonJwsSignatureStrategy();
        }
        
        // Extract the signed payload from JWS token
        String signedPayload = extractSignedPayload(jwsToken);
        JsonNode jsonNode = objectMapper.readTree(signedPayload);
        
        // Verify the JWS signature
        return jwsStrategy.verify(jsonNode, jwsToken, publicKey);
    }
    
    /**
     * Extract signed payload from JWS token
     */
    public String extractSignedPayload(String jwsToken) throws Exception {
        String[] parts = jwsToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWS format");
        }
        
        // Decode the payload part
        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
        return new String(payloadBytes, "UTF-8");
    }
    
    // ============================================================================
    // Hybrid/Detached Hash Strategy Methods
    // ============================================================================
    
    /**
     * Compute digest using SHA-256
     */
    public byte[] computeDigest(byte[] content) throws Exception {
        if (hybridStrategy == null) {
            hybridStrategy = new HybridDetachedHashStrategy();
        }
        
        return hybridStrategy.computeDigest(content);
    }
    
    /**
     * Sign digest using RSA
     */
    public byte[] signDigest(byte[] digest, PrivateKey privateKey) throws Exception {
        if (hybridStrategy == null) {
            hybridStrategy = new HybridDetachedHashStrategy();
        }
        
        return hybridStrategy.signDigest(digest, privateKey);
    }
    
    /**
     * Verify digest signature using RSA
     */
    public boolean verifyDigestSignature(byte[] digest, byte[] signature, PublicKey publicKey) throws Exception {
        if (hybridStrategy == null) {
            hybridStrategy = new HybridDetachedHashStrategy();
        }
        
        return hybridStrategy.verifyDigestSignature(digest, signature, publicKey);
    }
    
    /**
     * Encode bytes to Base64
     */
    public String encodeBase64(byte[] data) {
        if (hybridStrategy == null) {
            hybridStrategy = new HybridDetachedHashStrategy();
        }
        
        return hybridStrategy.encodeBase64(data);
    }
    
    // ============================================================================
    // Utility Methods
    // ============================================================================
    
    /**
     * Convert Document to String
     */
    private String documentToString(Document doc) throws Exception {
        javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = factory.newTransformer();
        javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
        java.io.StringWriter writer = new java.io.StringWriter();
        javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }
    
    /**
     * Get RSA key pair for testing
     */
    public KeyPair getRsaKeyPair() throws Exception {
        return TestKeyManager.getRsaKeyPair();
    }
    
    /**
     * Get EC key pair for testing
     */
    public KeyPair getEcKeyPair() throws Exception {
        return TestKeyManager.getEcKeyPair();
    }
    
    /**
     * Get Ed25519 key pair for testing
     */
    public KeyPair getEd25519KeyPair() throws Exception {
        return TestKeyManager.getEd25519KeyPair();
    }
    
    /**
     * Get strategy label for XML C14N + XMLDSig
     */
    public String getXmlC14nStrategyLabel() {
        return "XMLDSig (C14N 1.1 + XMLDSig)";
    }
    
    /**
     * Get strategy label for JSON Canonicalization + JWS
     */
    public String getJsonJwsStrategyLabel() {
        return "JWS (RFC 8785 Canonical JSON + JWS)";
    }
    
    /**
     * Get strategy label for Hybrid/Detached Hash
     */
    public String getHybridStrategyLabel() {
        return "Hybrid (Detached Hash + Signature)";
    }
} 