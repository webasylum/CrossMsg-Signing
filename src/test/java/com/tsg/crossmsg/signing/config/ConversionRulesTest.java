package com.tsg.crossmsg.signing.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.Set;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

public class ConversionRulesTest {
    
    @Test
    @DisplayName("Test MessageBuildingBlock description generation")
    void testMessageBuildingBlockDescription() {
        String name = "AccountParties";
        String definition = "Confirmation of information related to parties";
        String expected = "AccountParties \nConfirmation of information related to parties";
        assertEquals(expected, ConversionRules.generateMessageBuildingBlockDescription(name, definition));
    }
    
    @Test
    @DisplayName("Test single occurrence schema generation")
    void testSingleOccurrenceSchema() {
        Map<String, Object> schema = ConversionRules.generateSingleOccurrenceSchema(
            false, false, "ReferencedType");
        
        assertEquals("object", schema.get("type"));
        assertEquals("ReferencedType", schema.get("$ref"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> anyOf = (List<Map<String, Object>>) schema.get("anyOf");
        assertNotNull(anyOf);
        assertEquals(1, anyOf.size());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> firstOption = (Map<String, Object>) anyOf.get(0);
        assertEquals("ReferencedType", firstOption.get("$ref"));
    }
   
    @Test
    @DisplayName("Test multiple occurrence schema generation")
    void testMultipleOccurrenceSchema() {
        Map<String, Object> schema = ConversionRules.generateMultipleOccurrenceSchema("InvestmentPlan17", 1, 50);
        
        // Verify anyOf array
        assertTrue(schema.containsKey("anyOf"));
        List<Map<String, Object>> anyOf = (List<Map<String, Object>>) schema.get("anyOf");
        assertEquals(2, anyOf.size());
        
        // Verify single item schema
        Map<String, Object> singleItem = anyOf.get(0);
        assertEquals("#InvestmentPlan17", singleItem.get("$ref"));
        
        // Verify array schema
        Map<String, Object> arraySchema = anyOf.get(1);
        assertEquals("array", arraySchema.get("type"));
        assertEquals(1, arraySchema.get("minItems"));
        assertEquals(50, arraySchema.get("maxItems"));
        
        // Verify items reference
        Map<String, Object> items = (Map<String, Object>) arraySchema.get("items");
        assertEquals("#InvestmentPlan17", items.get("$ref"));
    }
    
    @Test
    @DisplayName("Test ExternalSchema generation")
    void testExternalSchemaGeneration() {
        String name = "SupplementaryDataEnvelope1";
        String definition = "Technical component that contains the validated supplementary data";
        
        Map<String, Object> schema = ConversionRules.generateExternalSchema(name, definition);
        assertEquals(name, schema.get("$anchor"));
        assertEquals(definition, schema.get("description"));
    }
    
    @Test
    @DisplayName("Test JSON Schema version and constants")
    void testJsonSchemaConstants() {
        assertEquals("https://json-schema.org/draft/2020-12/schema", ConversionRules.JSON_SCHEMA_VERSION);
        assertEquals("urn:iso:std:iso:20022:tech:json:", ConversionRules.BASE_URN);
        assertEquals("object", ConversionRules.SCHEMA_TYPE);
        assertFalse(ConversionRules.ADDITIONAL_PROPERTIES);
        assertEquals("Document", ConversionRules.ROOT_ELEMENT);
    }
    
    @Test
    @DisplayName("Test schema ID generation")
    void testSchemaIdGeneration() {
        String identifier = "acmt.002.001.08";
        String expectedId = "urn:iso:std:iso:20022:tech:json:acmt.002.001.08";
        assertEquals(expectedId, ConversionRules.generateSchemaId(identifier));
        
        // Test invalid identifier
        assertThrows(IllegalArgumentException.class, () -> {
            ConversionRules.generateSchemaId("invalid.identifier");
        });
    }
    
    @Test
    @DisplayName("Test schema description generation")
    void testSchemaDescriptionGeneration() {
        LocalDate date = LocalDate.of(2024, 7, 29);
        String software = "ISO 20022 Schema Generator v1.0";
        String documentation = "Sample documentation text";
        
        String description = ConversionRules.generateSchemaDescription(date, software, documentation);
        assertTrue(description.startsWith("Generated from ISO 20022 repository of 2024-07-29"));
        assertTrue(description.contains("by ISO 20022 Schema Generator v1.0"));
        assertTrue(description.contains("Sample documentation text"));
        
        // Test without documentation
        String descriptionWithoutDoc = ConversionRules.generateSchemaDescription(date, software, null);
        assertTrue(descriptionWithoutDoc.startsWith("Generated from ISO 20022 repository of 2024-07-29"));
        assertTrue(descriptionWithoutDoc.contains("by ISO 20022 Schema Generator v1.0"));
        assertFalse(descriptionWithoutDoc.contains("Sample documentation text"));
    }
    
    @Test
    @DisplayName("Test root element reference generation")
    void testRootElementReferenceGeneration() {
        Map<String, String> reference = ConversionRules.generateRootElementReference("Document");
        assertEquals("#_Document", reference.get("$ref"));
        
        reference = ConversionRules.generateRootElementReference("CustomRoot");
        assertEquals("#_CustomRoot", reference.get("$ref"));
    }
    
    @Test
    @DisplayName("Test encoding rules (Section 6.1)")
    void testEncodingRules() {
        assertEquals("UTF-8", ConversionRules.ENCODING);
    }
    
    @Test
    @DisplayName("Test MessageDefinitionIdentifier rules (Section 8.2)")
    void testMessageDefinitionIdentifierRules() {
        assertEquals(".", ConversionRules.IDENTIFIER_SEPARATOR);
        assertEquals(4, ConversionRules.IDENTIFIER_COMPONENTS.length);
        assertEquals("BusinessArea", ConversionRules.IDENTIFIER_COMPONENTS[0]);
        assertEquals("Functionality", ConversionRules.IDENTIFIER_COMPONENTS[1]);
        assertEquals("Flavour", ConversionRules.IDENTIFIER_COMPONENTS[2]);
        assertEquals("Version", ConversionRules.IDENTIFIER_COMPONENTS[3]);
        
        // Test identifier validation
        assertTrue(ConversionRules.isValidMessageDefinitionIdentifier("acmt.002.001.08"));
        assertFalse(ConversionRules.isValidMessageDefinitionIdentifier("acmt.002.001"));
        assertFalse(ConversionRules.isValidMessageDefinitionIdentifier(""));
        assertFalse(ConversionRules.isValidMessageDefinitionIdentifier(null));
    }
    
    @Test
    @DisplayName("Test file naming rules (Section 8.3.1)")
    void testFileNamingRules() {
        assertEquals(".schema.json", ConversionRules.SCHEMA_FILE_SUFFIX);
        assertEquals("-", ConversionRules.NAME_SEPARATOR);
        
        // Test schema filename generation
        String filename = ConversionRules.generateSchemaFilename("acmt.002.001.08", "AccountDetailsConfirmationV08");
        assertEquals("acmt.002.001.08-AccountDetailsConfirmationV08.schema.json", filename);
    }
    
    @Test
    @DisplayName("Test namespace constants")
    void testNamespaces() {
        assertNotNull(ConversionRules.DEFAULT_NAMESPACE);
        assertNotNull(ConversionRules.BIZ_MSG_ENVLP_NAMESPACE);
        assertTrue(ConversionRules.DEFAULT_NAMESPACE.startsWith("urn:iso:std:iso:20022"));
        assertTrue(ConversionRules.BIZ_MSG_ENVLP_NAMESPACE.startsWith("urn:iso:std:iso:20022"));
    }
    
    @Test
    @DisplayName("Test element transformations")
    void testElementTransformations() {
        Map<String, String> transformations = ConversionRules.ELEMENT_TRANSFORMATIONS;
        assertNotNull(transformations);
        assertFalse(transformations.isEmpty());
        
        // Test currency transformations
        assertEquals("currency", transformations.get("Ccy"));
        assertEquals("amount", transformations.get("Amt"));
        
        // Test common elements
        assertEquals("messageId", transformations.get("MsgId"));
        assertEquals("creationDateTime", transformations.get("CreDtTm"));
    }
    
    @Test
    @DisplayName("Test preserved elements")
    void testPreservedElements() {
        Set<String> preserved = ConversionRules.PRESERVED_ELEMENTS;
        assertNotNull(preserved);
        assertTrue(preserved.contains("Signature"));
        assertTrue(preserved.contains("Sgntr"));
        assertTrue(preserved.contains("AppHdr"));
    }
    
    @Test
    @DisplayName("Test non-canonical elements")
    void testNonCanonicalElements() {
        Set<String> nonCanonical = ConversionRules.NON_CANONICAL_ELEMENTS;
        assertNotNull(nonCanonical);
        assertTrue(nonCanonical.contains("CreDtTm"));
        assertTrue(nonCanonical.contains("MsgId"));
        assertTrue(nonCanonical.contains("NbOfTxs"));
    }
    
    @Test
    @DisplayName("Test array elements")
    void testArrayElements() {
        Set<String> arrays = ConversionRules.ARRAY_ELEMENTS;
        assertNotNull(arrays);
        assertTrue(arrays.contains("CdtTrfTxInf"));
        assertTrue(arrays.contains("PmtInf"));
    }
    
    @Test
    @DisplayName("Test nested structures")
    void testNestedStructures() {
        Map<String, String> nested = ConversionRules.NESTED_STRUCTURES;
        assertNotNull(nested);
        assertEquals("debtor", nested.get("Dbtr"));
        assertEquals("creditor", nested.get("Cdtr"));
        assertEquals("debtorAgent", nested.get("DbtrAgt"));
        assertEquals("creditorAgent", nested.get("CdtrAgt"));
    }
    
    @Test
    @DisplayName("Test required elements")
    void testRequiredElements() {
        Set<String> required = ConversionRules.REQUIRED_ELEMENTS;
        assertNotNull(required);
        assertTrue(required.contains("MsgId"));
        assertTrue(required.contains("CreDtTm"));
        assertTrue(required.contains("NbOfTxs"));
    }
    
    @Test
    @DisplayName("Test validation rules")
    void testValidationRules() {
        assertTrue(ConversionRules.MAX_MESSAGE_SIZE > 0);
        assertNotNull(ConversionRules.ERROR_INVALID_NAMESPACE);
        assertNotNull(ConversionRules.ERROR_MISSING_REQUIRED_ELEMENT);
        assertNotNull(ConversionRules.ERROR_INVALID_CURRENCY);
        assertNotNull(ConversionRules.ERROR_MESSAGE_TOO_LARGE);
        assertNotNull(ConversionRules.ERROR_INVALID_ENCODING);
        assertNotNull(ConversionRules.ERROR_INVALID_IDENTIFIER);
    }
    
    @Test
    @DisplayName("Test MessageComponent schema generation")
    void testMessageComponentSchemaGeneration() {
        // Create test data
        String name = "SupplementaryData1";
        String definition = "Additional information where Envelope is Mandatory and PlaceAndName is Optional";
        Map<String, Object> properties = new HashMap<>();
        
        // Add PlcAndNm property
        Map<String, Object> plcAndNm = new HashMap<>();
        plcAndNm.put("$ref", "#Max350Text");
        plcAndNm.put("description", "PlaceAndName \nUnambiguous reference to the location where the supplementary data must be inserted in the message instance.\nIn the case of XML, this is expressed by a valid XPath.");
        properties.put("PlcAndNm", plcAndNm);
        
        // Add Envlp property
        Map<String, Object> envlp = new HashMap<>();
        envlp.put("$ref", "#SupplementaryDataEnvelope1");
        envlp.put("description", "Envelope \nTechnical element wrapping the supplementary data.");
        properties.put("Envlp", envlp);
        
        List<String> requiredElements = Arrays.asList("Envlp");
        
        // Generate schema
        Map<String, Object> schema = ConversionRules.generateMessageComponentSchema(
            name, definition, properties, requiredElements, true);
        
        // Verify schema structure
        assertEquals(definition, schema.get("description"));
        assertEquals("object", schema.get("type"));
        assertFalse((Boolean) schema.get("additionalProperties"));
        assertEquals(requiredElements, schema.get("required"));
        assertEquals(properties, schema.get("properties"));
        assertEquals(name, schema.get("$anchor"));
        
        // Test without required elements
        schema = ConversionRules.generateMessageComponentSchema(
            name, definition, properties, null, false);
        assertEquals(1, schema.get("minProperties"));
        assertFalse(schema.containsKey("required"));
    }
    
    @Test
    @DisplayName("Test ChoiceComponent schema generation")
    void testChoiceComponentSchemaGeneration() {
        // Create test data
        String name = "BlockedStatusReason2Choice";
        String definition = "Choice of formats for a blocked status reason.";
        Map<String, Object> properties = new HashMap<>();
        
        // Add NoSpcfdRsn property
        properties.put("NoSpcfdRsn", ConversionRules.createReferencedProperty("NoReasonCode", null));
        
        // Add Rsn property
        properties.put("Rsn", ConversionRules.createArrayProperty("BlockedStatusReason2"));
        
        // Generate schema
        Map<String, Object> schema = ConversionRules.generateChoiceComponentSchema(
            name, definition, properties);
        
        // Verify schema structure
        assertEquals(name, schema.get("$anchor"));
        assertEquals(definition, schema.get("description"));
        assertEquals("object", schema.get("type"));
        assertFalse((Boolean) schema.get("additionalProperties"));
        assertEquals(1, schema.get("minProperties"));
        assertEquals(1, schema.get("maxProperties"));
        assertEquals(properties, schema.get("properties"));
    }
    
    @Test
    @DisplayName("Test property creation utilities")
    void testPropertyCreationUtilities() {
        // Test referenced property
        Map<String, Object> refProperty = ConversionRules.createReferencedProperty("Max350Text", "Test description");
        assertEquals("#Max350Text", refProperty.get("$ref"));
        assertEquals("Test description", refProperty.get("description"));
        
        // Test referenced property without description
        refProperty = ConversionRules.createReferencedProperty("Max350Text", null);
        assertEquals("#Max350Text", refProperty.get("$ref"));
        assertFalse(refProperty.containsKey("description"));
        
        // Test array property
        Map<String, Object> arrayProperty = ConversionRules.createArrayProperty("BlockedStatusReason2");
        assertEquals("array", arrayProperty.get("type"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> items = (Map<String, Object>) arrayProperty.get("items");
        assertEquals("#BlockedStatusReason2", items.get("$ref"));
    }
    
    @Test
    @DisplayName("Test MessageElement schema generation")
    void testMessageElementSchemaGeneration() {
        // Test single occurrence with reference
        Map<String, Object> schema = ConversionRules.generateMessageElementSchema(
            "AddtlInf", "AdditionalInformation", "Additional information text",
            false, false, "Max350Text", 1, 1);
        
        assertEquals("AdditionalInformation \nAdditional information text", schema.get("description"));
        assertEquals("#Max350Text", schema.get("$ref"));
        
        // Test single occurrence with string type
        schema = ConversionRules.generateMessageElementSchema(
            "AcctSvcr", "AccountServicer", "Party that manages the account",
            true, false, null, 1, 1);
        
        assertEquals("AccountServicer \nParty that manages the account", schema.get("description"));
        assertEquals("string", schema.get("type"));
        
        // Test multiple occurrences with reference
        schema = ConversionRules.generateMessageElementSchema(
            "CshSttlm", "CashSettlement", 
            "Cash settlement standing instruction associated to transactions on the account",
            false, false, "CashSettlement1", 1, 8);
        
        assertEquals("CashSettlement \nCash settlement standing instruction associated to transactions on the account", 
            schema.get("description"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> anyOf = (List<Map<String, Object>>) schema.get("anyOf");
        assertEquals(2, anyOf.size());
        
        // Verify single item schema
        Map<String, Object> singleItem = anyOf.get(0);
        assertEquals("#CashSettlement1", singleItem.get("$ref"));
        
        // Verify array schema
        Map<String, Object> arraySchema = anyOf.get(1);
        assertEquals("array", arraySchema.get("type"));
        assertEquals(1, arraySchema.get("minItems"));
        assertEquals(8, arraySchema.get("maxItems"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> items = (Map<String, Object>) arraySchema.get("items");
        assertEquals("#CashSettlement1", items.get("$ref"));
    }
    
    @Test
    @DisplayName("Test MessageElement description generation")
    void testMessageElementDescriptionGeneration() {
        String elementName = "AccountOwner";
        String definition = "Party that legally owns the account";
        String expected = "AccountOwner \nParty that legally owns the account";
        
        assertEquals(expected, ConversionRules.generateMessageElementDescription(elementName, definition));
    }
    
    @Test
    @DisplayName("Test single occurrence schema creation")
    void testSingleOccurrenceSchemaCreation() {
        // Test non-composite association end
        Map<String, Object> schema = ConversionRules.createSingleOccurrenceSchema(true, false, null);
        assertEquals("string", schema.get("type"));
        
        // Test composite association end
        schema = ConversionRules.createSingleOccurrenceSchema(true, true, "PartyIdentification125Choice");
        assertEquals("#PartyIdentification125Choice", schema.get("$ref"));
        
        // Test regular element
        schema = ConversionRules.createSingleOccurrenceSchema(false, false, "GenericIdentification82");
        assertEquals("#GenericIdentification82", schema.get("$ref"));
    }
    
    @Test
    @DisplayName("Test multiple occurrence schema creation")
    void testMultipleOccurrenceSchemaCreation() {
        // Test with reference type
        Map<String, Object> schema = ConversionRules.createMultipleOccurrenceSchema(
            false, false, "GenericIdentification82", 1, -1);
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> anyOf = (List<Map<String, Object>>) schema.get("anyOf");
        assertEquals(2, anyOf.size());
        
        // Verify single item schema
        Map<String, Object> singleItem = anyOf.get(0);
        assertEquals("#GenericIdentification82", singleItem.get("$ref"));
        
        // Verify array schema
        Map<String, Object> arraySchema = anyOf.get(1);
        assertEquals("array", arraySchema.get("type"));
        assertEquals(1, arraySchema.get("minItems"));
        assertFalse(arraySchema.containsKey("maxItems")); // No max items for unbounded
        
        @SuppressWarnings("unchecked")
        Map<String, Object> items = (Map<String, Object>) arraySchema.get("items");
        assertEquals("#GenericIdentification82", items.get("$ref"));
        
        // Test with string type
        schema = ConversionRules.createMultipleOccurrenceSchema(
            true, false, null, 1, 5);
        
        anyOf = (List<Map<String, Object>>) schema.get("anyOf");
        singleItem = anyOf.get(0);
        assertEquals("string", singleItem.get("type"));
        
        arraySchema = anyOf.get(1);
        assertEquals("array", arraySchema.get("type"));
        assertEquals(1, arraySchema.get("minItems"));
        assertEquals(5, arraySchema.get("maxItems"));
        
        items = (Map<String, Object>) arraySchema.get("items");
        assertEquals("string", items.get("type"));
    }
    
    @Test
    @DisplayName("Test decimal pattern generation")
    void testDecimalPatternGeneration() {
        // Test case 1a: totalDigits > 0, fractionDigits > 0
        String pattern = ConversionRules.generateDecimalPattern(18, 17, null, null, null, null);
        assertTrue(pattern.startsWith("^[+-]?0*(("));
        assertTrue(pattern.contains("(0|[1-9][0-9]{0,17})([.]0*)?"));
        assertTrue(pattern.contains("([.][0-9]{1,17}0*)"));
        assertTrue(pattern.endsWith("))$"));
        
        // Test case 1b: totalDigits > 0, fractionDigits = 0
        pattern = ConversionRules.generateDecimalPattern(5, 0, 1.0, null, null, null);
        assertTrue(pattern.startsWith("^[+]?0*(("));
        assertTrue(pattern.contains("(0|[1-9][0-9]{0,4})([.]0*)?"));
        assertTrue(pattern.contains("[.][0]+"));
        assertTrue(pattern.endsWith(")$"));
        
        // Test case 1c: totalDigits > 0, fractionDigits not defined
        pattern = ConversionRules.generateDecimalPattern(11, null, 0.0, 100.0, null, null);
        assertTrue(pattern.startsWith("^[+]?0*(("));
        assertTrue(pattern.contains("(0|[1-9][0-9]{0,10})([.]0*)?"));
        assertTrue(pattern.contains("([.][0-9]{1,11}0*)"));
        assertTrue(pattern.endsWith("))$"));
        
        // Test case 2a: totalDigits not defined, fractionDigits > 0
        pattern = ConversionRules.generateDecimalPattern(null, 10, null, null, null, null);
        assertTrue(pattern.startsWith("^[+-]?"));
        assertTrue(pattern.contains("([0-9]+([.][0-9]{0,10}0*)?|"));
        assertTrue(pattern.contains("0*[.][0-9]{1,10}0*)"));
        assertTrue(pattern.endsWith(")$"));
        
        // Test case 2b: totalDigits not defined, fractionDigits = 0
        pattern = ConversionRules.generateDecimalPattern(null, 0, null, null, null, null);
        assertTrue(pattern.startsWith("^[+-]?0*(("));
        assertTrue(pattern.contains("(0|[1-9][0-9]*)([.]0*)?"));
        assertTrue(pattern.contains("[.][0]+"));
        assertTrue(pattern.endsWith(")$"));
        
        // Test case 2c: totalDigits not defined, fractionDigits not defined
        pattern = ConversionRules.generateDecimalPattern(null, null, null, null, null, null);
        assertTrue(pattern.startsWith("^[+-]?"));
        assertTrue(pattern.contains("(([0-9]+[.]?)|"));
        assertTrue(pattern.contains("(0*[.][0-9]+)|"));
        assertTrue(pattern.contains("((?=[0-9.]{2,})[0-9]*[.][0-9]*))"));
        assertTrue(pattern.endsWith(")$"));
    }
    
    @Test
    @DisplayName("Test Decimal schema generation")
    void testDecimalSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateDecimalSchema(
            "DecimalNumber",
            "Number of objects represented as a decimal number, for example 0.75 or 45.6.",
            18, 17, null, null, null, null);
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^[+-]?0*(("));
        assertEquals("Number of objects represented as a decimal number, for example 0.75 or 45.6.",
            schema.get("description"));
        assertEquals("DecimalNumber", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Quantity schema generation")
    void testQuantitySchemaGeneration() {
        // Test positive number
        Map<String, Object> schema = ConversionRules.generateQuantitySchema(
            "Max5PositiveNumber",
            "unitCode=\n\nNumber of objects represented as a positive integer.",
            5, 0, 1.0, null, null, null);
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^[+]?0*(("));
        assertEquals("unitCode=\n\nNumber of objects represented as a positive integer.",
            schema.get("description"));
        assertEquals("Max5PositiveNumber", schema.get("$anchor"));
        
        // Test non-negative number with fraction
        schema = ConversionRules.generateQuantitySchema(
            "Max2Fraction1NonNegativeNumber",
            "unitCode=\n\nNumber of objects represented as a non negative decimal number, for example 1.1 or 8.0.",
            2, 1, 0.0, 9.9, null, null);
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^[+]?0*(("));
        assertEquals("unitCode=\n\nNumber of objects represented as a non negative decimal number, for example 1.1 or 8.0.",
            schema.get("description"));
        assertEquals("Max2Fraction1NonNegativeNumber", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Rate schema generation")
    void testRateSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateRateSchema(
            "PercentageBoundedRate",
            "Rate expressed as a percentage, ie, in hundredths, eg, 0.7 is 7/10 of a percent, and 7.0 is 7%.",
            100.0,
            11, 10, 0.0, 100.0, null, null);
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^[+]?0*(("));
        assertEquals("baseValue=100.0\n\nRate expressed as a percentage, ie, in hundredths, eg, 0.7 is 7/10 of a percent, and 7.0 is 7%.",
            schema.get("description"));
        assertEquals("PercentageBoundedRate", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Amount schema generation")
    void testAmountSchemaGeneration() {
        // Test with currency identifier set
        Map<String, Object> schema = ConversionRules.generateAmountSchema(
            "ActiveCurrencyAndAmount",
            "A number of monetary units specified in an active currency where the unit of currency is explicit and compliant with ISO 4217.\n\ncurrencyIdentifierSet=",
            "ActiveCurrencyCode",
            18, 5, null, null, null, null);
        
        assertEquals("object", schema.get("type"));
        assertFalse((Boolean) schema.get("additionalProperties"));
        
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) schema.get("required");
        assertEquals(2, required.size());
        assertTrue(required.contains("amt"));
        assertTrue(required.contains("Ccy"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertEquals(2, properties.size());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> amount = (Map<String, Object>) properties.get("amt");
        assertEquals("string", amount.get("type"));
        assertTrue(((String) amount.get("pattern")).startsWith("^[+-]?0*(("));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> currency = (Map<String, Object>) properties.get("Ccy");
        assertEquals("#ActiveCurrencyCode", currency.get("$ref"));
        
        assertEquals("A number of monetary units specified in an active currency where the unit of currency is explicit and compliant with ISO 4217.\n\ncurrencyIdentifierSet=",
            schema.get("description"));
        assertEquals("ActiveCurrencyAndAmount", schema.get("$anchor"));
        
        // Test without currency identifier set
        schema = ConversionRules.generateAmountSchema(
            "ImpliedCurrencyAndAmount",
            "Number of monetary units specified in a currency where the unit of currency is implied by the context and compliant with ISO 4217. The decimal separator is a dot.\nNote: a zero amount is considered a positive amount.",
            null,
            18, 5, null, null, null, null);
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^[+-]?0*(("));
        assertEquals("Number of monetary units specified in a currency where the unit of currency is implied by the context and compliant with ISO 4217. The decimal separator is a dot.\nNote: a zero amount is considered a positive amount.",
            schema.get("description"));
        assertEquals("ImpliedCurrencyAndAmount", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Duration schema generation")
    void testDurationSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateDurationSchema(
            "Duration",
            "W3C XML Schema Built-in datatype \"duration\"");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^-?P"));
        assertEquals("W3C XML Schema Built-in datatype \"duration\"", schema.get("description"));
        assertEquals("Duration", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test DateTime schema generation")
    void testDateTimeSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateDateTimeSchema(
            "ISODateTime",
            "A particular point in the progression of time defined by a mandatory date and a mandatory time component, the time being expressed in hours, minutes, seconds and milliseconds.");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^-?([1-9][0-9]{3,}|0[0-9]{3})-"));
        assertEquals("A particular point in the progression of time defined by a mandatory date and a mandatory time component, the time being expressed in hours, minutes, seconds and milliseconds.",
            schema.get("description"));
        assertEquals("ISODateTime", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test NormalisedDateTime schema generation")
    void testNormalisedDateTimeSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateNormalisedDateTimeSchema(
            "ISONormalisedDateTime",
            "A particular point in the progression of time defined by a mandatory date and a mandatory time component, the time being expressed in hours, minutes, seconds and milliseconds.");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^-?([1-9][0-9]{3,}|0[0-9]{3})-"));
        assertTrue(((String) schema.get("pattern")).endsWith("Z$"));
        assertEquals("A particular point in the progression of time defined by a mandatory date and a mandatory time component, the time being expressed in hours, minutes, seconds and milliseconds.",
            schema.get("description"));
        assertEquals("ISONormalisedDateTime", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Date schema generation")
    void testDateSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateDateSchema(
            "ISODate",
            "A particular point in the progression of time defined by a mandatory date component.");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^-?([1-9][0-9]{3,}|0[0-9]{3})-"));
        assertEquals("A particular point in the progression of time defined by a mandatory date component.",
            schema.get("description"));
        assertEquals("ISODate", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Time schema generation")
    void testTimeSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateTimeSchema(
            "ISOTime",
            "A particular point in the progression of time defined by a mandatory time component, the time being expressed in hours, minutes, seconds and milliseconds.");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^(([01][0-9]|2[0-3]):"));
        assertEquals("A particular point in the progression of time defined by a mandatory time component, the time being expressed in hours, minutes, seconds and milliseconds.",
            schema.get("description"));
        assertEquals("ISOTime", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test YearMonth schema generation")
    void testYearMonthSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateYearMonthSchema(
            "ISOYearMonth",
            "Month within a particular calendar year.");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^-?([1-9][0-9]{3,})-"));
        assertEquals("Month within a particular calendar year.", schema.get("description"));
        assertEquals("ISOYearMonth", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test MonthDay schema generation")
    void testMonthDaySchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateMonthDaySchema(
            "ISOMonthDay",
            "W3C XML Schema Built-in datatype \"gMonthDay\".");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^--"));
        assertEquals("W3C XML Schema Built-in datatype \"gMonthDay\".", schema.get("description"));
        assertEquals("ISOMonthDay", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Year schema generation")
    void testYearSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateYearSchema(
            "ISOYear",
            "Year represented by YYYY (ISO 8601).");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^-?([1-9][0-9]{3,}|0[0-9]{3})"));
        assertEquals("Year represented by YYYY (ISO 8601).", schema.get("description"));
        assertEquals("ISOYear", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Month schema generation")
    void testMonthSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateMonthSchema(
            "ISOMonth",
            "Month represented by MM (ISO 8601).");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^--"));
        assertEquals("Month represented by MM (ISO 8601).", schema.get("description"));
        assertEquals("ISOMonth", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test Day schema generation")
    void testDaySchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateDaySchema(
            "ISODay",
            "W3C XML Schema Built-in datatype \"gDay\".");
        
        assertEquals("string", schema.get("type"));
        assertTrue(((String) schema.get("pattern")).startsWith("^---"));
        assertEquals("W3C XML Schema Built-in datatype \"gDay\".", schema.get("description"));
        assertEquals("ISODay", schema.get("$anchor"));
    }
    
    @Test
    @DisplayName("Test CurrencyAmount schema generation")
    void testCurrencyAmountSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateCurrencyAmountSchema(
            "CurrencyAmount",
            "Amount of money in a specific currency",
            "CurrencyCode");
        
        assertEquals("object", schema.get("type"));
        assertEquals("CurrencyAmount", schema.get("$anchor"));
        assertEquals("Amount of money in a specific currency", schema.get("description"));
        assertEquals(Arrays.asList("amt", "Ccy"), schema.get("required"));
        assertEquals(2, schema.get("minProperties"));
        assertEquals(2, schema.get("maxProperties"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertNotNull(properties.get("amt"));
        assertNotNull(properties.get("Ccy"));
    }
    
    @Test
    @DisplayName("Test ChoiceComponent schema generation with cardinality")
    void testChoiceComponentSchemaGenerationWithCardinality() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("option1", generateTextSchema("Option1", "First option", 1, 10, null, null));
        properties.put("option2", generateTextSchema("Option2", "Second option", 1, 10, null, null));
        
        Map<String, Object> schema = ConversionRules.generateChoiceComponentSchema(
            "ChoiceComponent",
            "A choice between two options",
            properties,
            1,
            1);
        
        assertEquals("object", schema.get("type"));
        assertEquals("ChoiceComponent", schema.get("$anchor"));
        assertEquals("A choice between two options", schema.get("description"));
        assertEquals(1, schema.get("minProperties"));
        assertEquals(1, schema.get("maxProperties"));
        assertEquals(properties, schema.get("properties"));
    }
    
    @Test
    @DisplayName("Test MessageComponent schema generation with required fields")
    void testMessageComponentSchemaGenerationWithRequiredFields() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("field1", generateTextSchema("Field1", "First field", 1, 10, null, null));
        properties.put("field2", generateTextSchema("Field2", "Second field", 1, 10, null, null));
        
        List<String> requiredElements = Arrays.asList("field1");
        
        Map<String, Object> schema = ConversionRules.generateMessageComponentSchema(
            "MessageComponent",
            "A message component with required fields",
            properties,
            requiredElements);
        
        assertEquals("object", schema.get("type"));
        assertEquals("MessageComponent", schema.get("$anchor"));
        assertEquals("A message component with required fields", schema.get("description"));
        assertEquals(1, schema.get("minProperties"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> schemaProperties = (Map<String, Object>) schema.get("properties");
        assertEquals(properties, schemaProperties);
        
        @SuppressWarnings("unchecked")
        List<String> schemaRequired = (List<String>) schema.get("required");
        assertEquals(requiredElements, schemaRequired);
    }
    
    @Test
    @DisplayName("Test Array schema generation")
    void testArraySchemaGeneration() {
        Map<String, Object> items = generateTextSchema("Item", "Array item", 1, 10, null, null);
        
        Map<String, Object> schema = ConversionRules.generateArraySchema(
            "ArrayType",
            "An array of items",
            items,
            1,
            10);
        
        assertEquals("array", schema.get("type"));
        assertEquals("ArrayType", schema.get("$anchor"));
        assertEquals("An array of items", schema.get("description"));
        assertEquals(1, schema.get("minItems"));
        assertEquals(10, schema.get("maxItems"));
        assertEquals(items, schema.get("items"));
    }
    
    @Test
    @DisplayName("Test BooleanAsString schema generation")
    void testBooleanAsStringSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateBooleanAsStringSchema(
            "BooleanType",
            "A boolean value represented as a string");
        
        assertEquals("string", schema.get("type"));
        assertEquals("BooleanType", schema.get("$anchor"));
        assertEquals("A boolean value represented as a string", schema.get("description"));
        assertEquals("^(true|false)$", schema.get("pattern"));
    }
    
    @Test
    @DisplayName("Test NumberAsString schema generation")
    void testNumberAsStringSchemaGeneration() {
        Map<String, Object> schema = ConversionRules.generateNumberAsStringSchema(
            "NumberType",
            "A number value represented as a string",
            10,
            2,
            0.0,
            1000.0);
        
        assertEquals("string", schema.get("type"));
        assertEquals("NumberType", schema.get("$anchor"));
        assertEquals("A number value represented as a string", schema.get("description"));
        assertNotNull(schema.get("pattern"));
    }

    private Map<String, Object> generateTextSchema(String name, String definition, Integer minLength, Integer maxLength, Integer length, String pattern) {
        return ConversionRules.generateTextSchema(name, definition, minLength, maxLength, length, pattern);
    }
} 