# Signature Exclusion Principle

> **This strategy strictly follows the Signature Exclusion Principle: the signature or digest is always calculated over the message content, explicitly excluding the signature itself.**
>
> - For Hybrid/Detached Hash, the digest is always computed over the message, and the signature is stored separately (never included in the digest).
> - See the main [README](../../../../README.md#signature-exclusion-principle-universal-requirement) for details and rationale.

# Hybrid / Detached Hash Strategy

## Overview
This strategy computes a deterministic digest (SHA-256 or HMAC-SHA256) over the canonical representation of the message (XML C14N or RFC 8785 JSON), signs only the digest with an asymmetric key, and carries the signed digest in a lightweight header field (e.g., AppHdr.MsgDgst or JSON property).

## Process Flow
1. **Canonicalize:** Produce a stable byte stream using XML C14N or RFC 8785 JSON canonicalization.
2. **Digest:** Compute SHA-256 hash (or HMAC if a shared secret is used) over the canonicalized content.
3. **Sign Digest:** Use a private key to sign the hash (RSA/ECDSA/EdDSA).
4. **Embed:** Place the signed digest string into a dedicated header slot (e.g., AppHdr.MsgDgst).
5. **Transmit:** Deliver as XML or JSON.
6. **Verify:**
   - Extract signed digest from header.
   - Re-canonicalize message.
   - Re-compute digest.
   - Verify digest signature with public key.

## Implementation Notes
- **Canonicalization:** Uses XML C14N 1.1 or RFC 8785 JSON canonicalization for stable input.
- **Digest Algorithm:** SHA-256 (or HMAC-SHA256 for shared secret scenarios).
- **Signature Algorithm:** RSA_SHA256, ECDSA_SHA256, or EdDSA.
- **Signature Embedding:** The signed digest is placed in a dedicated header field, not in the main message body.
- **Signature Exclusion:** The signature/digest is always computed over the message content, never including the signature itself.
- **Libraries:** Java MessageDigest, BouncyCastle, or JCA for signatures.

## References
- [W3C XML C14N 1.1](https://www.w3.org/TR/xml-c14n11/)
- [RFC 8785: JSON Canonicalization Scheme (JCS)](https://datatracker.ietf.org/doc/html/rfc8785)
- [RFC 7515: JSON Web Signature (JWS)](https://datatracker.ietf.org/doc/html/rfc7515)

## See also
- [XMLDSig Strategy](../xml-signature/canonicalization.md)
- [JWS Strategy](../../jws/strategy.md) 