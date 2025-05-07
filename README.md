# Payment Message Signature Strategies Test Suite

## Overview

This repository provides a comprehensive test harness for evaluating signature strategies that ensure end-to-end integrity of ISO 20022 pacs.008 payment messages across format conversions (XML ↔ JSON) in a low-trust, multi-hop payment infrastructure.

## Project Phases

### Phase 1: Classical Cryptography Implementation
- Implementation of current standards (RSA/ECDSA)
- Focus on immediate compatibility and performance
- Core signature strategies:
  1. XML C14N + XMLDSig
  2. JSON Canonicalization (RFC 8785) + JWS
  3. Hybrid/Detached Hash

### Phase 2: Hybrid Quantum-Safe Implementation
- Integration of quantum-safe algorithms alongside classical ones
- Support for hybrid signatures
- Enhanced security with quantum resistance
- Maintained backward compatibility

### Phase 3: Future Quantum-Safe Transition
- Documentation of transition strategy
- Analysis of quantum-safe algorithm candidates
- Implementation roadmap for pure quantum-safe signatures

## Documentation Standards

### Project Structure
```
/docs
  /architecture
    - system-design.md
    - security-model.md
    - quantum-transition.md
  /api
    - signature-strategies.md
    - format-conversion.md
  /implementation
    /phase1
      - classical-implementation.md
      - test-results.md
    /phase2
      - hybrid-implementation.md
      - performance-analysis.md
  /examples
    - signed-messages/
    - test-cases/
```

### Documentation Update Process
1. Each code change must include corresponding documentation updates
2. Documentation is versioned alongside code
3. Automated checks ensure documentation stays in sync
4. Regular documentation reviews as part of code review process

## Implementation Details

### Common Foundations

#### Message Model
- ISO 20022 pacs.008.001.09 payment (`<BizMsgEnvlp>` + `<Document><FIToFICstmrCdtTrf>` payload).
- Full BizMsgEnvlp wrapper must travel with signature container intact.

#### Format Conversion
- **XML → JSON:** deterministic mapping (strip prefixes, Ccy → `{"amt","Ccy"}`, elements → objects/arrays).
- **JSON → XML:** reverse mapping producing identical XML structure for canonicalization.

#### Key Concepts
- **Canonicalization:** process of transforming data to a canonical byte stream for stable hashing or signing.
- **Signature Container:** an element/property in the message header that carries the digital signature or digest.

### Strategy 1: XML C14N + XMLDSig

#### Description
- Use W3C XML Canonicalization 1.1 to produce a stable byte stream of the XML payload.
- Generate an XML Digital Signature (`XMLDSig`) over those bytes.
- Embed the `<Signature>` element in the ISO 20022 AppHdr (`<Sgntr>` slot).

#### Process Flow
1. **Prepare XML**: ensure namespaces and whitespace conform to the spec.
2. **Canonicalize**: apply exclusive C14N 1.1 transform.
3. **Sign**: use Apache Santuario to generate `<Signature>` over the canonical bytes.
4. **Embed**: insert `<Signature>` into `<AppHdr>` of `BizMsgEnvlp`.
5. **Transmit**: deliver as XML or convert to JSON (leaving `<Signature>` untouched).
6. **Verify**:
   - Extract `<Signature>` from AppHdr.
   - If JSON, convert back to XML via deterministic mapping.
   - Canonicalize (C14N 1.1).
   - Use XMLDSig verifier with sender's public key.

#### Implementation Details
- **Libraries:** Apache Santuario (XMLSec), JUnit 5
- **Canonicalization Method URI:** `http://www.w3.org/2006/12/xml-c14n11`
- **Signature Algorithm:** `RSA_SHA256` or `ECDSA_SHA256`

### Strategy 2: JSON Canonicalization (RFC 8785) + JWS

