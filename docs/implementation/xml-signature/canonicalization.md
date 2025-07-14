# Signature Exclusion Principle

> **All XML digital signature operations in this project strictly follow the Signature Exclusion Principle: the signature or digest is always calculated over the message content, explicitly excluding the signature itself.**
>
> - For XMLDSig, this is enforced by the *enveloped signature transform* ([W3C XMLDSig](https://www.w3.org/TR/xmldsig-core/)), which ensures the `<Signature>` element is excluded from the digest calculation.
> - See the main [README](../../../../README.md#signature-exclusion-principle-universal-requirement) for details and rationale.

# XML Signature Canonicalization

## Overview
The XML canonicalization process ensures consistent byte representation of XML documents for signature generation and verification. This implementation uses W3C C14N 1.1 with enhancements for ISO 20022 message handling.

## Process Flow

### 1. Element Processing
- Elements are processed in document order
- Each element is processed exactly once
- Early JSON name resolution for consistent mapping
- Support for nested structure preservation
- Enhanced type checking using Node.getNodeType()

### 2. Name Resolution
- XML element names are mapped to JSON equivalents
- Uses ConversionRules.ELEMENT_TRANSFORMATIONS
- Handles nested structures via NESTED_STRUCTURES map
- Preserves semantic meaning across formats
- Supports bidirectional transformation

### 3. Attribute Handling
- Attributes are processed in a single pass
- Consistent XML to JSON mapping
- Preserves attribute order
- Handles namespace declarations
- Maintains semantic structure

### 4. Format Conversion
- XML to JSON conversion preserves structure
- JSON to XML conversion maintains canonicalization compatibility
- Bidirectional mapping ensures consistency
- Validates conversion integrity
- Preserves semantic meaning

## Implementation Details

### Element Processing
```java
private void walkDomCanonicalOrder(Node node, StringBuilder output) {
    // Early JSON name resolution
    String jsonName = ConversionRules.ELEMENT_TRANSFORMATIONS.get(node.getNodeName());
    
    // Process element based on type
    if (isPreservedElement(node)) {
        processPreservedElement(node, output);
    } else if (isNonCanonicalElement(node)) {
        processNonCanonicalElement(node, output);
    } else if (isTransformedElement(node)) {
        processTransformedElement(node, output);
    } else {
        processRegularElement(node, output);
    }
    
    // Process attributes
    processAttributes(node, output);
    
    // Process children
    processChildElements(node, output);
}
```

### Name Resolution
```java
private String resolveJsonName(String xmlName) {
    // Check element transformations
    String jsonName = ConversionRules.ELEMENT_TRANSFORMATIONS.get(xmlName);
    if (jsonName != null) {
        return jsonName;
    }
    
    // Check nested structures
    jsonName = ConversionRules.NESTED_STRUCTURES.get(xmlName);
    if (jsonName != null) {
        return jsonName;
    }
    
    // Default to original name
    return xmlName;
}
```

### Format Conversion
```java
public String convertXmlToJson(String xml) {
    // Parse XML
    Document doc = parseXml(xml);
    
    // Transform element names
    transformElementNames(doc);
    
    // Convert to JSON
    return convertToJson(doc);
}

public String convertJsonToXml(String json) {
    // Parse JSON
    JsonObject jsonObj = parseJson(json);
    
    // Reverse transform names
    reverseTransformElementNames(jsonObj);
    
    // Convert to XML
    return convertToXml(jsonObj);
}
```

## Best Practices

1. **Element Processing**
   - Process each element exactly once
   - Maintain document order
   - Preserve semantic structure
   - Handle all element types
   - Validate element integrity

2. **Name Resolution**
   - Use consistent mapping rules
   - Support bidirectional transformation
   - Preserve semantic meaning
   - Handle nested structures
   - Validate name mappings

3. **Format Conversion**
   - Ensure bidirectional compatibility
   - Preserve semantic structure
   - Validate conversion integrity
   - Handle all element types
   - Maintain canonicalization compatibility

## Change History

### 2024-06-08
- **Change**: Enhanced element processing and format conversion
- **Reason**: Improve canonicalization consistency and format conversion integrity
- **Impact**: Better handling of element names and nested structures
- **Migration**: No migration required, improvements are backward compatible

### 2024-03-19
- **Change**: Initial canonicalization implementation
- **Reason**: Project initialization
- **Impact**: Established baseline canonicalization process
- **Migration**: N/A

## References
- W3C C14N 1.1: https://www.w3.org/TR/xml-c14n11/
- ISO 20022 Message Format: https://www.iso20022.org/
- JSON Canonicalization RFC 8785: https://datatracker.ietf.org/doc/html/rfc8785

## Process Tracking

### Canonicalization Points
1. Initial state before changes
2. After setting document ID
3. Final state after signing/verification

### File-Based Logging
```java
private void saveCanonicalizedContent(String content, String prefix, String operation) {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String filename = String.format("%s/process_%04d_%s_%s_%s.txt", 
        OUTPUT_DIR, currentProcessId, prefix, operation, timestamp);
    // ... save content ...
}
```

## Implementation Details

### Canonicalization Method
```java
private String getCanonicalizedContent(Document doc, String algorithm) {
    Canonicalizer canon = Canonicalizer.getInstance(algorithm);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    canon.canonicalizeSubtree(doc.getDocumentElement(), baos);
    return baos.toString();
}
```

### Reference Creation
```java
Reference ref = fac.newReference(
    referenceUri,
    fac.newDigestMethod(DIGEST_ALGORITHM, null),
    Collections.singletonList(
        fac.newTransform(TRANSFORM_ALGORITHM, (TransformParameterSpec) null)
    ),
    null,
    null
);
```

## Process ID System

### Implementation
```java
private static final AtomicInteger processIdCounter = new AtomicInteger(0);
private final int currentProcessId;

public XmlSignatureStrategy() {
    this.currentProcessId = processIdCounter.incrementAndGet();
}
```

### Usage
- Groups related canonicalization files
- Tracks signing and verification operations
- Maintains operation history
- Enables debugging and analysis

## Logging System

### Canonicalization Logging
```java
logger.info("Initial canonicalized content saved");
logger.info("Canonicalized content after ID setting saved");
logger.info("Final canonicalized content saved");
```

### Error Logging
```java
logger.warning("Failed to canonicalize document: " + e.getMessage());
```

## Best Practices

1. Always use consistent canonicalization algorithm
2. Track canonicalization at key points
3. Maintain detailed logs
4. Handle errors gracefully
5. Validate canonicalized content

## Error Handling

### Common Issues
1. Invalid XML structure
2. Namespace conflicts
3. Missing elements
4. Invalid references

### Resolution Steps
1. Validate XML structure
2. Check namespace context
3. Verify element existence
4. Validate references
5. Log detailed errors

## Future Improvements

1. Enhanced canonicalization validation
2. Improved error reporting
3. Better process tracking
4. More detailed logging
5. Automated testing 