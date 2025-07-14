# Format Conversion Implementation

## Overview
The format conversion implementation ensures consistent and reliable conversion between XML and JSON formats while maintaining signature integrity and semantic structure. This document details the conversion process and its implementation.

## Process Flow

### 1. XML to JSON Conversion
- Parse XML document
- Transform element names using mapping rules
- Preserve semantic structure
- Handle nested elements and attributes
- Maintain canonicalization compatibility

### 2. JSON to XML Conversion
- Parse JSON object
- Reverse transform element names
- Reconstruct XML structure
- Preserve semantic meaning
- Ensure canonicalization compatibility

### 3. Name Transformation
- Use ConversionRules.ELEMENT_TRANSFORMATIONS
- Handle nested structures via NESTED_STRUCTURES
- Support bidirectional mapping
- Preserve semantic meaning
- Validate transformation integrity

### 4. Structure Preservation
- Maintain element hierarchy
- Preserve attribute relationships
- Handle namespace declarations
- Support complex nested structures
- Ensure semantic equivalence

## Implementation Details

### MessageConverter Class
```java
public class MessageConverter {
    // XML to JSON conversion
    public String xmlToJson(String xml) {
        Document doc = parseXml(xml);
        transformElementNames(doc);
        return convertToJson(doc);
    }
    
    // JSON to XML conversion
    public String jsonToXml(String json) {
        JsonObject jsonObj = parseJson(json);
        reverseTransformElementNames(jsonObj);
        return convertToXml(jsonObj);
    }
    
    // Element name transformation
    private void transformElementNames(Document doc) {
        // Apply element transformations
        // Handle nested structures
        // Preserve semantic meaning
    }
    
    // Reverse transformation
    private void reverseTransformElementNames(JsonObject json) {
        // Reverse element transformations
        // Reconstruct nested structures
        // Maintain semantic meaning
    }
}
```

### Conversion Rules
```java
public class ConversionRules {
    // Element name mappings
    public static final Map<String, String> ELEMENT_TRANSFORMATIONS = Map.of(
        "MsgId", "messageId",
        "CreDtTm", "creationDateTime",
        "NbOfTxs", "numberOfTransactions"
    );
    
    // Nested structure mappings
    public static final Map<String, String> NESTED_STRUCTURES = Map.of(
        "GrpHdr", "groupHeader",
        "SttlmInf", "settlementInformation"
    );
}
```

## Best Practices

1. **Name Transformation**
   - Use consistent mapping rules
   - Support bidirectional transformation
   - Preserve semantic meaning
   - Handle nested structures
   - Validate name mappings

2. **Structure Preservation**
   - Maintain element hierarchy
   - Preserve attribute relationships
   - Handle namespace declarations
   - Support complex nested structures
   - Ensure semantic equivalence

3. **Validation**
   - Verify conversion integrity
   - Validate semantic structure
   - Check canonicalization compatibility
   - Ensure signature validity
   - Test bidirectional conversion

## Change History

### 2024-06-08
- **Change**: Enhanced format conversion implementation
- **Reason**: Improve conversion reliability and semantic preservation
- **Impact**: Better handling of element names and nested structures
- **Migration**: No migration required, improvements are backward compatible

### 2024-03-19
- **Change**: Initial format conversion implementation
- **Reason**: Project initialization
- **Impact**: Established baseline conversion process
- **Migration**: N/A

## References
- ISO 20022 Message Format: https://www.iso20022.org/
- JSON Schema: https://json-schema.org/
- XML Schema: https://www.w3.org/XML/Schema
- JSON Canonicalization RFC 8785: https://datatracker.ietf.org/doc/html/rfc8785 