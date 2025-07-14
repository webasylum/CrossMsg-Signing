package com.tsg.crossmsg.signing;

import com.tsg.crossmsg.signing.model.Iso20022KvpParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@DisplayName("ISO 20022 KVP Parser - Pure Parser Accuracy and Cross-Format Consistency")
class Iso20022KvpParserDirectComparisonTest {
    
    private Iso20022KvpParser kvpParser;
    private String xmlSample;
    private String jsonSample;
    
    @BeforeEach
    void setUp() throws Exception {
        // Initialize KVP parser
        kvpParser = new Iso20022KvpParser();
        
        // Load both matching ISO 20022 message samples directly
        xmlSample = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get(getClass().getResource("/iso/SinglePriority_Inbound_pacs.008.xml").toURI())));
        
        jsonSample = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get(getClass().getResource("/iso/SinglePriority_Inbound-pacs008.json").toURI())));
    }
    
    @Test
    @DisplayName("Test KVP parser accuracy - verify identical payment data extraction from XML and JSON")
    void testKvpParserAccuracy() throws Exception {
        System.out.println("\n=== KVP Parser Accuracy Test ===");
        System.out.println("Focus: Pure parser accuracy and cross-format consistency");
        System.out.println("No signature strategies - only KVP extraction validation");
        
        // Step 1: Extract KVPs from XML file
        Map<String, String> xmlKvps = kvpParser.extractKvpsFromXml(xmlSample);
        System.out.println("XML File - Extracted " + xmlKvps.size() + " KVPs:");
        printKvps(xmlKvps);
        
        // Step 2: Extract KVPs from JSON file
        Map<String, String> jsonKvps = kvpParser.extractKvpsFromJson(jsonSample);
        System.out.println("\nJSON File - Extracted " + jsonKvps.size() + " KVPs:");
        printKvps(jsonKvps);
        
        // Step 3: Verify KVP counts match
        assertEquals(xmlKvps.size(), jsonKvps.size(), 
            "Both XML and JSON should extract the same number of KVPs");
        
        // Step 4: Verify all KVP values are identical
        for (String key : xmlKvps.keySet()) {
            assertTrue(jsonKvps.containsKey(key), 
                "JSON should contain the same key: " + key);
            assertEquals(xmlKvps.get(key), jsonKvps.get(key), 
                "KVP value for '" + key + "' should be identical between XML and JSON");
        }
        
        System.out.println("\n=== TEST RESULTS ===");
        System.out.println("✓ KVP parser extracts identical payment data from both formats");
        System.out.println("✓ Parser correctly ignores structural elements");
        System.out.println("✓ All " + xmlKvps.size() + " genuine payment KVPs extracted consistently");
        System.out.println("✓ KVP values are identical across both syntax formats");
        System.out.println("✓ Parser accuracy validated - focus on payment data, not message structure");
        System.out.println("✓ Cross-format consistency confirmed");
    }
    
    @Test
    @DisplayName("Test KVP parser structural element filtering - verify only payment data is extracted")
    void testKvpParserStructuralElementFiltering() throws Exception {
        System.out.println("\n=== KVP Parser Structural Element Filtering Test ===");
        System.out.println("Focus: Verify parser correctly filters structural vs. payment data");
        
        // Extract KVPs from both formats
        Map<String, String> xmlKvps = kvpParser.extractKvpsFromXml(xmlSample);
        Map<String, String> jsonKvps = kvpParser.extractKvpsFromJson(jsonSample);
        
        // Verify that structural elements are NOT in the KVPs
        Set<String> structuralElements = Set.of(
            "BizMsgEnvlp", "Header", "Body", "Document", "AppHdr",
            "FIToFICstmrCdtTrf", "GrpHdr", "CdtTrfTxInf"
        );
        
        for (String structuralElement : structuralElements) {
            assertFalse(xmlKvps.containsKey(structuralElement), 
                "XML KVPs should not contain structural element: " + structuralElement);
            assertFalse(jsonKvps.containsKey(structuralElement), 
                "JSON KVPs should not contain structural element: " + structuralElement);
        }
        
        // Verify that genuine payment data IS in the KVPs
        Set<String> expectedPaymentData = Set.of(
            "From_LEI", "To_LEI", "BusinessMessageIdentifier", "MessageDefinitionIdentifier",
            "GroupHeader_MessageId", "GroupHeader_ControlSum", "GroupHeader_InterbankSettlementDate",
            "Payment_EndToEndId", "Payment_UETR",
            "Payment_InterbankSettlementAmount_Amount", "Payment_InterbankSettlementAmount_Currency"
        );
        
        for (String paymentData : expectedPaymentData) {
            assertTrue(xmlKvps.containsKey(paymentData), 
                "XML KVPs should contain payment data: " + paymentData);
            assertTrue(jsonKvps.containsKey(paymentData), 
                "JSON KVPs should contain payment data: " + paymentData);
        }
        
        System.out.println("✓ Structural elements correctly filtered out");
        System.out.println("✓ Genuine payment data correctly extracted");
        System.out.println("✓ Parser focuses on business data, not message structure");
        System.out.println("✓ Both formats produce identical payment data KVPs");
        System.out.println("✓ Parser filtering accuracy validated");
    }
    
    @Test
    @DisplayName("Test KVP parser cross-format consistency - verify parser behavior is identical")
    void testKvpParserCrossFormatConsistency() throws Exception {
        System.out.println("\n=== KVP Parser Cross-Format Consistency Test ===");
        System.out.println("Focus: Verify parser behaves identically across XML and JSON formats");
        
        // Extract KVPs from both formats
        Map<String, String> xmlKvps = kvpParser.extractKvpsFromXml(xmlSample);
        Map<String, String> jsonKvps = kvpParser.extractKvpsFromJson(jsonSample);
        
        // Verify identical KVP extraction behavior
        assertEquals(xmlKvps.size(), jsonKvps.size(), 
            "Parser should extract same number of KVPs from both formats");
        
        // Verify identical key sets
        Set<String> xmlKeys = xmlKvps.keySet();
        Set<String> jsonKeys = jsonKvps.keySet();
        assertEquals(xmlKeys, jsonKeys, 
            "Parser should extract identical key sets from both formats");
        
        // Verify identical values for each key
        for (String key : xmlKeys) {
            assertEquals(xmlKvps.get(key), jsonKvps.get(key), 
                "Parser should extract identical values for key: " + key);
        }
        
        System.out.println("✓ Parser extracts identical key sets from both formats");
        System.out.println("✓ Parser extracts identical values from both formats");
        System.out.println("✓ Parser behavior is consistent across XML and JSON");
        System.out.println("✓ Cross-format consistency validated");
        System.out.println("✓ Parser accuracy confirmed for both syntax formats");
    }
    
    private void printKvps(Map<String, String> kvps) {
        kvps.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.println("  " + entry.getKey() + " = " + entry.getValue()));
    }
} 