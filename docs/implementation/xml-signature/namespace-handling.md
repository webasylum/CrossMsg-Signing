# Signature Exclusion Principle

> **All XML digital signature operations in this project strictly follow the Signature Exclusion Principle: the signature or digest is always calculated over the message content, explicitly excluding the signature itself.**
>
> - For XMLDSig, this is enforced by the *enveloped signature transform* ([W3C XMLDSig](https://www.w3.org/TR/xmldsig-core/)), which ensures the `<Signature>` element is excluded from the digest calculation.
> - See the main [README](../../../../README.md#signature-exclusion-principle-universal-requirement) for details and rationale.

# XML Signature Namespace Handling

## Overview

The XML signature implementation uses a sophisticated namespace handling system to ensure proper signing and verification of ISO 20022 messages. This document details the namespace handling mechanisms and their implementation.

## Namespace Context

### Default Namespace Prefix
- Set to "ds" for XMLDSig operations
- Ensures consistent namespace prefixing
- Prevents namespace conflicts

### ISO 20022 Namespaces
- `urn:iso:std:iso:20022:tech:xsd:head.001.001.02` for AppHdr
- `urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09` for Document
- Handled with empty prefixes to maintain original structure

## Implementation Details

### Signing Context
```java
DOMSignContext signContext = new DOMSignContext(privateKey, appHdr);
signContext.setDefaultNamespacePrefix("ds");
signContext.putNamespacePrefix(Constants.SignatureSpecNS, "ds");
signContext.putNamespacePrefix(ConversionRules.DEFAULT_NAMESPACE, "");
signContext.putNamespacePrefix(ConversionRules.BIZ_MSG_ENVLP_NAMESPACE, "");
```

### Validation Context
```java
DOMValidateContext valContext = new DOMValidateContext(publicKey, signatureElement);
valContext.setDefaultNamespacePrefix("ds");
valContext.putNamespacePrefix(Constants.SignatureSpecNS, "ds");
valContext.putNamespacePrefix(ConversionRules.DEFAULT_NAMESPACE, "");
valContext.putNamespacePrefix(ConversionRules.BIZ_MSG_ENVLP_NAMESPACE, "");
```

## AppHdr Element Location

### Namespace-Aware Search
```java
NodeList appHdrNodes = doc.getElementsByTagNameNS(
    ConversionRules.BIZ_MSG_ENVLP_NAMESPACE,
    "AppHdr"
);
```

### Fallback Mechanism
```java
NodeList fallbackNodes = doc.getElementsByTagName("AppHdr");
```

## Best Practices

1. Always use namespace-aware element location
2. Maintain consistent namespace prefixes
3. Handle namespace fallbacks gracefully
4. Log namespace-related operations
5. Validate namespace context before operations

## Error Handling

### Common Issues
1. Missing namespace declarations
2. Incorrect namespace prefixes
3. Namespace conflicts
4. Invalid namespace URIs

### Resolution Steps
1. Verify namespace declarations
2. Check namespace prefixes
3. Validate namespace context
4. Log namespace operations
5. Handle fallback cases

## Logging

### Namespace Operations
```java
logger.info("Created signing context with namespace prefixes");
logger.info("Signing context namespace mappings:");
logger.info("- Default prefix: ds");
logger.info("- " + Constants.SignatureSpecNS + " -> ds");
logger.info("- " + ConversionRules.DEFAULT_NAMESPACE + " -> (empty)");
logger.info("- " + ConversionRules.BIZ_MSG_ENVLP_NAMESPACE + " -> (empty)");
```

## Future Improvements

1. Enhanced namespace validation
2. Improved namespace conflict resolution
3. Better namespace fallback handling
4. More detailed namespace logging
5. Automated namespace testing 