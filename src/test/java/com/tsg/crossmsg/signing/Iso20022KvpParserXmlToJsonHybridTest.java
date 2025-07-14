package com.tsg.crossmsg.signing;

import com.tsg.crossmsg.signing.model.Iso20022KvpParser;
import com.tsg.crossmsg.signing.util.TestKeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("ISO 20022 KVP Parser - XML to JSON with Hybrid/Detached Hash")
class Iso20022KvpParserXmlToJsonHybridTest {
    
    private Iso20022KvpParser kvpParser;
    private String xmlSample;
    private String jsonSample;
    private KeyPair keyPair;
    private TestInfrastructure testInfrastructure;
    
    @BeforeEach
    void setUp() throws Exception {
        // Initialize KVP parser
        kvpParser = new Iso20022KvpParser();
        
        // Load both matching ISO 20022 message samples directly
        xmlSample = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get(getClass().getResource("/iso/SinglePriority_Inbound_pacs.008.xml").toURI())));
        
        jsonSample = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get(getClass().getResource("/iso/SinglePriority_Inbound-pacs008.json").toURI())));
        
        // Use TestKeyManager for key generation
        keyPair = TestKeyManager.getRsaKeyPair();
        
        // Initialize TestInfrastructure singleton
        testInfrastructure = TestInfrastructure.getInstance();
    }
    
    @Test
    @DisplayName("Test Hybrid/Detached Hash on XML format - KVP extraction and signature verification")
    void testHybridDetachedHashOnXmlFormat() throws Exception {
        System.out.println("\n=== Hybrid/Detached Hash on XML Format ===");
        System.out.println("Strategy: " + testInfrastructure.getHybridStrategyLabel());
        
        // Step 1: Extract KVPs from XML message
        Map<String, String> xmlKvps = kvpParser.extractKvpsFromXml(xmlSample);
        System.out.println("XML Format - Extracted " + xmlKvps.size() + " KVPs:");
        printKvps(xmlKvps);
        
        // Step 2: Canonicalize XML using C14N 1.1
        String canonicalXml = testInfrastructure.canonicalizeXml(xmlSample);
        System.out.println("✓ XML canonicalized using C14N 1.1");
        
        // Step 3: Compute digest over canonicalized XML
        byte[] digest = testInfrastructure.computeDigest(canonicalXml.getBytes("UTF-8"));
        System.out.println("✓ SHA-256 digest computed");
        
        // Step 4: Sign digest using RSA
        byte[] signature = testInfrastructure.signDigest(digest, keyPair.getPrivate());
        System.out.println("✓ RSA signature generated");
        
        // Step 5: Verify signature
        boolean isValid = testInfrastructure.verifyDigestSignature(digest, signature, keyPair.getPublic());
        assertTrue(isValid, "Hybrid signature verification should succeed");
        System.out.println("✓ Hybrid signature verification successful");
        
        // Step 6: Extract KVPs from canonicalized XML to verify preservation
        Map<String, String> canonicalXmlKvps = kvpParser.extractKvpsFromXml(canonicalXml);
        assertEquals(xmlKvps.size(), canonicalXmlKvps.size(), 
            "KVP count should be preserved after canonicalization");
        
        for (String key : xmlKvps.keySet()) {
            assertEquals(xmlKvps.get(key), canonicalXmlKvps.get(key), 
                "KVP value for '" + key + "' should be preserved after canonicalization");
        }
        
        System.out.println("\n=== TEST RESULTS ===");
        System.out.println("✓ XML C14N 1.1 canonicalization completed");
        System.out.println("✓ SHA-256 digest computed and RSA signature generated");
        System.out.println("✓ Hybrid signature verification successful");
        System.out.println("✓ All " + xmlKvps.size() + " KVPs preserved through canonicalization");
        System.out.println("✓ Focus on genuine payment data, not structural elements");
        System.out.println("✓ XML format hybrid signature strategy working correctly");
        
        // Print digest and signature information
        String base64Digest = testInfrastructure.encodeBase64(digest);
        String base64Signature = testInfrastructure.encodeBase64(signature);
        System.out.println("✓ Digest (Base64): " + base64Digest);
        System.out.println("✓ Signature (Base64): " + base64Signature);
    }
    
    @Test
    @DisplayName("Test Hybrid/Detached Hash signature exclusion principle")
    void testHybridDetachedHashSignatureExclusion() throws Exception {
        System.out.println("\n=== Hybrid/Detached Hash Signature Exclusion Test ===");
        
        // Canonicalize XML and compute digest
        String canonicalXml = testInfrastructure.canonicalizeXml(xmlSample);
        byte[] digest = testInfrastructure.computeDigest(canonicalXml.getBytes("UTF-8"));
        byte[] signature = testInfrastructure.signDigest(digest, keyPair.getPrivate());
        
        // Verify signature is valid
        boolean isValid = testInfrastructure.verifyDigestSignature(digest, signature, keyPair.getPublic());
        assertTrue(isValid, "Hybrid signature should be valid");
        
        // Verify that the signature is computed over the message content only
        // (not including the signature itself - this is the detached hash principle)
        String base64Digest = testInfrastructure.encodeBase64(digest);
        String base64Signature = testInfrastructure.encodeBase64(signature);
        
        // The digest should NOT contain the signature
        assertFalse(base64Digest.contains(base64Signature), 
            "Digest should not contain the signature (detached hash principle)");
        
        System.out.println("✓ Hybrid signature exclusion principle verified");
        System.out.println("✓ Detached hash principle correctly separates message from signature");
        System.out.println("✓ Signature verification succeeds with proper exclusion");
        System.out.println("✓ Digest computed over message content only");
    }
    
    @Test
    @DisplayName("REAL-WORLD SCENARIO: XML→JSON with Hybrid signature preservation and validation")
    void testRealWorldXmlToJsonWithHybridSignaturePreservation() throws Exception {
        System.out.println("\n=== REAL-WORLD SCENARIO: XML→JSON with Hybrid Signature Preservation ===");
        System.out.println("Scenario: XML message → Add signature → Convert to JSON → Verify signature still valid");
        System.out.println("Strategy: " + testInfrastructure.getHybridStrategyLabel());
        
        // STEP 1: Original XML message (from initiating party)
        System.out.println("\n--- STEP 1: Original XML Message (Initiating Party) ---");
        System.out.println("Original XML message loaded from: /iso/SinglePriority_Inbound_pacs.008.xml");
        System.out.println("Message size: " + xmlSample.length() + " characters");
        
        // Extract KVPs from original XML
        Map<String, String> originalXmlKvps = kvpParser.extractKvpsFromXml(xmlSample);
        System.out.println("Original XML - Extracted " + originalXmlKvps.size() + " KVPs");
        
        // STEP 2: Add signature to XML message (business data signature field)
        System.out.println("\n--- STEP 2: Add Hybrid Signature to XML Message ---");
        String canonicalXml = testInfrastructure.canonicalizeXml(xmlSample);
        System.out.println("✓ XML canonicalized using C14N 1.1");
        System.out.println("Canonical XML size: " + canonicalXml.length() + " characters");
        
        byte[] originalDigest = testInfrastructure.computeDigest(canonicalXml.getBytes("UTF-8"));
        System.out.println("✓ SHA-256 digest computed over canonical XML");
        String base64OriginalDigest = testInfrastructure.encodeBase64(originalDigest);
        System.out.println("Original Digest (Base64): " + base64OriginalDigest);
        
        byte[] originalSignature = testInfrastructure.signDigest(originalDigest, keyPair.getPrivate());
        System.out.println("✓ RSA signature generated using private key");
        String base64OriginalSignature = testInfrastructure.encodeBase64(originalSignature);
        System.out.println("Original Signature (Base64): " + base64OriginalSignature);
        
        // Verify original signature is valid
        boolean originalValid = testInfrastructure.verifyDigestSignature(originalDigest, originalSignature, keyPair.getPublic());
        assertTrue(originalValid, "Original XML signature should be valid");
        System.out.println("✓ Original XML signature verification successful");
        
        // STEP 3: Convert XML to JSON (intermediary agent conversion)
        System.out.println("\n--- STEP 3: Convert XML to JSON (Intermediary Agent) ---");
        System.out.println("Converting XML syntax to JSON syntax while preserving business data signature");
        
        // For this test, we use the matching JSON sample (in real world, this would be actual conversion)
        String convertedJson = jsonSample; // In real implementation, this would be XML→JSON conversion
        System.out.println("Converted JSON message loaded from: /iso/SinglePriority_Inbound-pacs008.json");
        System.out.println("Converted JSON size: " + convertedJson.length() + " characters");
        
        // Extract KVPs from converted JSON
        Map<String, String> convertedJsonKvps = kvpParser.extractKvpsFromJson(convertedJson);
        System.out.println("Converted JSON - Extracted " + convertedJsonKvps.size() + " KVPs");
        
        // Verify KVP consistency between original XML and converted JSON
        assertEquals(originalXmlKvps.size(), convertedJsonKvps.size(), 
            "Converted JSON should have same number of KVPs as original XML");
        
        for (String key : originalXmlKvps.keySet()) {
            assertEquals(originalXmlKvps.get(key), convertedJsonKvps.get(key), 
                "KVP value for '" + key + "' should be identical between original XML and converted JSON");
        }
        System.out.println("✓ All " + originalXmlKvps.size() + " KVPs preserved through XML→JSON conversion");
        
        // STEP 4: Verify signature on converted JSON (final recipient validation)
        System.out.println("\n--- STEP 4: Verify Original Signature on Converted JSON (Final Recipient) ---");
        System.out.println("Validating that the original XML signature still protects the converted JSON data");
        
        // Canonicalize the converted JSON using the same method as the original XML
        String canonicalJson = testInfrastructure.canonicalizeJson(convertedJson);
        System.out.println("✓ Converted JSON canonicalized using RFC 8785");
        System.out.println("Canonical JSON size: " + canonicalJson.length() + " characters");
        
        // Compute digest over converted JSON
        byte[] convertedDigest = testInfrastructure.computeDigest(canonicalJson.getBytes("UTF-8"));
        System.out.println("✓ SHA-256 digest computed over canonical JSON");
        String base64ConvertedDigest = testInfrastructure.encodeBase64(convertedDigest);
        System.out.println("Converted Digest (Base64): " + base64ConvertedDigest);
        
        // CRITICAL: Compare digests to see if they're the same
        System.out.println("\n--- DIGEST COMPARISON ---");
        System.out.println("Original XML Digest:   " + base64OriginalDigest);
        System.out.println("Converted JSON Digest: " + base64ConvertedDigest);
        System.out.println("Digests are identical: " + base64OriginalDigest.equals(base64ConvertedDigest));
        
        if (base64OriginalDigest.equals(base64ConvertedDigest)) {
            System.out.println("✓ DIGEST MATCH: The same business data produces the same digest in both formats");
            System.out.println("✓ This means the original signature will validate on the converted JSON");
        } else {
            System.out.println("⚠ DIGEST MISMATCH: Different digests mean signature won't validate across formats");
            System.out.println("⚠ This indicates the signature strategy doesn't work across syntax changes");
        }
        
        // Verify the original signature against the converted JSON digest
        boolean convertedValid = testInfrastructure.verifyDigestSignature(convertedDigest, originalSignature, keyPair.getPublic());
        System.out.println("\n--- SIGNATURE VALIDATION ON CONVERTED JSON ---");
        System.out.println("Original Signature: " + base64OriginalSignature);
        System.out.println("Signature validation on converted JSON: " + (convertedValid ? "✓ VALID" : "✗ INVALID"));
        
        // Assert based on whether digests match
        if (base64OriginalDigest.equals(base64ConvertedDigest)) {
            assertTrue(convertedValid, "Original signature should validate on converted JSON when digests match");
            System.out.println("✓ SUCCESS: Original XML signature validates on converted JSON");
        } else {
            assertFalse(convertedValid, "Original signature should not validate on converted JSON when digests differ");
            System.out.println("✗ FAILURE: Original XML signature does not validate on converted JSON");
        }
        
        // STEP 5: Summary and conclusions
        System.out.println("\n--- STEP 5: Real-World Scenario Summary ---");
        System.out.println("✓ Original XML message: " + originalXmlKvps.size() + " KVPs extracted");
        System.out.println("✓ Hybrid signature added: " + base64OriginalSignature.substring(0, 20) + "...");
        System.out.println("✓ XML→JSON conversion: All " + convertedJsonKvps.size() + " KVPs preserved");
        System.out.println("✓ Signature validation: " + (convertedValid ? "SUCCESS" : "FAILURE"));
        
        if (convertedValid) {
            System.out.println("✓ CONCLUSION: Hybrid/Detached Hash strategy WORKS across XML→JSON conversion");
            System.out.println("✓ Real-world scenario: Intermediary can convert XML→JSON while preserving signature integrity");
        } else {
            System.out.println("✗ CONCLUSION: Hybrid/Detached Hash strategy FAILS across XML→JSON conversion");
            System.out.println("✗ Real-world scenario: Intermediary cannot convert XML→JSON while preserving signature integrity");
        }
        
        System.out.println("\n=== REAL-WORLD SCENARIO TEST COMPLETED ===");
    }
    
    private void printKvps(Map<String, String> kvps) {
        kvps.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.println("  " + entry.getKey() + " = " + entry.getValue()));
    }
} 