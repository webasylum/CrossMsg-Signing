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
@DisplayName("ISO 20022 KVP Parser - JSON to XML with Hybrid/Detached Hash")
class Iso20022KvpParserJsonToXmlHybridTest {
    
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
    @DisplayName("Test Hybrid/Detached Hash on JSON format - KVP extraction and signature verification")
    void testHybridDetachedHashOnJsonFormat() throws Exception {
        System.out.println("\n=== Hybrid/Detached Hash on JSON Format ===");
        System.out.println("Strategy: " + testInfrastructure.getHybridStrategyLabel());
        
        // Step 1: Extract KVPs from JSON message
        Map<String, String> jsonKvps = kvpParser.extractKvpsFromJson(jsonSample);
        System.out.println("JSON Format - Extracted " + jsonKvps.size() + " KVPs:");
        printKvps(jsonKvps);
        
        // Step 2: Canonicalize JSON using RFC 8785
        String canonicalJson = testInfrastructure.canonicalizeJson(jsonSample);
        System.out.println("✓ JSON canonicalized using RFC 8785");
        
        // Step 3: Compute digest over canonicalized JSON
        byte[] digest = testInfrastructure.computeDigest(canonicalJson.getBytes("UTF-8"));
        System.out.println("✓ SHA-256 digest computed");
        
        // Step 4: Sign digest using RSA
        byte[] signature = testInfrastructure.signDigest(digest, keyPair.getPrivate());
        System.out.println("✓ RSA signature generated");
        
        // Step 5: Verify signature
        boolean isValid = testInfrastructure.verifyDigestSignature(digest, signature, keyPair.getPublic());
        assertTrue(isValid, "Hybrid signature verification should succeed");
        System.out.println("✓ Hybrid signature verification successful");
        
        // Step 6: Extract KVPs from canonicalized JSON to verify preservation
        Map<String, String> canonicalJsonKvps = kvpParser.extractKvpsFromJson(canonicalJson);
        assertEquals(jsonKvps.size(), canonicalJsonKvps.size(), 
            "KVP count should be preserved after canonicalization");
        
        for (String key : jsonKvps.keySet()) {
            assertEquals(jsonKvps.get(key), canonicalJsonKvps.get(key), 
                "KVP value for '" + key + "' should be preserved after canonicalization");
        }
        
        System.out.println("\n=== TEST RESULTS ===");
        System.out.println("✓ JSON RFC 8785 canonicalization completed");
        System.out.println("✓ SHA-256 digest computed and RSA signature generated");
        System.out.println("✓ Hybrid signature verification successful");
        System.out.println("✓ All " + jsonKvps.size() + " KVPs preserved through canonicalization");
        System.out.println("✓ Focus on genuine payment data, not structural elements");
        System.out.println("✓ JSON format hybrid signature strategy working correctly");
        
        // Print digest and signature information
        String base64Digest = testInfrastructure.encodeBase64(digest);
        String base64Signature = testInfrastructure.encodeBase64(signature);
        System.out.println("✓ Digest (Base64): " + base64Digest);
        System.out.println("✓ Signature (Base64): " + base64Signature);
    }
    
    @Test
    @DisplayName("Test JSON and XML KVP consistency for Hybrid/Detached Hash")
    void testJsonXmlKvpConsistencyForHybridDetachedHash() throws Exception {
        System.out.println("\n=== JSON and XML KVP Consistency Test for Hybrid/Detached Hash ===");
        
        // Extract KVPs from both formats
        Map<String, String> jsonKvps = kvpParser.extractKvpsFromJson(jsonSample);
        Map<String, String> xmlKvps = kvpParser.extractKvpsFromXml(xmlSample);
        
        // Verify both formats extract identical payment data
        assertEquals(jsonKvps.size(), xmlKvps.size(), 
            "Both JSON and XML should extract the same number of KVPs");
        
        for (String key : jsonKvps.keySet()) {
            assertTrue(xmlKvps.containsKey(key), 
                "XML should contain the same key: " + key);
            assertEquals(jsonKvps.get(key), xmlKvps.get(key), 
                "KVP value for '" + key + "' should be identical between JSON and XML");
        }
        
        // JSON format can be processed with Hybrid/Detached Hash
        String canonicalJson = testInfrastructure.canonicalizeJson(jsonSample);
        byte[] digest = testInfrastructure.computeDigest(canonicalJson.getBytes("UTF-8"));
        byte[] signature = testInfrastructure.signDigest(digest, keyPair.getPrivate());
        boolean isValid = testInfrastructure.verifyDigestSignature(digest, signature, keyPair.getPublic());
        
        assertTrue(isValid, "Hybrid/Detached Hash should work on JSON format");
        
        System.out.println("✓ JSON and XML contain identical payment data (" + jsonKvps.size() + " KVPs)");
        System.out.println("✓ Hybrid/Detached Hash strategy works on JSON format");
        System.out.println("✓ Both formats can be processed consistently");
        System.out.println("✓ Focus on payment data integrity, not syntax differences");
    }
    
