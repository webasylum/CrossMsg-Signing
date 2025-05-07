package com.tsg.crossmsg.signing.model;

/**
 * Represents the available signature strategies for ISO 20022 messages.
 */
public enum SignatureStrategy {
    XML_C14N_XMLDSIG,
    JSON_CANONICAL_JWS,
    HYBRID_DETACHED_HASH
} 