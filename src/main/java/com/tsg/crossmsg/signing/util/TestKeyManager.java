package com.tsg.crossmsg.signing.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Test Key Manager for loading test keys and certificates from PEM files.
 * 
 * This class provides a centralized way to load test keys and certificates
 * for signature strategy testing. Keys are loaded from the test-keys/ directory
 * and cached for performance.
 * 
 * IMPORTANT: These keys are for TESTING PURPOSES ONLY.
 */
public class TestKeyManager {
    
    private static final String TEST_KEYS_DIR = "test-keys";
    private static final String RSA_PRIVATE_KEY_FILE = "rsa_private.pem";
    private static final String RSA_PUBLIC_KEY_FILE = "rsa_public.pem";
    private static final String RSA_CERT_FILE = "rsa_cert.pem";
    private static final String EC_PRIVATE_KEY_FILE = "ec_private.pem";
    private static final String EC_PUBLIC_KEY_FILE = "ec_public.pem";
    private static final String ED25519_PRIVATE_KEY_FILE = "ed25519_private.pem";
    private static final String ED25519_PUBLIC_KEY_FILE = "ed25519_public.pem";
    
    // Cached keys and certificates
    private static KeyPair rsaKeyPair;
    private static KeyPair ecKeyPair;
    private static KeyPair ed25519KeyPair;
    private static X509Certificate rsaCertificate;
    
    /**
     * Get RSA key pair for testing
     */
    public static synchronized KeyPair getRsaKeyPair() throws Exception {
        if (rsaKeyPair == null) {
            rsaKeyPair = loadKeyPair(RSA_PRIVATE_KEY_FILE, RSA_PUBLIC_KEY_FILE, "RSA");
        }
        return rsaKeyPair;
    }
    
    /**
     * Get EC key pair for testing
     */
    public static synchronized KeyPair getEcKeyPair() throws Exception {
        if (ecKeyPair == null) {
            ecKeyPair = loadKeyPair(EC_PRIVATE_KEY_FILE, EC_PUBLIC_KEY_FILE, "EC");
        }
        return ecKeyPair;
    }
    
    /**
     * Get Ed25519 key pair for testing
     */
    public static synchronized KeyPair getEd25519KeyPair() throws Exception {
        if (ed25519KeyPair == null) {
            ed25519KeyPair = loadKeyPair(ED25519_PRIVATE_KEY_FILE, ED25519_PUBLIC_KEY_FILE, "Ed25519");
        }
        return ed25519KeyPair;
    }
    
    /**
     * Get RSA certificate for XMLDSig testing
     */
    public static synchronized X509Certificate getRsaCertificate() throws Exception {
        if (rsaCertificate == null) {
            rsaCertificate = loadCertificate(RSA_CERT_FILE);
        }
        return rsaCertificate;
    }
    
    /**
     * Load a key pair from PEM files
     */
    private static KeyPair loadKeyPair(String privateKeyFile, String publicKeyFile, String algorithm) throws Exception {
        File privateKeyPath = new File(TEST_KEYS_DIR, privateKeyFile);
        File publicKeyPath = new File(TEST_KEYS_DIR, publicKeyFile);
        
        if (!privateKeyPath.exists()) {
            throw new IOException("Private key file not found: " + privateKeyPath.getAbsolutePath() + 
                "\nPlease run ./scripts/generate-test-keys.sh to generate test keys.");
        }
        
        if (!publicKeyPath.exists()) {
            throw new IOException("Public key file not found: " + publicKeyPath.getAbsolutePath() + 
                "\nPlease run ./scripts/generate-test-keys.sh to generate test keys.");
        }
        
        PrivateKey privateKey = loadPrivateKey(privateKeyPath, algorithm);
        PublicKey publicKey = loadPublicKey(publicKeyPath, algorithm);
        
        return new KeyPair(publicKey, privateKey);
    }
    
    /**
     * Load a private key from PEM file
     */
    private static PrivateKey loadPrivateKey(File keyFile, String algorithm) throws Exception {
        try (FileInputStream fis = new FileInputStream(keyFile)) {
            byte[] keyBytes = fis.readAllBytes();
            
            // Remove PEM headers and decode
            String pemContent = new String(keyBytes);
            String privateKeyPEM = pemContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN EC PRIVATE KEY-----", "")
                .replace("-----END EC PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] decodedKey = java.util.Base64.getDecoder().decode(privateKeyPEM);
            
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            
            return keyFactory.generatePrivate(keySpec);
        }
    }
    
    /**
     * Load a public key from PEM file
     */
    private static PublicKey loadPublicKey(File keyFile, String algorithm) throws Exception {
        try (FileInputStream fis = new FileInputStream(keyFile)) {
            byte[] keyBytes = fis.readAllBytes();
            
            // Remove PEM headers and decode
            String pemContent = new String(keyBytes);
            String publicKeyPEM = pemContent
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] decodedKey = java.util.Base64.getDecoder().decode(publicKeyPEM);
            
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            
            return keyFactory.generatePublic(keySpec);
        }
    }
    
    /**
     * Load a certificate from PEM file
     */
    private static X509Certificate loadCertificate(String certFile) throws Exception {
        File certPath = new File(TEST_KEYS_DIR, certFile);
        
        if (!certPath.exists()) {
            throw new IOException("Certificate file not found: " + certPath.getAbsolutePath() + 
                "\nPlease run ./scripts/generate-test-keys.sh to generate test certificates.");
        }
        
        try (FileInputStream fis = new FileInputStream(certPath)) {
            java.security.cert.CertificateFactory certFactory = 
                java.security.cert.CertificateFactory.getInstance("X.509");
            
            return (X509Certificate) certFactory.generateCertificate(fis);
        }
    }
    
    /**
     * Check if test keys are available
     */
    public static boolean areTestKeysAvailable() {
        try {
            getRsaKeyPair();
            getEcKeyPair();
            getEd25519KeyPair();
            getRsaCertificate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get a key pair for a specific algorithm
     */
    public static KeyPair getKeyPairForAlgorithm(String algorithm) throws Exception {
        switch (algorithm.toUpperCase()) {
            case "RSA":
                return getRsaKeyPair();
            case "EC":
            case "ECDSA":
                return getEcKeyPair();
            case "ED25519":
                return getEd25519KeyPair();
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
    }
} 