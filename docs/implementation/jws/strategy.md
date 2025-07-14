# Signature Exclusion Principle

> **This strategy strictly follows the Signature Exclusion Principle: the signature or digest is always calculated over the message content, explicitly excluding the signature itself.**
>
> - For JWS, canonicalization and signing are performed over the JSON object **without** the signature property, as required by [RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515) and [RFC 8785](https://datatracker.ietf.org/doc/html/rfc8785).
> - See the main [README](../../../../README.md#signature-exclusion-principle-universal-requirement) for details and rationale.

# JWS (JSON Web Signature) Strategy

## Overview
This strategy uses RFC 8785 canonicalization and JWS (RFC 7515) to sign ISO 20022 payment messages in JSON format. The signature is embedded in the AppHdr.Signature property and is preserved across format conversions.

## Process Flow
1. **Prepare JSON:** Ensure consistent field ordering and no extraneous whitespace.
2. **Canonicalize:** Apply RFC 8785 to produce canonical UTF-8 bytes.
3. **Sign:** Use a JWS implementation (e.g., Nimbus JOSE + JWT) to create a JWS (e.g., ES256 or EdDSA) over the canonicalized JSON (excluding the signature property).
4. **Embed:** Insert the JWS compact serialization into `AppHdr.Signature`.
5. **Transmit:** Deliver as JSON or convert to XML, mapping the Signature property to an XML element.
6. **Verify:**
   - Extract JWS from Signature property or XML <Signature> element.
   - Reconstruct JSON via XML→JSON mapping if needed.
   - Canonicalize (RFC 8785).
   - Verify JWS using the sender's public key.

## Implementation Notes
- **Canonicalization:** Uses RFC 8785 for deterministic JSON byte representation.
- **Signature Algorithm:** ES256, EdDSA, or RS256 (as supported by the JWS library).
- **Signature Embedding:** The JWS string is placed in the AppHdr.Signature property.
- **Signature Exclusion:** The signature property is always excluded from the canonicalization and signing process.
- **Libraries:** Nimbus JOSE + JWT (Java), or any RFC 7515-compliant JWS library.

## References
- [RFC 7515: JSON Web Signature (JWS)](https://datatracker.ietf.org/doc/html/rfc7515)
- [RFC 8785: JSON Canonicalization Scheme (JCS)](https://datatracker.ietf.org/doc/html/rfc8785)

## See also
- [XMLDSig Strategy](../xml-signature/canonicalization.md)
- [Hybrid/Detached Hash Strategy](../../hybrid/strategy.md) 