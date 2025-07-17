package com.tsg.crossmsg.signing.xmlsig;

/**
 * Exception thrown when XML signature operations fail
 */
public class XmlSignatureException extends Exception {
    public XmlSignatureException(String message) {
        super(message);
    }

    public XmlSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
} 