package com.tsg.crossmsg.signing;

import org.junit.jupiter.api.BeforeEach;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class BaseSigningTest {
    protected static final Duration TEST_TIMEOUT = Duration.ofMinutes(5);
    protected KeyPair testKeyPair;
    
    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Initialize test key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        testKeyPair = keyGen.generateKeyPair();
    }
    
    protected String getSampleXmlMessage() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.008.001.02\">" +
               "<FIToFICstmrCdtTrf>" +
               "<GrpHdr><MsgId>MSG-01</MsgId><CreDtTm>2024-03-21T10:30:00</CreDtTm></GrpHdr>" +
               "</FIToFICstmrCdtTrf>" +
               "</Document>";
    }
    
    protected String getSampleJsonMessage() {
        return "{" +
               "\"Document\": {" +
               "  \"FIToFICstmrCdtTrf\": {" +
               "    \"GrpHdr\": {" +
               "      \"MsgId\": \"MSG-01\"," +
               "      \"CreDtTm\": \"2024-03-21T10:30:00\"" +
               "    }" +
               "  }" +
               "}}";
    }
} 