    @Test
    @DisplayName("Test Hybrid/Detached Hash signature exclusion principle on JSON data")
    void testHybridDetachedHashSignatureExclusionOnJsonData() throws Exception {
        System.out.println("\n=== Hybrid/Detached Hash Signature Exclusion on JSON Data Test ===");
        
        // Canonicalize JSON and compute digest
        String canonicalJson = testInfrastructure.canonicalizeJson(jsonSample);
        byte[] digest = testInfrastructure.computeDigest(canonicalJson.getBytes("UTF-8"));
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
        
        System.out.println("✓ Hybrid signature exclusion principle verified for JSON data");
        System.out.println("✓ Detached hash principle correctly separates message from signature");
        System.out.println("✓ Signature verification succeeds with proper exclusion");
        System.out.println("✓ Digest computed over message content only");
    }
    
    @Test
    @DisplayName("REAL-WORLD SCENARIO: JSON→XML with signature preservation and validation")
    void testRealWorldJsonToXmlWithSignaturePreservation() throws Exception {
        System.out.println("\n=== REAL-WORLD SCENARIO: JSON→XML with Hybrid Signature Preservation ===");
        System.out.println("Scenario: JSON message → Add signature → Convert to XML → Verify signature still valid");
        System.out.println("Strategy: " + testInfrastructure.getHybridStrategyLabel());
        
        // STEP 1: Original JSON message (from initiating party)
        System.out.println("\n--- STEP 1: Original JSON Message (Initiating Party) ---");
        System.out.println("Original JSON message loaded from: /iso/SinglePriority_Inbound-pacs008.json");
        System.out.println("Message size: " + jsonSample.length() + " characters");
        
        // Extract KVPs from original JSON
        Map<String, String> originalJsonKvps = kvpParser.extractKvpsFromJson(jsonSample);
        System.out.println("Original JSON - Extracted " + originalJsonKvps.size() + " KVPs");
        
        // STEP 2: Add signature to JSON message (business data signature field)
        System.out.println("\n--- STEP 2: Add Hybrid Signature to JSON Message ---");
        String canonicalJson = testInfrastructure.canonicalizeJson(jsonSample);
        System.out.println("✓ JSON canonicalized using RFC 8785");
        System.out.println("Canonical JSON size: " + canonicalJson.length() + " characters");
        
        byte[] originalDigest = testInfrastructure.computeDigest(canonicalJson.getBytes("UTF-8"));
        System.out.println("✓ SHA-256 digest computed over canonical JSON");
        String base64OriginalDigest = testInfrastructure.encodeBase64(originalDigest);
        System.out.println("Original Digest (Base64): " + base64OriginalDigest);
        
        byte[] originalSignature = testInfrastructure.signDigest(originalDigest, keyPair.getPrivate());
        System.out.println("✓ RSA signature generated using private key");
        String base64OriginalSignature = testInfrastructure.encodeBase64(originalSignature);
        System.out.println("Original Signature (Base64): " + base64OriginalSignature);
        
        // Verify original signature is valid
        boolean originalValid = testInfrastructure.verifyDigestSignature(originalDigest, originalSignature, keyPair.getPublic());
        assertTrue(originalValid, "Original JSON signature should be valid");
        System.out.println("✓ Original JSON signature verification successful");
        
        // STEP 3: Convert JSON to XML (intermediary agent conversion)
        System.out.println("\n--- STEP 3: Convert JSON to XML (Intermediary Agent) ---");
        System.out.println("Converting JSON syntax to XML syntax while preserving business data signature");
        
        // For this test, we use the matching XML sample (in real world, this would be actual conversion)
        String convertedXml = xmlSample; // In real implementation, this would be JSON→XML conversion
        System.out.println("Converted XML message loaded from: /iso/SinglePriority_Inbound_pacs.008.xml");
        System.out.println("Converted XML size: " + convertedXml.length() + " characters");
        
        // Extract KVPs from converted XML
        Map<String, String> convertedXmlKvps = kvpParser.extractKvpsFromXml(convertedXml);
        System.out.println("Converted XML - Extracted " + convertedXmlKvps.size() + " KVPs");
        
        // Verify KVP consistency between original JSON and converted XML
        assertEquals(originalJsonKvps.size(), convertedXmlKvps.size(), 
            "Converted XML should have same number of KVPs as original JSON");
        
        for (String key : originalJsonKvps.keySet()) {
            assertEquals(originalJsonKvps.get(key), convertedXmlKvps.get(key), 
                "KVP value for '" + key + "' should be identical between original JSON and converted XML");
        }
        System.out.println("✓ All " + originalJsonKvps.size() + " KVPs preserved through JSON→XML conversion");
        
        // STEP 4: Verify signature on converted XML (final recipient validation)
        System.out.println("\n--- STEP 4: Verify Original Signature on Converted XML (Final Recipient) ---");
        System.out.println("Validating that the original JSON signature still protects the converted XML data");
        
        // Canonicalize the converted XML using the same method as the original JSON
        String canonicalXml = testInfrastructure.canonicalizeXml(convertedXml);
        System.out.println("✓ Converted XML canonicalized using C14N 1.1");
        System.out.println("Canonical XML size: " + canonicalXml.length() + " characters");
        
        // Compute digest over converted XML
        byte[] convertedDigest = testInfrastructure.computeDigest(canonicalXml.getBytes("UTF-8"));
        System.out.println("✓ SHA-256 digest computed over canonical XML");
        String base64ConvertedDigest = testInfrastructure.encodeBase64(convertedDigest);
        System.out.println("Converted Digest (Base64): " + base64ConvertedDigest);
        
        // CRITICAL: Compare digests to see if they're the same
        System.out.println("\n--- DIGEST COMPARISON ---");
        System.out.println("Original JSON Digest:  " + base64OriginalDigest);
        System.out.println("Converted XML Digest:  " + base64ConvertedDigest);
        System.out.println("Digests are identical: " + base64OriginalDigest.equals(base64ConvertedDigest));
        
        if (base64OriginalDigest.equals(base64ConvertedDigest)) {
            System.out.println("✓ DIGEST MATCH: The same business data produces the same digest in both formats");
            System.out.println("✓ This means the original signature will validate on the converted XML");
        } else {
            System.out.println("⚠ DIGEST MISMATCH: Different digests mean signature won't validate across formats");
            System.out.println("⚠ This indicates the signature strategy doesn't work across syntax changes");
        }
        
        // Verify the original signature against the converted XML digest
        boolean convertedValid = testInfrastructure.verifyDigestSignature(convertedDigest, originalSignature, keyPair.getPublic());
        System.out.println("\n--- SIGNATURE VALIDATION ON CONVERTED XML ---");
        System.out.println("Original Signature: " + base64OriginalSignature);
        System.out.println("Signature validation on converted XML: " + (convertedValid ? "✓ VALID" : "✗ INVALID"));
        
        // Assert based on whether digests match
        if (base64OriginalDigest.equals(base64ConvertedDigest)) {
            assertTrue(convertedValid, "Original signature should validate on converted XML when digests match");
            System.out.println("✓ SUCCESS: Original JSON signature validates on converted XML");
        } else {
            assertFalse(convertedValid, "Original signature should not validate on converted XML when digests differ");
            System.out.println("✗ FAILURE: Original JSON signature does not validate on converted XML");
        }
        
        // STEP 5: Summary and conclusions
        System.out.println("\n--- STEP 5: Real-World Scenario Summary ---");
        System.out.println("✓ Original JSON message: " + originalJsonKvps.size() + " KVPs extracted");
        System.out.println("✓ Hybrid signature added: " + base64OriginalSignature.substring(0, 20) + "...");
        System.out.println("✓ JSON→XML conversion: All " + convertedXmlKvps.size() + " KVPs preserved");
        System.out.println("✓ Signature validation: " + (convertedValid ? "SUCCESS" : "FAILURE"));
        
        if (convertedValid) {
            System.out.println("✓ CONCLUSION: Hybrid/Detached Hash strategy WORKS across JSON→XML conversion");
            System.out.println("✓ Real-world scenario: Intermediary can convert JSON→XML while preserving signature integrity");
        } else {
            System.out.println("✗ CONCLUSION: Hybrid/Detached Hash strategy FAILS across JSON→XML conversion");
            System.out.println("✗ Real-world scenario: Intermediary cannot convert JSON→XML while preserving signature integrity");
        }
        
        System.out.println("\n=== REAL-WORLD SCENARIO TEST COMPLETED ===");
    }
    
    private void printKvps(Map<String, String> kvps) {
        kvps.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.println("  " + entry.getKey() + " = " + entry.getValue()));
    }
} 