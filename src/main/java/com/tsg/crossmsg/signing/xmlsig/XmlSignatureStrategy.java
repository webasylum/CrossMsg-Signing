package com.tsg.crossmsg.signing.xmlsig;

import com.tsg.crossmsg.signing.config.ConversionRules;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.xml.crypto.dsig.Transform;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.ArrayList;

/**
 * Implementation of XML signature strategy using XML C14N and XMLDSig.
 * This strategy signs ISO 20022 XML messages using W3C XML Canonicalization 1.1
 * and embeds the signature in the AppHdr element.
 */
public class XmlSignatureStrategy {
    private static final Logger logger = Logger.getLogger(XmlSignatureStrategy.class.getName());

    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    // XML Signature constants
    private static final String C14N_ALGORITHM = CanonicalizationMethod.EXCLUSIVE;
    private static final String SIGNATURE_ALGORITHM = SignatureMethod.RSA_SHA256;
    private static final String DIGEST_ALGORITHM = DigestMethod.SHA256;
    private static final String TRANSFORM_ALGORITHM = Transforms.TRANSFORM_C14N11_OMIT_COMMENTS;

    // Output directory for canonicalized values
    private static final String OUTPUT_DIR = "/app/build/canonicalized";

    // Process ID counter for grouping related canonicalization files
    private static final AtomicInteger processIdCounter = new AtomicInteger(0);
    private final int currentProcessId;

    static {
        try {
            Init.init();
            logger.info("XML Security initialized successfully");
            // Create output directory if it doesn't exist
            Files.createDirectories(Paths.get(OUTPUT_DIR));
        } catch (Exception e) {
            logger.severe("Failed to initialize XML Security: " + e.getMessage());
            throw new RuntimeException("Failed to initialize XML Security", e);
        }
    }

    /**
     * Creates a new XmlSignatureStrategy with the given private key and
     * certificate.
     * 
     * @param privateKey  The private key to use for signing
     * @param certificate The certificate containing the public key
     */
    public XmlSignatureStrategy(PrivateKey privateKey, X509Certificate certificate) {
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.currentProcessId = processIdCounter.incrementAndGet();
        logger.info("Created new XmlSignatureStrategy instance with process ID: " + currentProcessId);
    }

    /**
     * Creates a new XmlSignatureStrategy without a default key pair.
     * Use this constructor when you want to provide the key pair for each
     * operation.
     */
    public XmlSignatureStrategy() {
        this.privateKey = null;
        this.certificate = null;
        this.currentProcessId = processIdCounter.incrementAndGet();
        logger.info("Created new XmlSignatureStrategy instance with process ID: " + currentProcessId);
    }

    /**
     * Signs an ISO 20022 XML message using the instance's private key and
     * certificate.
     * 
     * @param xml The XML document to sign
     * @return The signed XML document
     * @throws XmlSignatureException if signing fails or if no private
     *                               key/certificate was provided
     */
    public Document sign(Document xml) throws XmlSignatureException {
        if (privateKey == null || certificate == null) {
            throw new XmlSignatureException(
                    "No private key or certificate provided. Use the constructor with parameters or the overloaded sign method.");
        }
        return sign(xml, privateKey, certificate);
    }