#### Description
- Serialize the full `BizMsgEnvlp` JSON object using RFC 8785 canonicalization (sorted keys, stripped whitespace).
- Generate a JSON Web Signature (JWS) over the canonical bytes.
- Carry the compact JWS string in a top-level `"Signature"` property of the JSON AppHdr.

#### Process Flow
1. **Prepare JSON:** ensure consistent field ordering, no extraneous whitespace.
2. **Canonicalize:** apply RFC 8785 to produce canonical UTF-8 bytes.
3. **Sign:** use Nimbus JOSE + JWT to create a JWS (e.g., `ES256` or `EdDSA`).
4. **Embed:** insert JWS compact serialization into `AppHdr.Signature`.
5. **Transmit:** deliver as JSON or convert to XML, mapping the `Signature` property to an XML element.
6. **Verify**:
   - Extract JWS from `Signature` property or XML `<Signature>` element.
   - Reconstruct JSON via XML→JSON mapping if needed.
   - Canonicalize (RFC 8785).
   - Verify JWS using the sender's public key.

#### Implementation Details
- **Libraries:** Nimbus JOSE + JWT, JUnit 5
- **Canonicalization Spec:** RFC 8785 JSON Canonicalization Scheme
- **JWS Algorithms:** `ES256`, `Ed25519`

### Strategy 3: Hybrid / Detached-Hash

#### Description
- Compute a deterministic digest (SHA-256 or HMAC-SHA256) over the canonical representation of the message (XML C14N or RFC 8785 JSON).
- Sign **only** the digest with an asymmetric key (RSA/ECDSA/EdDSA).
- Carry the signed digest (base64 or hex) in a lightweight header field (e.g., `AppHdr.MsgDgst` or JSON property).

#### Process Flow
1. **Canonicalize**: same as Strategy 1 or 2 to produce a stable byte stream.
2. **Digest**: compute SHA-256 hash (or HMAC if a shared secret is used).
3. **Sign Digest**: use private key to sign the hash.
4. **Embed**: place the signed digest string into a dedicated header slot.
5. **Transmit**: deliver as XML or JSON.
6. **Verify**:
   - Extract signed digest from header.
   - Re-canonicalize message.
   - Re-compute digest.
   - Verify digest signature with public key.

#### Implementation Details
- **Libraries:** Java `MessageDigest`, BouncyCastle or JCA for signatures, JUnit 5
- **Hash Algorithm:** `SHA-256`
- **Signature Algorithm:** `RSA_SHA256` or `ECDSA_SHA256`

## Test Plan

### Directory Structure
```
/src/test/java
  /xmlsig  → XMLDSig tests
  /jws     → JSON Canonical + JWS tests
  /hybrid  → Detached-hash tests
/resources
  sample.xml
  sample.json
README.md
```

### Test Cases
For **each** strategy:
1. **Sign** sample XML
2. **Convert** signed XML → JSON, ensure header signature persists
3. **Verify** signature in JSON
4. **Convert** signed JSON → XML, ensure header signature persists
5. **Verify** signature in XML

### Tools & Commands
- **JUnit 5**: `mvn test` or IDE run configurations
- **CursorAI**: use provided Java classes and sample files to autotest conversion & verification

## Conventions & Terminology
- **Canonical Form**: the exact byte stream inputs to hash or signature
- **Signature Container**: header field or XML element carrying the signature
- **Signer**: originator; **Verifier**: any intermediary or final recipient

## References
- W3C C14N 1.1: https://www.w3.org/TR/xml-c14n11/
- JSON Canonicalization RFC 8785: https://datatracker.ietf.org/doc/html/rfc8785
- JWS (RFC 7515): https://datatracker.ietf.org/doc/html/rfc7515
- ISO 20022 AppHdr spec (head.001.001.02)

## Next Steps
1. Set up automated documentation checks
2. Implement Phase 1 signature strategies
3. Document implementation details
4. Begin Phase 2 hybrid implementation planning
5. Create comprehensive quantum transition documentation

