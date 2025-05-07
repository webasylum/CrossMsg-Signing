package com.tsg.crossmsg.signing;

import com.tsg.crossmsg.signing.model.MessageFormat;
import com.tsg.crossmsg.signing.model.SignatureStrategy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test class to verify project setup and dependencies.
 */
class ProjectSetupTest {

    @Test
    void testMessageFormatEnum() {
        assertNotNull(MessageFormat.XML);
        assertNotNull(MessageFormat.JSON);
        assertEquals(2, MessageFormat.values().length);
    }

    @Test
    void testSignatureStrategyEnum() {
        assertNotNull(SignatureStrategy.XML_C14N_XMLDSIG);
        assertNotNull(SignatureStrategy.JSON_CANONICAL_JWS);
        assertNotNull(SignatureStrategy.HYBRID_DETACHED_HASH);
        assertEquals(3, SignatureStrategy.values().length);
    }
} 