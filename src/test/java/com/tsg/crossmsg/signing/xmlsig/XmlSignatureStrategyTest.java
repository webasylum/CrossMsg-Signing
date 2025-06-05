package com.tsg.crossmsg.signing.xmlsig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import static org.junit.jupiter.api.Assertions.*;

class XmlSignatureStrategyTest {
    private static final String TEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09\">\n" +
        "  <FIToFICstmrCdtTrf>\n" +
        "    <GrpHdr>\n" +
        "      <MsgId>TEST123456789</MsgId>\n" +
        "      <CreDtTm>2024-03-21T10:00:00</CreDtTm>\n" +
        "      <NbOfTxs>1</NbOfTxs>\n" +
        "    </GrpHdr>\n" +
        "  </FIToFICstmrCdtTrf>\n" +
        "</Document>";

    private KeyPair keyPair;
    private X509Certificate cert;
    private XmlSignatureStrategy strategy;

    @BeforeEach
    void setUp() throws Exception {
        // Generate test key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        // Create self-signed certificate (for testing only)
        cert = createSelfSignedCertificate(keyPair);

        strategy = new XmlSignatureStrategy(keyPair.getPrivate(), cert);
    }

    @Test
    @DisplayName("Test XML signature generation and verification")
    void testXmlSignature() throws Exception {
        // Parse test XML
        Document document = parseXml(TEST_XML);

        // Sign the document
        Document signedDoc = strategy.sign(document);

        // Verify the signature
        boolean isValid = strategy.verify(signedDoc, keyPair.getPublic());
        assertTrue(isValid, "Signature verification should succeed");
    }

    @Test
    @DisplayName("Test signature verification with wrong key")
    void testSignatureVerificationWithWrongKey() throws Exception {
        // Generate another key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair wrongKeyPair = keyGen.generateKeyPair();

        // Parse and sign test XML
        Document document = parseXml(TEST_XML);
        Document signedDoc = strategy.sign(document);

        // Try to verify with wrong key
        boolean isValid = strategy.verify(signedDoc, wrongKeyPair.getPublic());
        assertFalse(isValid, "Signature verification should fail with wrong key");
    }

    @Test
    @DisplayName("Test signature verification of modified document")
    void testSignatureVerificationOfModifiedDocument() throws Exception {
        // Parse and sign test XML
        Document document = parseXml(TEST_XML);
        Document signedDoc = strategy.sign(document);

        // Modify the signed document
        signedDoc.getElementsByTagName("MsgId").item(0).setTextContent("MODIFIED");

        // Try to verify modified document
        boolean isValid = strategy.verify(signedDoc, keyPair.getPublic());
        assertFalse(isValid, "Signature verification should fail for modified document");
    }

    private Document parseXml(String xml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    private X509Certificate createSelfSignedCertificate(KeyPair keyPair) throws Exception {
        // Create certificate generator
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
        
        // Set certificate fields
        X500Principal issuerName = new X500Principal("CN=Test Certificate");
        X500Principal subjectName = new X500Principal("CN=Test Certificate");
        
        certGen.setIssuerDN(issuerName);
        certGen.setSubjectDN(subjectName);
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L));
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256withRSA");
        
        // Generate certificate
        return certGen.generate(keyPair.getPrivate());
    }
} 