    /**
     * Saves canonicalized content to a file
     */
    private void saveCanonicalizedContent(String content, String prefix, String operation) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/process_%04d_%s_%s_%s.txt",
                    OUTPUT_DIR, currentProcessId, prefix, operation, timestamp);

            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("Process ID: " + currentProcessId);
                writer.println("Timestamp: " + timestamp);
                writer.println("Operation: " + operation);
                writer.println("Strategy: " + getStrategyLabel());
                writer.println("Canonicalized Content:");
                writer.println("---------------------");
                writer.println(content);
            }

            logger.info("Saved canonicalized content to: " + filename);
        } catch (IOException e) {
            logger.warning("Failed to save canonicalized content: " + e.getMessage());
        }
    }

    /**
     * Gets the canonicalized form of a document
     */
    private String getCanonicalizedContent(Document doc, String algorithm) {
        try {
            Canonicalizer canon = Canonicalizer.getInstance(algorithm);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            canon.canonicalizeSubtree(doc.getDocumentElement(), baos);
            return baos.toString();
        } catch (Exception e) {
            logger.warning("Failed to canonicalize document: " + e.getMessage());
            return "Failed to canonicalize: " + e.getMessage();
        }
    }

    /**
     * Recursively extracts ALL key-value pairs from a DOM subtree in canonical
     * order.
     * This method ensures that EVERY element in the ISO 20022 message is captured,
     * including all attributes, namespaces, and nested elements.
     * 
     * @param element The DOM element to process
     * @param path    The current XPath-like path to the element
     * @param sb      StringBuilder to collect the output
     */
    private void walkDomCanonicalOrder(Element element, String path, StringBuilder sb) {
        String elementName = element.getNodeName();
        String text = element.getTextContent().trim();

        // Get JSON name if transformation exists
        String jsonName = ConversionRules.ELEMENT_TRANSFORMATIONS.get(elementName);
        String nestedName = ConversionRules.NESTED_STRUCTURES.get(elementName);

        // Process element based on its type (only once)
        if (ConversionRules.PRESERVED_ELEMENTS.contains(elementName)) {
            sb.append(path).append(" = <preserved>\n");
        } else if (ConversionRules.NON_CANONICAL_ELEMENTS.contains(elementName)) {
            if (!text.isEmpty()) {
                sb.append(path).append(" = ").append(text).append(" <non-canonical>\n");
            }
        } else if (jsonName != null) {
            if (!text.isEmpty()) {
                sb.append(path).append(" = ").append(text).append(" <transformed:").append(jsonName).append(">\n");
            }
        } else if (nestedName != null) {
            sb.append(path).append(" = <nested:").append(nestedName).append(">\n");
        } else if (!element.hasAttributes() && !element.hasChildNodes() && (text == null || text.isEmpty())) {
            sb.append(path).append(" = <empty>\n");
        } else if (!text.isEmpty()) {
            sb.append(path).append(" = ").append(text).append("\n");
        }

        // Process attributes with JSON name mapping
        if (element.hasAttributes()) {
            for (int i = 0; i < element.getAttributes().getLength(); i++) {
                String attrName = element.getAttributes().item(i).getNodeName();
                String attrValue = element.getAttributes().item(i).getNodeValue();
                String jsonAttrName = ConversionRules.ELEMENT_TRANSFORMATIONS.get(attrName);

                if (jsonAttrName != null) {
                    sb.append(path).append("@").append(jsonAttrName).append(" = ").append(attrValue).append("\n");
                } else {
                    sb.append(path).append("@").append(attrName).append(" = ").append(attrValue).append("\n");
                }
            }
        }

        // Process child elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String childPath = path + "/" + child.getNodeName();
                walkDomCanonicalOrder((Element) child, childPath, sb);
            }
        }
    }

    /**
     * Saves the canonicalized key-value pairs to a file.
     * This method ensures that ALL elements from the ISO 20022 message are captured
     * in a consistent, ordered format.
     */
    private void saveCanonicalizedKeyValuePairs(Document doc, String prefix, String operation) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/process_%04d_%s_%s_keyvalues_%s.txt",
                    OUTPUT_DIR, currentProcessId, prefix, operation, timestamp);

            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("Process ID: " + currentProcessId);
                writer.println("Timestamp: " + timestamp);
                writer.println("Operation: " + operation);
                writer.println("Strategy: " + getStrategyLabel());

                StringBuilder sb = new StringBuilder();
                walkDomCanonicalOrder(doc.getDocumentElement(), "/" + doc.getDocumentElement().getNodeName(), sb);
                String[] pairs = sb.toString().split("\n");

                // Write header information
                writer.println("\nMessage Structure Summary:");
                writer.println("------------------------");
                writer.println("Total key-value pairs: " + pairs.length);
                writer.println("Document root: " + doc.getDocumentElement().getNodeName());
                writer.println("Namespaces: " + doc.getDocumentElement().getAttribute("xmlns"));

                // Write categorized key-value pairs
                writer.println("\nDetailed Key-Value Pairs:");
                writer.println("------------------------");

                // Group pairs by category
                Map<String, List<String>> categorizedPairs = new LinkedHashMap<>();
                categorizedPairs.put("Message Header", new ArrayList<>());
                categorizedPairs.put("Payment Information", new ArrayList<>());
                categorizedPairs.put("Debtor Information", new ArrayList<>());
                categorizedPairs.put("Creditor Information", new ArrayList<>());
                categorizedPairs.put("Settlement Information", new ArrayList<>());
                categorizedPairs.put("Other Elements", new ArrayList<>());

                for (String pair : pairs) {
                    if (pair.contains("AppHdr") || pair.contains("MsgId") || pair.contains("CreDt")) {
                        categorizedPairs.get("Message Header").add(pair);
                    } else if (pair.contains("IntrBkSttlmAmt") || pair.contains("PmtId") ||
                            pair.contains("UETR") || pair.contains("EndToEndId")) {
                        categorizedPairs.get("Payment Information").add(pair);
                    } else if (pair.contains("Dbtr")) {
                        categorizedPairs.get("Debtor Information").add(pair);
                    } else if (pair.contains("Cdtr")) {
                        categorizedPairs.get("Creditor Information").add(pair);
                    } else if (pair.contains("Sttlm") || pair.contains("ClrSys")) {
                        categorizedPairs.get("Settlement Information").add(pair);
                    } else {
                        categorizedPairs.get("Other Elements").add(pair);
                    }
                }

                // Write categorized pairs
                for (Map.Entry<String, List<String>> category : categorizedPairs.entrySet()) {
                    if (!category.getValue().isEmpty()) {
                        writer.println("\n" + category.getKey() + ":");
                        writer.println("-".repeat(category.getKey().length() + 1));
                        for (String pair : category.getValue()) {
                            writer.println(pair);
                        }
                    }
                }

                // Write validation summary
                writer.println("\nValidation Summary:");
                writer.println("------------------");
                try {
                    validateRequiredElements(doc);
                    writer.println("✓ All required elements present");
                } catch (XmlSignatureException e) {
                    writer.println("✗ Missing required elements: " + e.getMessage());
                }

                try {
                    validatePaymentInformation(doc);
                    writer.println("✓ Payment information complete");
                } catch (XmlSignatureException e) {
                    writer.println("✗ Payment information incomplete: " + e.getMessage());
                }

                // Write transformation summary
                writer.println("\nTransformation Summary:");
                writer.println("---------------------");
                int transformedCount = 0;
                int preservedCount = 0;
                int nonCanonicalCount = 0;

                for (String pair : pairs) {
                    if (pair.contains("<transformed:"))
                        transformedCount++;
                    if (pair.contains("<preserved>"))
                        preservedCount++;
                    if (pair.contains("<non-canonical>"))
                        nonCanonicalCount++;
                }

                writer.println("Transformed elements: " + transformedCount);
                writer.println("Preserved elements: " + preservedCount);
                writer.println("Non-canonical elements: " + nonCanonicalCount);

                writer.println("\nEnd of Audit Report");
            }

            logger.info("Saved detailed key-value pairs to: " + filename);
        } catch (IOException e) {
            logger.warning("Failed to save key-value pairs: " + e.getMessage());
        }
    }

    // Helper to write canonicalized XML to a file
    private void saveCanonicalizedXml(String canonicalized, String prefix, String operation) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/process_%04d_%s_%s_canonicalized_%s.xml",
                    OUTPUT_DIR, currentProcessId, prefix, operation, timestamp);
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("Process ID: " + currentProcessId);
                writer.println("Timestamp: " + timestamp);
                writer.println("Operation: " + operation);
                writer.println("Strategy: " + getStrategyLabel());
                writer.println("Canonicalized XML:");
                writer.println("----------------");
                writer.print(canonicalized);
            }
            logger.info("Saved canonicalized XML to: " + filename);
        } catch (IOException e) {
            logger.warning("Failed to save canonicalized XML: " + e.getMessage());
        }
    }

    // Helper to write digest of canonicalized XML to a file (base64)
    private void saveCanonicalizedDigest(String canonicalized, String prefix, String operation) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(canonicalized.getBytes());
            String digestBase64 = Base64.getEncoder().encodeToString(digest);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s/process_%04d_%s_%s_digest_%s.txt",
                    OUTPUT_DIR, currentProcessId, prefix, operation, timestamp);
            try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
                writer.println("Process ID: " + currentProcessId);
                writer.println("Timestamp: " + timestamp);
                writer.println("Operation: " + operation);
                writer.println("Strategy: " + getStrategyLabel());
                writer.println("SHA-256 Digest (base64):");
                writer.println("-----------------------");
                writer.println(digestBase64);
            }
            logger.info("Saved canonicalized digest to: " + filename);
        } catch (Exception e) {
            logger.warning("Failed to save canonicalized digest: " + e.getMessage());
        }
    }

    // Helper to parse canonicalized XML string back to Document
    private Document parseCanonicalizedXml(String canonicalizedXml) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new java.io.ByteArrayInputStream(canonicalizedXml.getBytes()));
    }

    public Document sign(Document xml, PrivateKey privateKey, X509Certificate certificate)
            throws XmlSignatureException {
        try {
            logger.info("Starting XML signature process");

            // Validate required elements and payment information
            validateRequiredElements(xml);
            validatePaymentInformation(xml);
            logger.info("Document validation passed");

            // Get and save initial canonicalized form
            String initialCanonicalized = getCanonicalizedContent(xml, TRANSFORM_ALGORITHM);
            saveCanonicalizedContent(initialCanonicalized, "signing", "initial");
            logger.info("Initial canonicalized content saved");

            // Save canonicalized XML and digest before signing
            saveCanonicalizedXml(initialCanonicalized, "signing", "before_signing");
            saveCanonicalizedDigest(initialCanonicalized, "signing", "before_signing");

            // Save original key-value pairs
            saveCanonicalizedKeyValuePairs(xml, "signing", "before_signing");
            logger.info("Key-value pairs before signing saved");

            // Find AppHdr element
            Element appHdr = findAppHdr(xml);
            if (appHdr == null) {
                logger.severe("AppHdr element not found in document");
                throw new XmlSignatureException("AppHdr element not found");
            }
            logger.info("Found AppHdr element: " + appHdr.getNodeName());

            // Set ID on document element for reference
            Element documentElement = xml.getDocumentElement();
            if (!documentElement.hasAttribute("Id")) {
                // Use the message type as the ID
                String messageType = documentElement.getAttribute("xmlns");
                if (messageType != null && !messageType.isEmpty()) {
                    // Extract message type from namespace
                    String[] parts = messageType.split(":");
                    if (parts.length > 0) {
                        messageType = parts[parts.length - 1];
                    }
                }
                documentElement.setAttribute("Id", messageType);
                logger.info("Set Id attribute on document element to: " + messageType);
            }
            // Register the Id attribute as type ID for XMLDSig
            documentElement.setIdAttribute("Id", true);
            logger.info("Document element ID: " + documentElement.getAttribute("Id"));
            logger.info("Document element namespace: " + documentElement.getAttribute("xmlns"));

            // Get and save canonicalized form after ID setting
            String afterIdCanonicalized = getCanonicalizedContent(xml, TRANSFORM_ALGORITHM);
            saveCanonicalizedContent(afterIdCanonicalized, "signing", "after_id_set");
            logger.info("Canonicalized content after ID setting saved");
            // Save canonicalized XML and digest after ID set
            saveCanonicalizedXml(afterIdCanonicalized, "signing", "after_id_set");
            saveCanonicalizedDigest(afterIdCanonicalized, "signing", "after_id_set");
            // Save after ID set
            Document canonicalizedDocAfterId = parseCanonicalizedXml(afterIdCanonicalized);
            saveCanonicalizedKeyValuePairs(canonicalizedDocAfterId, "signing", "after_id_set");
            saveCanonicalizedKeyValuePairs(xml, "signing", "after_id_set");
            logger.info("Key-value pairs after ID set saved");

            // Create signature factory
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            logger.info("Created XMLSignatureFactory");

            // Create reference to document
            String referenceUri = "#" + documentElement.getAttribute("Id");
            logger.info("Creating reference with URI: " + referenceUri);

            Reference ref = fac.newReference(
                    referenceUri,
                    fac.newDigestMethod(DIGEST_ALGORITHM, null),
                    java.util.Arrays.asList(
                            fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                            fac.newTransform(TRANSFORM_ALGORITHM, (TransformParameterSpec) null)),
                    null,
                    null);
            logger.info("Created reference to document with transform: " + TRANSFORM_ALGORITHM);

            // Create signature
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(
                            C14N_ALGORITHM,
                            (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SIGNATURE_ALGORITHM, null),
                    Collections.singletonList(ref));
            logger.info("Created SignedInfo with algorithm: " + SIGNATURE_ALGORITHM);
            logger.info("SignedInfo canonicalization method: " + C14N_ALGORITHM);

            // Create KeyInfo
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(certificate));
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509Data));
            logger.info("Created KeyInfo with certificate");
            logger.info("Certificate subject: " + certificate.getSubjectX500Principal());

            // Create signature but do NOT insert it yet
            javax.xml.crypto.dsig.XMLSignature signature = fac.newXMLSignature(si, ki);
            logger.info("Created XMLSignature");

            // Create signing context with proper namespace context
            // Insert the signature as the last child of AppHdr (not before
            // canonicalization/digest)
            DOMSignContext signContext = new DOMSignContext(privateKey, appHdr);
            signContext.setDefaultNamespacePrefix("ds");
            signContext.putNamespacePrefix(Constants.SignatureSpecNS, "ds");
            signContext.putNamespacePrefix(ConversionRules.DEFAULT_NAMESPACE, "");
            signContext.putNamespacePrefix(ConversionRules.BIZ_MSG_ENVLP_NAMESPACE, "");
            logger.info("Created signing context with namespace prefixes");
            logger.info("Signing context namespace mappings:");
            logger.info("- Default prefix: ds");
            logger.info("- " + Constants.SignatureSpecNS + " -> ds");
            logger.info("- " + ConversionRules.DEFAULT_NAMESPACE + " -> (empty)");
            logger.info("- " + ConversionRules.BIZ_MSG_ENVLP_NAMESPACE + " -> (empty)");

            // Remove any existing Signature element from AppHdr before signing (defensive)
            NodeList existingSigs = appHdr.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
            for (int i = 0; i < existingSigs.getLength(); i++) {
                appHdr.removeChild(existingSigs.item(i));
            }

            // Now sign (this will insert the <Signature> element at the correct place)
            signature.sign(signContext);
            logger.info("Document signed successfully");
            // Save canonicalized XML and digest after signing
            String afterSigningCanonicalized = getCanonicalizedContent(xml, TRANSFORM_ALGORITHM);
            saveCanonicalizedXml(afterSigningCanonicalized, "signing", "after_signing");
            saveCanonicalizedDigest(afterSigningCanonicalized, "signing", "after_signing");
            // Save after signing
            Document canonicalizedDocAfterSigning = parseCanonicalizedXml(afterSigningCanonicalized);
            saveCanonicalizedKeyValuePairs(canonicalizedDocAfterSigning, "signing", "after_signing");
            saveCanonicalizedKeyValuePairs(xml, "signing", "after_signing");

            // Get and save final canonicalized form (now includes <Signature>)
            String finalCanonicalized = getCanonicalizedContent(xml, TRANSFORM_ALGORITHM);
            saveCanonicalizedContent(finalCanonicalized, "signing", "final");
            logger.info("Final canonicalized content saved");
            // Save canonicalized XML and digest at final step
            saveCanonicalizedXml(finalCanonicalized, "signing", "final");
            saveCanonicalizedDigest(finalCanonicalized, "signing", "final");
            // Save final state
            Document canonicalizedDocFinal = parseCanonicalizedXml(finalCanonicalized);
            saveCanonicalizedKeyValuePairs(canonicalizedDocFinal, "signing", "final");
            saveCanonicalizedKeyValuePairs(xml, "signing", "final");

            return xml;
        } catch (Exception e) {
            logger.severe("Failed to sign document: " + e.getMessage());
            throw new XmlSignatureException("Failed to sign document", e);
        }
    }

    public boolean verify(Document xml, PublicKey publicKey) throws XmlSignatureException {
        try {
            logger.info("Starting signature verification");

            // Get and save initial canonicalized form
            String initialCanonicalized = getCanonicalizedContent(xml, TRANSFORM_ALGORITHM);
            saveCanonicalizedContent(initialCanonicalized, "verification", "initial");
            logger.info("Initial canonicalized content saved");
            // Save canonicalized XML and digest before validation
            saveCanonicalizedXml(initialCanonicalized, "verification", "before_validation");
            saveCanonicalizedDigest(initialCanonicalized, "verification", "before_validation");
            // Save original key-value pairs
            saveCanonicalizedKeyValuePairs(xml, "verification", "before_validation");
            logger.info("Key-value pairs before validation saved");

            // Find signature element
            NodeList signatureNodes = xml.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
            if (signatureNodes.getLength() == 0) {
                logger.severe("No signature element found in document");
                return false;
            }
            Element signatureElement = (Element) signatureNodes.item(0);
            logger.info("Found signature element: " + signatureElement.getNodeName());

            // Create validation context with proper namespace context
            DOMValidateContext valContext = new DOMValidateContext(publicKey, signatureElement);
            valContext.setDefaultNamespacePrefix("ds");
            valContext.putNamespacePrefix(Constants.SignatureSpecNS, "ds");
            valContext.putNamespacePrefix(ConversionRules.DEFAULT_NAMESPACE, "");
            valContext.putNamespacePrefix(ConversionRules.BIZ_MSG_ENVLP_NAMESPACE, "");
            logger.info("Created validation context with namespace prefixes");
            logger.info("Validation context namespace mappings:");
            logger.info("- Default prefix: ds");
            logger.info("- " + Constants.SignatureSpecNS + " -> ds");
            logger.info("- " + ConversionRules.DEFAULT_NAMESPACE + " -> (empty)");
            logger.info("- " + ConversionRules.BIZ_MSG_ENVLP_NAMESPACE + " -> (empty)");

            // Get the reference URI from the signature
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            javax.xml.crypto.dsig.XMLSignature signature = fac.unmarshalXMLSignature(valContext);
            String referenceUri = signature.getSignedInfo().getReferences().get(0).getURI();
            logger.info("Found reference URI: " + referenceUri);

            // Log signature details
            logger.info("Signature details:");
            logger.info("- Canonicalization method: "
                    + signature.getSignedInfo().getCanonicalizationMethod().getAlgorithm());
            logger.info("- Signature method: " + signature.getSignedInfo().getSignatureMethod().getAlgorithm());
            logger.info("- Number of references: " + signature.getSignedInfo().getReferences().size());

            // Ensure document element has the correct ID
            Element documentElement = xml.getDocumentElement();
            String expectedId = referenceUri.startsWith("#") ? referenceUri.substring(1) : referenceUri;
            logger.info("Document element details:");
            logger.info("- Current ID: " + documentElement.getAttribute("Id"));
            logger.info("- Expected ID: " + expectedId);
            logger.info("- Namespace: " + documentElement.getAttribute("xmlns"));

            // Set the ID attribute if it doesn't exist or is different
            if (!documentElement.hasAttribute("Id") || !expectedId.equals(documentElement.getAttribute("Id"))) {
                logger.info("Setting document ID to match reference: " + expectedId);
                documentElement.setAttribute("Id", expectedId);
            }

            // Get and save canonicalized form after ID setting
            String afterIdCanonicalized = getCanonicalizedContent(xml, TRANSFORM_ALGORITHM);
            saveCanonicalizedContent(afterIdCanonicalized, "verification", "after_id_set");
            logger.info("Canonicalized content after ID setting saved");
            // Save canonicalized XML and digest after ID set
            saveCanonicalizedXml(afterIdCanonicalized, "verification", "after_id_set");
            saveCanonicalizedDigest(afterIdCanonicalized, "verification", "after_id_set");
            // Save after ID set
            Document canonicalizedDocAfterId = parseCanonicalizedXml(afterIdCanonicalized);
            saveCanonicalizedKeyValuePairs(canonicalizedDocAfterId, "verification", "after_id_set");
            saveCanonicalizedKeyValuePairs(xml, "verification", "after_id_set");
            logger.info("Key-value pairs after ID set (verification) saved");

            // Register the ID attribute for validation
            documentElement.setIdAttribute("Id", true);
            logger.info("Registered ID attribute for validation");

            // Unmarshal and validate the signature
            signature = fac.unmarshalXMLSignature(valContext);
            logger.info("Unmarshalled signature");

            boolean isValid = signature.validate(valContext);
            logger.info("Signature validation result: " + isValid);

            if (!isValid) {
                logger.warning("Signature validation failed");
                if (signature.getSignedInfo().getReferences().size() > 0) {
                    Reference ref = (Reference) signature.getSignedInfo().getReferences().get(0);
                    boolean refValid = ref.validate(valContext);
                    logger.warning("Reference validation status: " + refValid);
                    if (!refValid) {
                        logger.warning("Reference URI: " + ref.getURI());
                        logger.warning("Reference digest method: " + ref.getDigestMethod().getAlgorithm());
                        logger.warning("Reference transforms: " + ref.getTransforms());
                        // Log the actual transform algorithm
                        if (ref.getTransforms() != null && !ref.getTransforms().isEmpty()) {
                            Transform t = (Transform) ref.getTransforms().get(0);
                            logger.warning("Transform algorithm: " + t.getAlgorithm());
                        }
                    }
                }
            }

            // Get and save final canonicalized form
            String finalCanonicalized = getCanonicalizedContent(xml, TRANSFORM_ALGORITHM);
            saveCanonicalizedContent(finalCanonicalized, "verification", "final");
            logger.info("Final canonicalized content saved");
            // Save canonicalized XML and digest at final step
            saveCanonicalizedXml(finalCanonicalized, "verification", "final");
            saveCanonicalizedDigest(finalCanonicalized, "verification", "final");
            // Save final state
            Document canonicalizedDocFinal = parseCanonicalizedXml(finalCanonicalized);
            saveCanonicalizedKeyValuePairs(canonicalizedDocFinal, "verification", "final");
            saveCanonicalizedKeyValuePairs(xml, "verification", "after_validation");
            logger.info("Key-value pairs after validation saved");

            return isValid;
        } catch (Exception e) {
            logger.severe("Failed to verify signature: " + e.getMessage());
            throw new XmlSignatureException("Failed to verify signature", e);
        }
    }

    /**
     * Finds the AppHdr element in the document.
     * First tries to find it with namespace, then without.
     */
    private Element findAppHdr(Document doc) {
        // First try with namespace
        NodeList appHdrNodes = doc.getElementsByTagNameNS(
                ConversionRules.BIZ_MSG_ENVLP_NAMESPACE,
                "AppHdr");

        if (appHdrNodes.getLength() > 0) {
            logger.info("Found AppHdr with namespace: " + ConversionRules.BIZ_MSG_ENVLP_NAMESPACE);
            return (Element) appHdrNodes.item(0);
        }

        // Fallback to finding without namespace
        NodeList fallbackNodes = doc.getElementsByTagName("AppHdr");
        if (fallbackNodes.getLength() > 0) {
            logger.info("Found AppHdr without namespace");
            return (Element) fallbackNodes.item(0);
        }

        logger.warning("AppHdr element not found in document");
        return null;
    }

    public String getStrategyLabel() {
        return "XMLDSig (C14N 1.1 + XMLDSig)";
    }

    /**
     * Validates that all required elements are present in the document.
     * 
     * @param doc The document to validate
     * @throws XmlSignatureException if required elements are missing
     */
    private void validateRequiredElements(Document doc) throws XmlSignatureException {
        StringBuilder missingElements = new StringBuilder();

        for (String requiredElement : ConversionRules.REQUIRED_ELEMENTS) {
            NodeList nodes = doc.getElementsByTagName(requiredElement);
            if (nodes.getLength() == 0) {
                if (missingElements.length() > 0) {
                    missingElements.append(", ");
                }
                missingElements.append(requiredElement);
            }
        }

        if (missingElements.length() > 0) {
            throw new XmlSignatureException("Missing required elements: " + missingElements.toString());
        }
    }

    /**
     * Validates critical payment information in the document.
     * 
     * @param doc The document to validate
     * @throws XmlSignatureException if critical information is missing or invalid
     */
    private void validatePaymentInformation(Document doc) throws XmlSignatureException {
        // Validate amount
        NodeList amountNodes = doc.getElementsByTagName("IntrBkSttlmAmt");
        if (amountNodes.getLength() == 0) {
            throw new XmlSignatureException("Missing payment amount");
        }

        // Validate currency
        Element amountElement = (Element) amountNodes.item(0);
        if (!amountElement.hasAttribute("Ccy")) {
            throw new XmlSignatureException("Missing currency in payment amount");
        }

        // Validate debtor and creditor information
        NodeList debtorNodes = doc.getElementsByTagName("Dbtr");
        NodeList creditorNodes = doc.getElementsByTagName("Cdtr");
        if (debtorNodes.getLength() == 0 || creditorNodes.getLength() == 0) {
            throw new XmlSignatureException("Missing debtor or creditor information");
        }

        // Validate account information
        NodeList debtorAcctNodes = doc.getElementsByTagName("DbtrAcct");
        NodeList creditorAcctNodes = doc.getElementsByTagName("CdtrAcct");
        if (debtorAcctNodes.getLength() == 0 || creditorAcctNodes.getLength() == 0) {
            throw new XmlSignatureException("Missing debtor or creditor account information");
        }
    }
}