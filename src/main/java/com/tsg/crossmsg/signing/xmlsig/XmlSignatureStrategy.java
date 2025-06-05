package com.tsg.crossmsg.signing.xmlsig;

import com.tsg.crossmsg.signing.config.ConversionRules;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import java.security.Key;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of XML C14N + XMLDSig signature strategy for ISO 20022 messages.
 * This strategy:
 * 1. Uses W3C XML Canonicalization 1.1 to produce a stable byte stream
 * 2. Generates XML Digital Signature (XMLDSig) over those bytes
 * 3. Embeds the Signature element in the ISO 20022 AppHdr (Sgntr slot)
 */
public class XmlSignatureStrategy {
    private static final String C14N_ALGORITHM = CanonicalizationMethod.INCLUSIVE;
    private static final String SIGNATURE_ALGORITHM = SignatureMethod.RSA_SHA256;
    private static final String DIGEST_ALGORITHM = "http://www.w3.org/2001/04/xmlenc#sha256";
    private static final String TRANSFORM_ALGORITHM = Transforms.TRANSFORM_C14N11_OMIT_COMMENTS;

    private final Key signingKey;
    private final X509Certificate signingCert;

    public XmlSignatureStrategy(Key signingKey, X509Certificate signingCert) {
        this.signingKey = signingKey;
        this.signingCert = signingCert;
    }

    /**
     * Signs an ISO 20022 XML message using XML C14N + XMLDSig
     * @param document The XML document to sign
     * @return The signed XML document
     * @throws XmlSignatureException if signing fails
     */
    public Document sign(Document document) throws XmlSignatureException {
        try {
            // Initialize XML Security
            org.apache.xml.security.Init.init();

            // Create signature factory
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Create reference to the document
            Reference ref = fac.newReference(
                "", // URI - empty for the whole document
                fac.newDigestMethod(DIGEST_ALGORITHM, null),
                Collections.singletonList(
                    fac.newTransform(TRANSFORM_ALGORITHM, (TransformParameterSpec) null)
                ),
                null,
                null
            );

            // Create SignedInfo
            SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(
                    C14N_ALGORITHM,
                    (C14NMethodParameterSpec) null
                ),
                fac.newSignatureMethod(SIGNATURE_ALGORITHM, null),
                Collections.singletonList(ref)
            );

            // Create KeyInfo
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(signingCert));
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));

            // Create signature context
            Element appHdr = findAppHdr(document);
            if (appHdr == null) {
                throw new XmlSignatureException("AppHdr element not found in document");
            }

            // Create signature element
            DOMSignContext dsc = new DOMSignContext(signingKey, appHdr);
            dsc.setDefaultNamespacePrefix("ds");

            // Create and sign the signature
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);

            return document;
        } catch (Exception e) {
            throw new XmlSignatureException("Failed to sign XML document", e);
        }
    }

    /**
     * Verifies an XML signature in an ISO 20022 message
     * @param document The signed XML document
     * @param verificationKey The key to use for verification
     * @return true if signature is valid
     * @throws XmlSignatureException if verification fails
     */
    public boolean verify(Document document, Key verificationKey) throws XmlSignatureException {
        try {
            // Find signature element
            NodeList nl = document.getElementsByTagNameNS(
                Constants.SignatureSpecNS,
                "Signature"
            );
            if (nl.getLength() == 0) {
                throw new XmlSignatureException("No signature found in document");
            }

            // Create signature factory
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

            // Create validation context
            DOMValidateContext valContext = new DOMValidateContext(
                verificationKey,
                nl.item(0)
            );

            // Unmarshal and validate signature
            XMLSignature signature = fac.unmarshalXMLSignature(valContext);
            return signature.validate(valContext);
        } catch (Exception e) {
            throw new XmlSignatureException("Failed to verify XML signature", e);
        }
    }

    private Element findAppHdr(Document document) {
        // First try to find AppHdr in the standard location
        NodeList nl = document.getElementsByTagNameNS(
            ConversionRules.BIZ_MSG_ENVLP_NAMESPACE,
            "AppHdr"
        );
        if (nl.getLength() > 0) {
            return (Element) nl.item(0);
        }

        // If not found, try without namespace
        nl = document.getElementsByTagName("AppHdr");
        if (nl.getLength() > 0) {
            return (Element) nl.item(0);
        }

        return null;
    }
} 