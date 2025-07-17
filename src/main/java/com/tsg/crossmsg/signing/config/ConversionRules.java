package com.tsg.crossmsg.signing.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Configuration class defining XML to JSON conversion rules for ISO 20022 messages.
 * This class implements the ISO 20022 transformation rules for JSON Schema generation
 * as specified in the ISO 20022 supplement for JSON Schema Draft 2020-12.
 */
public class ConversionRules {
    
    // MessageBuildingBlock constants (Section 8.4)
    public static final String DESCRIPTION_SEPARATOR = " \n";
    public static final String ARRAY_TYPE = "array";
    public static final String STRING_TYPE = "string";
    public static final int DEFAULT_MIN_ITEMS = 1;
    
    // JSON Schema version (Section 8.3.3.1)
    public static final String JSON_SCHEMA_VERSION = "https://json-schema.org/draft/2020-12/schema";
    
    // Base URN for schema IDs (Section 8.3.3.2.1)
    public static final String BASE_URN = "urn:iso:std:iso:20022:tech:json:";
    
    // Schema generation constants
    public static final String SCHEMA_TYPE = "object";
    public static final boolean ADDITIONAL_PROPERTIES = false;
    public static final String ROOT_ELEMENT = "Document";
    
    // Encoding rules (Section 6.1)
    public static final String ENCODING = StandardCharsets.UTF_8.name();
    
    // MessageDefinitionIdentifier rules (Section 8.2)
    public static final String IDENTIFIER_SEPARATOR = ".";
    public static final String[] IDENTIFIER_COMPONENTS = {
        "BusinessArea",
        "Functionality",
        "Flavour",
        "Version"
    };
    
    // File naming rules (Section 8.3.1)
    public static final String SCHEMA_FILE_SUFFIX = ".schema.json";
    public static final String NAME_SEPARATOR = "-";
    
    // Namespace handling
    public static final String DEFAULT_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.09";
    public static final String BIZ_MSG_ENVLP_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:head.001.001.02";
    
    // Element transformations
    public static final Map<String, String> ELEMENT_TRANSFORMATIONS = new HashMap<>();
    static {
        // Currency elements transformation
        ELEMENT_TRANSFORMATIONS.put("Ccy", "currency");
        ELEMENT_TRANSFORMATIONS.put("Amt", "amount");
        
        // Common ISO 20022 elements
        ELEMENT_TRANSFORMATIONS.put("MsgId", "messageId");
        ELEMENT_TRANSFORMATIONS.put("CreDtTm", "creationDateTime");
        ELEMENT_TRANSFORMATIONS.put("NbOfTxs", "numberOfTransactions");
        ELEMENT_TRANSFORMATIONS.put("TtlIntrBkSttlmAmt", "totalInterbankSettlementAmount");
        
        // Payment specific elements
        ELEMENT_TRANSFORMATIONS.put("IntrBkSttlmAmt", "interbankSettlementAmount");
        ELEMENT_TRANSFORMATIONS.put("IntrBkSttlmDt", "interbankSettlementDate");
        ELEMENT_TRANSFORMATIONS.put("SttlmMtd", "settlementMethod");
        ELEMENT_TRANSFORMATIONS.put("ClrSys", "clearingSystem");
        ELEMENT_TRANSFORMATIONS.put("PmtTpInf", "paymentTypeInformation");
        ELEMENT_TRANSFORMATIONS.put("LclInstrm", "localInstrument");
        ELEMENT_TRANSFORMATIONS.put("EndToEndId", "endToEndIdentification");
        ELEMENT_TRANSFORMATIONS.put("UETR", "uetr");
        ELEMENT_TRANSFORMATIONS.put("ChrgBr", "chargeBearer");
        ELEMENT_TRANSFORMATIONS.put("FinInstnId", "financialInstitutionIdentification");
        ELEMENT_TRANSFORMATIONS.put("LEI", "lei");
        ELEMENT_TRANSFORMATIONS.put("ClrSysId", "clearingSystemIdentification");
        ELEMENT_TRANSFORMATIONS.put("MmbId", "memberIdentification");
        ELEMENT_TRANSFORMATIONS.put("Nm", "name");
        ELEMENT_TRANSFORMATIONS.put("Id", "identification");
        ELEMENT_TRANSFORMATIONS.put("Othr", "other");
        ELEMENT_TRANSFORMATIONS.put("RmtInf", "remittanceInformation");
        ELEMENT_TRANSFORMATIONS.put("Strd", "structured");
        ELEMENT_TRANSFORMATIONS.put("CdtrRefInf", "creditorReferenceInformation");
        ELEMENT_TRANSFORMATIONS.put("Ref", "reference");
    }
    
    // Elements to preserve during conversion
    public static final Set<String> PRESERVED_ELEMENTS = new HashSet<>();
    static {
        PRESERVED_ELEMENTS.add("Signature");
        PRESERVED_ELEMENTS.add("Sgntr");
        PRESERVED_ELEMENTS.add("AppHdr");
        PRESERVED_ELEMENTS.add("BizMsgEnvlp");
        PRESERVED_ELEMENTS.add("Header");
        PRESERVED_ELEMENTS.add("Body");
        PRESERVED_ELEMENTS.add("Document");
    }
    
    // Elements that should be excluded from canonicalization
    public static final Set<String> NON_CANONICAL_ELEMENTS = new HashSet<>();
    static {
        NON_CANONICAL_ELEMENTS.add("CreDtTm");  // Creation timestamp
        NON_CANONICAL_ELEMENTS.add("MsgId");    // Message ID
        NON_CANONICAL_ELEMENTS.add("NbOfTxs");  // Number of transactions
        NON_CANONICAL_ELEMENTS.add("BizMsgIdr"); // Business Message Identifier
        NON_CANONICAL_ELEMENTS.add("CreDt");    // Creation Date
    }
    
    // Currency handling
    public static final String CURRENCY_ATTRIBUTE = "Ccy";
    public static final String AMOUNT_ATTRIBUTE = "Amt";
    
    // Array handling
    public static final Set<String> ARRAY_ELEMENTS = new HashSet<>();
    static {
        ARRAY_ELEMENTS.add("CdtTrfTxInf");
        ARRAY_ELEMENTS.add("PmtInf");
        ARRAY_ELEMENTS.add("SttlmInf");
        ARRAY_ELEMENTS.add("PmtTpInf");
        ARRAY_ELEMENTS.add("RmtInf");
    }
    
    // Special handling for nested structures
    public static final Map<String, String> NESTED_STRUCTURES = new HashMap<>();
    static {
        NESTED_STRUCTURES.put("FIToFICstmrCdtTrf", "fiToFICustomerCreditTransfer");
        NESTED_STRUCTURES.put("GrpHdr", "groupHeader");
        NESTED_STRUCTURES.put("CdtTrfTxInf", "creditTransferTransactionInformation");
        NESTED_STRUCTURES.put("PmtId", "paymentIdentification");
        NESTED_STRUCTURES.put("InstgAgt", "instructingAgent");
        NESTED_STRUCTURES.put("InstdAgt", "instructedAgent");
        NESTED_STRUCTURES.put("Dbtr", "debtor");
        NESTED_STRUCTURES.put("DbtrAcct", "debtorAccount");
        NESTED_STRUCTURES.put("DbtrAgt", "debtorAgent");
        NESTED_STRUCTURES.put("CdtrAgt", "creditorAgent");
        NESTED_STRUCTURES.put("Cdtr", "creditor");
        NESTED_STRUCTURES.put("CdtrAcct", "creditorAccount");
    }
    
    // Validation rules
    public static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final Set<String> REQUIRED_ELEMENTS = new HashSet<>();
    static {
        REQUIRED_ELEMENTS.add("MsgId");
        REQUIRED_ELEMENTS.add("CreDtTm");
        REQUIRED_ELEMENTS.add("NbOfTxs");
        REQUIRED_ELEMENTS.add("IntrBkSttlmAmt");
        REQUIRED_ELEMENTS.add("IntrBkSttlmDt");
        REQUIRED_ELEMENTS.add("SttlmMtd");
        REQUIRED_ELEMENTS.add("ClrSys");
        REQUIRED_ELEMENTS.add("EndToEndId");
        REQUIRED_ELEMENTS.add("UETR");
        REQUIRED_ELEMENTS.add("ChrgBr");
        REQUIRED_ELEMENTS.add("FinInstnId");
        REQUIRED_ELEMENTS.add("LEI");
        REQUIRED_ELEMENTS.add("Nm");
        REQUIRED_ELEMENTS.add("Id");
    }
    
    // Error messages
    public static final String ERROR_INVALID_NAMESPACE = "Invalid namespace in XML document";
    public static final String ERROR_MISSING_REQUIRED_ELEMENT = "Missing required element: %s";
    public static final String ERROR_INVALID_CURRENCY = "Invalid currency format";
    public static final String ERROR_MESSAGE_TOO_LARGE = "Message exceeds maximum size limit";
    public static final String ERROR_INVALID_ENCODING = "Message must be encoded in UTF-8";
    public static final String ERROR_INVALID_IDENTIFIER = "Invalid MessageDefinitionIdentifier format";
    
    /**
     * Generates a MessageBuildingBlock description according to ISO 20022 rules (Section 8.4.1.1)
     * @param name The name of the MessageElement
     * @param definition The definition of the MessageElement
     * @return The concatenated description
     */
    public static String generateMessageBuildingBlockDescription(String name, String definition) {
        return name + DESCRIPTION_SEPARATOR + definition;
    }
    
    /**
     * Generates a MessageBuildingBlock schema for single occurrence (Section 8.4.2)
     * @param isAssociationEnd Whether this is a MessageAssociationEnd
     * @param isComposite Whether this is a composite association
     * @param referencedType The referenced type name (if applicable)
     * @return The schema object
     */
    public static Map<String, Object> generateSingleOccurrenceSchema(boolean isAssociationEnd, 
                                                                   boolean isComposite,
                                                                   String referencedType) {
        Map<String, Object> schema = new HashMap<>();
        
        if (isAssociationEnd && !isComposite) {
            schema.put("type", STRING_TYPE);
        } else {
            schema.put("$ref", "#" + referencedType);
        }
        
        return schema;
    }
    
    /**
     * Generates a MessageBuildingBlock schema for multiple occurrences (Section 8.4.3)
     * @param referencedType The referenced type name
     * @param minOccurs Minimum number of occurrences
     * @param maxOccurs Maximum number of occurrences
     * @return The schema object with anyOf array
     */
    public static Map<String, Object> generateMultipleOccurrenceSchema(String referencedType,
                                                                     int minOccurs,
                                                                     int maxOccurs) {
        Map<String, Object> schema = new HashMap<>();
        List<Map<String, Object>> anyOf = new ArrayList<>();
        
        // Single item schema
        Map<String, Object> singleItem = new HashMap<>();
        singleItem.put("$ref", "#" + referencedType);
        anyOf.add(singleItem);
        
        // Array schema
        Map<String, Object> arraySchema = new HashMap<>();
        arraySchema.put("type", ARRAY_TYPE);
        arraySchema.put("minItems", Math.max(DEFAULT_MIN_ITEMS, minOccurs));
        if (maxOccurs > 0) {
            arraySchema.put("maxItems", maxOccurs);
        }
        
        Map<String, Object> items = new HashMap<>();
        items.put("$ref", "#" + referencedType);
        arraySchema.put("items", items);
        
        anyOf.add(arraySchema);
        schema.put("anyOf", anyOf);
        
        return schema;
    }
    
    /**
     * Generates an ExternalSchema subschema (Section 8.5.1)
     * @param name The name of the MessageComponentType
     * @param definition The definition of the MessageComponentType
     * @return The schema object
     */
    public static Map<String, Object> generateExternalSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("$anchor", name);
        schema.put("description", definition);
        return schema;
    }
    
    /**
     * Generates a schema filename according to ISO 20022 rules (Section 8.3.1)
     * @param identifier The MessageDefinitionIdentifier (e.g., "acmt.002.001.08")
     * @param messageName The name of the MessageDefinition (e.g., "AccountDetailsConfirmationV08")
     * @return The properly formatted schema filename
     */
    public static String generateSchemaFilename(String identifier, String messageName) {
        return identifier + NAME_SEPARATOR + messageName + SCHEMA_FILE_SUFFIX;
    }
    
    /**
     * Validates a MessageDefinitionIdentifier according to ISO 20022 rules (Section 8.2)
     * @param identifier The identifier to validate
     * @return true if the identifier is valid
     */
    public static boolean isValidMessageDefinitionIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        String[] parts = identifier.split("\\" + IDENTIFIER_SEPARATOR);
        return parts.length == IDENTIFIER_COMPONENTS.length;
    }
    
    /**
     * Generates the schema ID according to ISO 20022 rules (Section 8.3.3.2)
     * @param identifier The MessageDefinitionIdentifier
     * @return The complete schema ID
     */
    public static String generateSchemaId(String identifier) {
        if (!isValidMessageDefinitionIdentifier(identifier)) {
            throw new IllegalArgumentException(ERROR_INVALID_IDENTIFIER);
        }
        return BASE_URN + identifier;
    }
    
    /**
     * Generates the schema description according to ISO 20022 rules (Section 8.3.3.3)
     * @param repositoryDate The date of the repository
     * @param generationSoftware The software used for generation
     * @param documentation Optional documentation text
     * @return The complete schema description
     */
    public static String generateSchemaDescription(LocalDate repositoryDate, 
                                                 String generationSoftware, 
                                                 String documentation) {
        StringBuilder description = new StringBuilder();
        description.append("Generated from ISO 20022 repository of ")
                  .append(repositoryDate.format(DateTimeFormatter.ISO_DATE))
                  .append(" by ")
                  .append(generationSoftware);
        
        if (documentation != null && !documentation.isEmpty()) {
            description.append("\n").append(documentation);
        }
        
        return description.toString();
    }
    
    /**
     * Generates the root element reference according to ISO 20022 rules (Section 8.3.3.7.1)
     * @param rootElement The name of the root element
     * @return The reference object
     */
    public static Map<String, String> generateRootElementReference(String rootElement) {
        Map<String, String> reference = new HashMap<>();
        reference.put("$ref", "#_" + rootElement);
        return reference;
    }
    
    /**
     * Generates a MessageComponent schema according to ISO 20022 rules (Section 8.5.2)
     * @param name The name of the MessageComponent
     * @param definition The definition of the MessageComponent
     * @param properties Map of property names to their schemas
     * @param requiredElements List of required element names
     * @param hasMinCardinalityOne Whether any MessageElement has minimum cardinality of one or more
     * @return The complete MessageComponent schema
     */
    public static Map<String, Object> generateMessageComponentSchema(String name,
                                                                   String definition,
                                                                   Map<String, Object> properties,
                                                                   List<String> requiredElements,
                                                                   boolean hasMinCardinalityOne) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("description", definition);
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        
        // Set minProperties only if no MessageElement has minimum cardinality of one or more
        if (!hasMinCardinalityOne) {
            schema.put("minProperties", 1);
        }
        
        // Add required elements if any exist
        if (requiredElements != null && !requiredElements.isEmpty()) {
            schema.put("required", requiredElements);
        }
        
        // Add properties
        schema.put("properties", properties);
        
        // Add anchor
        schema.put("$anchor", name);
        
        return schema;
    }
    
    /**
     * Generates a ChoiceComponent schema according to ISO 20022 rules (Section 8.5.3)
     * @param name The name of the ChoiceComponent
     * @param definition The definition of the ChoiceComponent
     * @param properties Map of property names to their schemas
     * @return The complete ChoiceComponent schema
     */
    public static Map<String, Object> generateChoiceComponentSchema(String name,
                                                                  String definition,
                                                                  Map<String, Object> properties) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("$anchor", name);
        schema.put("description", definition);
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("minProperties", 1);
        schema.put("maxProperties", 1);
        schema.put("properties", properties);
        
        return schema;
    }
    
    /**
     * Creates a property schema for a MessageElement with reference
     * @param referencedType The type to reference
     * @param description The description of the element
     * @return The property schema
     */
    public static Map<String, Object> createReferencedProperty(String referencedType, String description) {
        Map<String, Object> property = new HashMap<>();
        property.put("$ref", "#" + referencedType);
        if (description != null) {
            property.put("description", description);
        }
        return property;
    }
    
    /**
     * Creates a property schema for an array MessageElement
     * @param referencedType The type to reference for array items
     * @return The array property schema
     */
    public static Map<String, Object> createArrayProperty(String referencedType) {
        Map<String, Object> property = new HashMap<>();
        property.put("type", "array");
        
        Map<String, Object> items = new HashMap<>();
        items.put("$ref", "#" + referencedType);
        property.put("items", items);
        
        return property;
    }
    
    /**
     * Generates a MessageElement schema according to ISO 20022 rules (Section 8.6)
     * @param name The abbreviated name of the MessageElement
     * @param elementName The full name of the MessageElement
     * @param definition The definition of the MessageElement
     * @param isAssociationEnd Whether this is a MessageAssociationEnd
     * @param isComposite Whether this is a composite association
     * @param referencedType The referenced type name (if applicable)
     * @param minOccurs Minimum number of occurrences
     * @param maxOccurs Maximum number of occurrences
     * @return The complete MessageElement schema
     */
    public static Map<String, Object> generateMessageElementSchema(String name,
                                                                 String elementName,
                                                                 String definition,
                                                                 boolean isAssociationEnd,
                                                                 boolean isComposite,
                                                                 String referencedType,
                                                                 int minOccurs,
                                                                 int maxOccurs) {
        Map<String, Object> schema = new HashMap<>();
        
        // Add description (Section 8.6.1.1)
        schema.put("description", generateMessageElementDescription(elementName, definition));
        
        // Handle single occurrence (Section 8.6.2)
        if (maxOccurs <= 1) {
            if (isAssociationEnd && !isComposite) {
                schema.put("type", "string");
            } else {
                schema.put("$ref", "#" + referencedType);
            }
        } 
        // Handle multiple occurrences (Section 8.6.3)
        else {
            List<Map<String, Object>> anyOf = new ArrayList<>();
            
            // Single item schema (Section 8.6.3.1)
            Map<String, Object> singleItem = new HashMap<>();
            if (isAssociationEnd && !isComposite) {
                singleItem.put("type", "string");
            } else {
                singleItem.put("$ref", "#" + referencedType);
            }
            anyOf.add(singleItem);
            
            // Array schema (Section 8.6.3.2)
            Map<String, Object> arraySchema = new HashMap<>();
            arraySchema.put("type", "array");
            arraySchema.put("minItems", Math.max(1, minOccurs));
            if (maxOccurs > 0) {
                arraySchema.put("maxItems", maxOccurs);
            }
            
            Map<String, Object> items = new HashMap<>();
            if (isAssociationEnd && !isComposite) {
                items.put("type", "string");
            } else {
                items.put("$ref", "#" + referencedType);
            }
            arraySchema.put("items", items);
            
            anyOf.add(arraySchema);
            schema.put("anyOf", anyOf);
        }
        
        return schema;
    }
    
    /**
     * Generates a MessageElement description according to ISO 20022 rules (Section 8.6.1.1)
     * @param elementName The name of the MessageElement
     * @param definition The definition of the MessageElement
     * @return The concatenated description
     */
    public static String generateMessageElementDescription(String elementName, String definition) {
        return elementName + DESCRIPTION_SEPARATOR + definition;
    }
    
    /**
     * Creates a single occurrence MessageElement schema
     * @param isAssociationEnd Whether this is a MessageAssociationEnd
     * @param isComposite Whether this is a composite association
     * @param referencedType The referenced type name
     * @return The schema for a single occurrence
     */
    public static Map<String, Object> createSingleOccurrenceSchema(boolean isAssociationEnd,
                                                                 boolean isComposite,
                                                                 String referencedType) {
        Map<String, Object> schema = new HashMap<>();
        if (isAssociationEnd && !isComposite) {
            schema.put("type", "string");
        } else {
            schema.put("$ref", "#" + referencedType);
        }
        return schema;
    }
    
    /**
     * Creates a multiple occurrence MessageElement schema
     * @param isAssociationEnd Whether this is a MessageAssociationEnd
     * @param isComposite Whether this is a composite association
     * @param referencedType The referenced type name
     * @param minOccurs Minimum number of occurrences
     * @param maxOccurs Maximum number of occurrences
     * @return The schema for multiple occurrences
     */
    public static Map<String, Object> createMultipleOccurrenceSchema(boolean isAssociationEnd,
                                                                   boolean isComposite,
                                                                   String referencedType,
                                                                   int minOccurs,
                                                                   int maxOccurs) {
        Map<String, Object> schema = new HashMap<>();
        List<Map<String, Object>> anyOf = new ArrayList<>();
        
        // Add single item schema
        anyOf.add(createSingleOccurrenceSchema(isAssociationEnd, isComposite, referencedType));
        
        // Add array schema
        Map<String, Object> arraySchema = new HashMap<>();
        arraySchema.put("type", "array");
        arraySchema.put("minItems", Math.max(1, minOccurs));
        if (maxOccurs > 0) {
            arraySchema.put("maxItems", maxOccurs);
        }
        
        Map<String, Object> items = new HashMap<>();
        if (isAssociationEnd && !isComposite) {
            items.put("type", "string");
        } else {
            items.put("$ref", "#" + referencedType);
        }
        arraySchema.put("items", items);
        
        anyOf.add(arraySchema);
        schema.put("anyOf", anyOf);
        
        return schema;
    }
    
    /**
     * Generates a Boolean data type schema according to ISO 20022 rules (Section 9.1)
     * @param name The name of the Boolean type
     * @param definition The definition of the type
     * @return The complete Boolean schema
     */
    public static Map<String, Object> generateBooleanSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("enum", Arrays.asList("1", "0", "true", "false"));
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates an Indicator data type schema according to ISO 20022 rules (Section 9.2)
     * @param name The name of the Indicator type
     * @param definition The definition of the type
     * @return The complete Indicator schema
     */
    public static Map<String, Object> generateIndicatorSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("enum", Arrays.asList("1", "0", "true", "false"));
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Binary data type schema according to ISO 20022 rules (Section 9.3)
     * @param name The name of the Binary type
     * @param definition The definition of the type
     * @param minLength Minimum length (if specified)
     * @param maxLength Maximum length (if specified)
     * @param length Fixed length (if specified)
     * @return The complete Binary schema
     */
    public static Map<String, Object> generateBinarySchema(String name, 
                                                         String definition,
                                                         Integer minLength,
                                                         Integer maxLength,
                                                         Integer length) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        
        if (length != null) {
            schema.put("minLength", length);
            schema.put("maxLength", length);
        } else {
            if (minLength != null) {
                schema.put("minLength", minLength);
            }
            if (maxLength != null) {
                schema.put("maxLength", maxLength);
            }
        }
        
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Text/String data type schema according to ISO 20022 rules (Section 9.4)
     * @param name The name of the Text type
     * @param definition The definition of the type
     * @param minLength Minimum length (if specified)
     * @param maxLength Maximum length (if specified)
     * @param length Fixed length (if specified)
     * @param pattern Regular expression pattern (if specified)
     * @return The complete Text schema
     */
    public static Map<String, Object> generateTextSchema(String name,
                                                       String definition,
                                                       Integer minLength,
                                                       Integer maxLength,
                                                       Integer length,
                                                       String pattern) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        
        if (length != null) {
            schema.put("minLength", length);
            schema.put("maxLength", length);
        } else {
            if (minLength != null) {
                schema.put("minLength", minLength);
            }
            if (maxLength != null) {
                schema.put("maxLength", maxLength);
            }
        }
        
        if (pattern != null) {
            schema.put("pattern", pattern);
        }
        
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a CodeSet schema according to ISO 20022 rules (Section 9.5.1)
     * @param name The name of the CodeSet
     * @param definition The definition of the CodeSet
     * @param pattern Regular expression pattern (if specified)
     * @param codes List of valid codes (if specified)
     * @param minLength Minimum length (if specified)
     * @param maxLength Maximum length (if specified)
     * @param length Fixed length (if specified)
     * @return The complete CodeSet schema
     */
    public static Map<String, Object> generateCodeSetSchema(String name,
                                                          String definition,
                                                          String pattern,
                                                          List<String> codes,
                                                          Integer minLength,
                                                          Integer maxLength,
                                                          Integer length) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        
        if (codes != null && !codes.isEmpty()) {
            schema.put("enum", codes);
        } else {
            if (pattern != null) {
                schema.put("pattern", pattern);
            }
            if (length != null) {
                schema.put("minLength", length);
                schema.put("maxLength", length);
            } else {
                if (minLength != null) {
                    schema.put("minLength", minLength);
                }
                if (maxLength != null) {
                    schema.put("maxLength", maxLength);
                }
            }
        }
        
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates an ExternalCodeSet schema according to ISO 20022 rules (Section 9.5.2)
     * @param name The name of the ExternalCodeSet
     * @param definition The definition of the ExternalCodeSet
     * @param pattern Regular expression pattern (if specified)
     * @param minLength Minimum length (if specified)
     * @param maxLength Maximum length (if specified)
     * @param length Fixed length (if specified)
     * @return The complete ExternalCodeSet schema
     */
    public static Map<String, Object> generateExternalCodeSetSchema(String name,
                                                                  String definition,
                                                                  String pattern,
                                                                  Integer minLength,
                                                                  Integer maxLength,
                                                                  Integer length) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        
        if (pattern != null) {
            schema.put("pattern", pattern);
        }
        if (length != null) {
            schema.put("minLength", length);
            schema.put("maxLength", length);
        } else {
            if (minLength != null) {
                schema.put("minLength", minLength);
            }
            if (maxLength != null) {
                schema.put("maxLength", maxLength);
            }
        }
        
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates an IdentifierSet schema according to ISO 20022 rules (Section 9.6)
     * @param name The name of the IdentifierSet
     * @param definition The definition of the IdentifierSet
     * @param pattern Regular expression pattern (if specified)
     * @param minLength Minimum length (if specified)
     * @param maxLength Maximum length (if specified)
     * @param length Fixed length (if specified)
     * @return The complete IdentifierSet schema
     */
    public static Map<String, Object> generateIdentifierSetSchema(String name,
                                                                String definition,
                                                                String pattern,
                                                                Integer minLength,
                                                                Integer maxLength,
                                                                Integer length) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        
        if (pattern != null) {
            schema.put("pattern", pattern);
        }
        if (length != null) {
            schema.put("minLength", length);
            schema.put("maxLength", length);
        } else {
            if (minLength != null) {
                schema.put("minLength", minLength);
            }
            if (maxLength != null) {
                schema.put("maxLength", maxLength);
            }
        }
        
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a decimal pattern according to ISO 20022 rules (Section 10.1)
     * @param totalDigits Total number of digits (if specified)
     * @param fractionDigits Number of decimal places (if specified)
     * @param minInclusive Minimum inclusive value (if specified)
     * @param maxInclusive Maximum inclusive value (if specified)
     * @param minExclusive Minimum exclusive value (if specified)
     * @param maxExclusive Maximum exclusive value (if specified)
     * @return The complete decimal pattern
     */
    public static String generateDecimalPattern(Integer totalDigits,
                                              Integer fractionDigits,
                                              Double minInclusive,
                                              Double maxInclusive,
                                              Double minExclusive,
                                              Double maxExclusive) {
        // Determine sign expression
        String signExpression;
        if ((minInclusive != null && minInclusive >= 0) || 
            (minExclusive != null && minExclusive >= 0)) {
            signExpression = "[+]?";
        } else if ((maxInclusive != null && maxInclusive < 0) || 
                   (maxExclusive != null && maxExclusive <= 0)) {
            signExpression = "-";
        } else {
            signExpression = "[+-]?";
        }
        
        // Build pattern based on totalDigits and fractionDigits
        StringBuilder pattern = new StringBuilder("^");
        pattern.append(signExpression);
        
        if (totalDigits != null && totalDigits > 0) {
            if (fractionDigits != null && fractionDigits > 0) {
                // Case 1a: totalDigits > 0, fractionDigits > 0
                pattern.append("0*((")
                       .append("(0|[1-9][0-9]{0,").append(totalDigits - 1).append("})([.]0*)?)|")
                       .append("([.][0-9]{1,").append(fractionDigits).append("}0*)|")
                       .append("((?=[1-9.][0-9.]{1,").append(totalDigits).append("}0*$)([1-9][0-9]*)?[.][0-9]{0,")
                       .append(fractionDigits).append("}0*)))$");
            } else if (fractionDigits != null && fractionDigits == 0) {
                // Case 1b: totalDigits > 0, fractionDigits = 0
                pattern.append("0*((")
                       .append("(0|[1-9][0-9]{0,").append(totalDigits - 1).append("})([.]0*)?)|")
                       .append("[.][0]+)$");
            } else {
                // Case 1c: totalDigits > 0, fractionDigits not defined
                pattern.append("0*((")
                       .append("(0|[1-9][0-9]{0,").append(totalDigits - 1).append("})([.]0*)?)|")
                       .append("([.][0-9]{1,").append(totalDigits).append("}0*)|")
                       .append("((?=[1-9.][0-9.]{1,").append(totalDigits).append("}0*$)([1-9][0-9]*)?[.][0-9]*)))$");
            }
        } else {
            if (fractionDigits != null && fractionDigits > 0) {
                // Case 2a: totalDigits not defined, fractionDigits > 0
                pattern.append("([0-9]+([.][0-9]{0,").append(fractionDigits).append("}0*)?|")
                       .append("0*[.][0-9]{1,").append(fractionDigits).append("}0*))$");
            } else if (fractionDigits != null && fractionDigits == 0) {
                // Case 2b: totalDigits not defined, fractionDigits = 0
                pattern.append("0*((")
                       .append("(0|[1-9][0-9]*)([.]0*)?)|")
                       .append("[.][0]+)$");
            } else {
                // Case 2c: totalDigits not defined, fractionDigits not defined
                pattern.append("(([0-9]+[.]?)|(0*[.][0-9]+)|((?=[0-9.]{2,})[0-9]*[.][0-9]*))$");
            }
        }
        
        return pattern.toString();
    }
    
    /**
     * Generates a Decimal data type schema according to ISO 20022 rules (Section 10.2)
     * @param name The name of the Decimal type
     * @param definition The definition of the type
     * @param totalDigits Total number of digits (if specified)
     * @param fractionDigits Number of decimal places (if specified)
     * @param minInclusive Minimum inclusive value (if specified)
     * @param maxInclusive Maximum inclusive value (if specified)
     * @param minExclusive Minimum exclusive value (if specified)
     * @param maxExclusive Maximum exclusive value (if specified)
     * @return The complete Decimal schema
     */
    public static Map<String, Object> generateDecimalSchema(String name,
                                                          String definition,
                                                          Integer totalDigits,
                                                          Integer fractionDigits,
                                                          Double minInclusive,
                                                          Double maxInclusive,
                                                          Double minExclusive,
                                                          Double maxExclusive) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", generateDecimalPattern(totalDigits, fractionDigits,
                                                   minInclusive, maxInclusive,
                                                   minExclusive, maxExclusive));
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Quantity data type schema according to ISO 20022 rules (Section 10.3)
     * @param name The name of the Quantity type
     * @param definition The definition of the type
     * @param totalDigits Total number of digits (if specified)
     * @param fractionDigits Number of decimal places (if specified)
     * @param minInclusive Minimum inclusive value (if specified)
     * @param maxInclusive Maximum inclusive value (if specified)
     * @param minExclusive Minimum exclusive value (if specified)
     * @param maxExclusive Maximum exclusive value (if specified)
     * @return The complete Quantity schema
     */
    public static Map<String, Object> generateQuantitySchema(String name,
                                                           String definition,
                                                           Integer totalDigits,
                                                           Integer fractionDigits,
                                                           Double minInclusive,
                                                           Double maxInclusive,
                                                           Double minExclusive,
                                                           Double maxExclusive) {
        return generateDecimalSchema(name, definition, totalDigits, fractionDigits,
                                   minInclusive, maxInclusive, minExclusive, maxExclusive);
    }
    
    /**
     * Generates a Rate data type schema according to ISO 20022 rules (Section 10.4)
     * @param name The name of the Rate type
     * @param definition The definition of the type
     * @param baseValue The base value for the rate
     * @param totalDigits Total number of digits (if specified)
     * @param fractionDigits Number of decimal places (if specified)
     * @param minInclusive Minimum inclusive value (if specified)
     * @param maxInclusive Maximum inclusive value (if specified)
     * @param minExclusive Minimum exclusive value (if specified)
     * @param maxExclusive Maximum exclusive value (if specified)
     * @return The complete Rate schema
     */
    public static Map<String, Object> generateRateSchema(String name,
                                                       String definition,
                                                       Double baseValue,
                                                       Integer totalDigits,
                                                       Integer fractionDigits,
                                                       Double minInclusive,
                                                       Double maxInclusive,
                                                       Double minExclusive,
                                                       Double maxExclusive) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", generateDecimalPattern(totalDigits, fractionDigits,
                                                   minInclusive, maxInclusive,
                                                   minExclusive, maxExclusive));
        
        // Add baseValue to description if provided
        String fullDescription = definition;
        if (baseValue != null) {
            fullDescription = "baseValue=" + baseValue + "\n\n" + definition;
        }
        schema.put("description", fullDescription);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates an Amount data type schema according to ISO 20022 rules (Section 10.5)
     * @param name The name of the Amount type
     * @param definition The definition of the type
     * @param currencyIdentifierSet The currency identifier set (if specified)
     * @param totalDigits Total number of digits (if specified)
     * @param fractionDigits Number of decimal places (if specified)
     * @param minInclusive Minimum inclusive value (if specified)
     * @param maxInclusive Maximum inclusive value (if specified)
     * @param minExclusive Minimum exclusive value (if specified)
     * @param maxExclusive Maximum exclusive value (if specified)
     * @return The complete Amount schema
     */
    public static Map<String, Object> generateAmountSchema(String name,
                                                         String definition,
                                                         String currencyIdentifierSet,
                                                         Integer totalDigits,
                                                         Integer fractionDigits,
                                                         Double minInclusive,
                                                         Double maxInclusive,
                                                         Double minExclusive,
                                                         Double maxExclusive) {
        if (currencyIdentifierSet != null && !currencyIdentifierSet.isEmpty()) {
            // Case 10.5.1: With CurrencyIdentifierSet
            Map<String, Object> schema = new HashMap<>();
            schema.put("type", "object");
            schema.put("additionalProperties", false);
            
            // Add required properties
            List<String> required = new ArrayList<>();
            required.add("amt");
            required.add("Ccy");
            schema.put("required", required);
            
            // Add properties
            Map<String, Object> properties = new HashMap<>();
            
            // Amount property
            Map<String, Object> amount = new HashMap<>();
            amount.put("type", "string");
            amount.put("pattern", generateDecimalPattern(totalDigits, fractionDigits,
                                                       minInclusive, maxInclusive,
                                                       minExclusive, maxExclusive));
            properties.put("amt", amount);
            
            // Currency property
            Map<String, Object> currency = new HashMap<>();
            currency.put("$ref", "#" + currencyIdentifierSet);
            properties.put("Ccy", currency);
            
            schema.put("properties", properties);
            schema.put("description", definition);
            schema.put("$anchor", name);
            
            return schema;
        } else {
            // Case 10.5.2: Without CurrencyIdentifierSet
            return generateDecimalSchema(name, definition, totalDigits, fractionDigits,
                                       minInclusive, maxInclusive, minExclusive, maxExclusive);
        }
    }
    
    /**
     * Generates a Duration data type schema according to ISO 20022 rules (Section 11.1)
     * @param name The name of the Duration type
     * @param definition The definition of the type
     * @return The complete Duration schema
     */
    public static Map<String, Object> generateDurationSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^-?P((([0-9]+Y([0-9]+M)?([0-9]+D)?|([0-9]+M)([0-9]+D)?|([0-9]+D))(T(([0-9]+H)([0-9]+M)?([0-9]+([.][0-9]+)?S)?|([0-9]+M)([0-9]+([.][0-9]+)?S)?|([0-9]+([.][0-9]+)?S)))?)|(T(([0-9]+H)([0-9]+M)?([0-9]+([.][0-9]+)?S)?|([0-9]+M)([0-9]+([.][0-9]+)?S)?|([0-9]+([.][0-9]+)?S))))$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a DateTime data type schema according to ISO 20022 rules (Section 11.2)
     * @param name The name of the DateTime type
     * @param definition The definition of the type
     * @return The complete DateTime schema
     */
    public static Map<String, Object> generateDateTimeSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](.[0-9]+)?|(24:00:00([.]0+)?))(Z|([+]|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a NormalisedDateTime data type schema according to ISO 20022 rules (Section 11.2.1)
     * @param name The name of the NormalisedDateTime type
     * @param definition The definition of the type
     * @return The complete NormalisedDateTime schema
     */
    public static Map<String, Object> generateNormalisedDateTimeSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]([.][0-9]+)?|(24:00:00([.]0+)?))Z$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Date data type schema according to ISO 20022 rules (Section 11.3)
     * @param name The name of the Date type
     * @param definition The definition of the type
     * @return The complete Date schema
     */
    public static Map<String, Object> generateDateSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^-?([1-9][0-9]{3,}|0[0-9]{3})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])(Z|([+-](0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Time data type schema according to ISO 20022 rules (Section 11.4)
     * @param name The name of the Time type
     * @param definition The definition of the type
     * @return The complete Time schema
     */
    public static Map<String, Object> generateTimeSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^(([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]([.][0-9]+)?|(24:00:00([.]0+)?))(Z|([+-](0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a YearMonth data type schema according to ISO 20022 rules (Section 11.5)
     * @param name The name of the YearMonth type
     * @param definition The definition of the type
     * @return The complete YearMonth schema
     */
    public static Map<String, Object> generateYearMonthSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^-?([1-9][0-9]{3,})-(0[1-9]|1[0-2])(Z|([+-](0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a MonthDay data type schema according to ISO 20022 rules (Section 11.6)
     * @param name The name of the MonthDay type
     * @param definition The definition of the type
     * @return The complete MonthDay schema
     */
    public static Map<String, Object> generateMonthDaySchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^--(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])(Z|([+-](0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Year data type schema according to ISO 20022 rules (Section 11.7)
     * @param name The name of the Year type
     * @param definition The definition of the type
     * @return The complete Year schema
     */
    public static Map<String, Object> generateYearSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^-?([1-9][0-9]{3,}|0[0-9]{3})(Z|([+-](0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Month data type schema according to ISO 20022 rules (Section 11.8)
     * @param name The name of the Month type
     * @param definition The definition of the type
     * @return The complete Month schema
     */
    public static Map<String, Object> generateMonthSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^--(0[1-9]|1[0-2])(Z|([+-](0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a Day data type schema according to ISO 20022 rules (Section 11.9)
     * @param name The name of the Day type
     * @param definition The definition of the type
     * @return The complete Day schema
     */
    public static Map<String, Object> generateDaySchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("pattern", "^---(0[1-9]|[12][0-9]|3[01])(Z|([+-](0[0-9]|1[0-3]):[0-5][0-9]|14:00))?$");
        schema.put("description", definition);
        schema.put("$anchor", name);
        return schema;
    }
    
    /**
     * Generates a schema for handling currency amount elements according to ISO 20022 rules
     * @param name The name of the currency amount type
     * @param definition The definition of the type
     * @param currencyIdentifierSet The currency identifier set to use
     * @return The complete currency amount schema
     */
    public static Map<String, Object> generateCurrencyAmountSchema(String name, String definition, String currencyIdentifierSet) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("description", definition);
        schema.put("$anchor", name);
        schema.put("required", Arrays.asList("amt", "Ccy"));
        schema.put("properties", new HashMap<String, Object>() {{
            put("amt", generateDecimalSchema("Amount", "Amount of money", 18, 5, null, null, null, null));
            put("Ccy", generateCodeSetSchema("Currency", "Currency code", "^[A-Z]{3}$", null, 3, 3, 3));
        }});
        schema.put("minProperties", 2);
        schema.put("maxProperties", 2);
        return schema;
    }

    /**
     * Generates a schema for handling choice components according to ISO 20022 rules
     * @param name The name of the choice component
     * @param definition The definition of the type
     * @param properties The properties of the choice component
     * @param minCardinality The minimum cardinality of the choice
     * @param maxCardinality The maximum cardinality of the choice
     * @return The complete choice component schema
     */
    public static Map<String, Object> generateChoiceComponentSchema(String name,
                                                                  String definition,
                                                                  Map<String, Object> properties,
                                                                  int minCardinality,
                                                                  int maxCardinality) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("description", definition);
        schema.put("$anchor", name);
        schema.put("properties", properties);
        schema.put("minProperties", minCardinality);
        schema.put("maxProperties", maxCardinality);
        return schema;
    }

    /**
     * Generates a schema for handling message components according to ISO 20022 rules
     * @param name The name of the message component
     * @param definition The definition of the type
     * @param properties The properties of the message component
     * @param requiredElements The list of required elements
     * @return The complete message component schema
     */
    public static Map<String, Object> generateMessageComponentSchema(String name,
                                                                   String definition,
                                                                   Map<String, Object> properties,
                                                                   List<String> requiredElements) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("description", definition);
        schema.put("$anchor", name);
        schema.put("properties", properties);
        schema.put("required", requiredElements);
        schema.put("minProperties", 1);
        return schema;
    }

    /**
     * Generates a schema for handling arrays according to ISO 20022 rules
     * @param name The name of the array type
     * @param definition The definition of the type
     * @param items The schema for array items
     * @param minItems The minimum number of items
     * @param maxItems The maximum number of items
     * @return The complete array schema
     */
    public static Map<String, Object> generateArraySchema(String name,
                                                        String definition,
                                                        Map<String, Object> items,
                                                        int minItems,
                                                        Integer maxItems) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "array");
        schema.put("description", definition);
        schema.put("$anchor", name);
        schema.put("items", items);
        schema.put("minItems", minItems);
        if (maxItems != null) {
            schema.put("maxItems", maxItems);
        }
        return schema;
    }

    /**
     * Generates a schema for handling boolean values as strings according to ISO 20022 rules
     * @param name The name of the boolean type
     * @param definition The definition of the type
     * @return The complete boolean schema
     */
    public static Map<String, Object> generateBooleanAsStringSchema(String name, String definition) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("description", definition);
        schema.put("$anchor", name);
        schema.put("pattern", "^(true|false)$");
        return schema;
    }

    /**
     * Generates a schema for handling numbers as strings according to ISO 20022 rules
     * @param name The name of the number type
     * @param definition The definition of the type
     * @param totalDigits The total number of digits
     * @param fractionDigits The number of fraction digits
     * @param minInclusive The minimum inclusive value
     * @param maxInclusive The maximum inclusive value
     * @return The complete number schema
     */
    public static Map<String, Object> generateNumberAsStringSchema(String name,
                                                                String definition,
                                                                Integer totalDigits,
                                                                Integer fractionDigits,
                                                                Double minInclusive,
                                                                Double maxInclusive) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "string");
        schema.put("description", definition);
        schema.put("$anchor", name);
        schema.put("pattern", generateDecimalPattern(totalDigits, fractionDigits, minInclusive, maxInclusive, null, null));
        return schema;
    }

    private ConversionRules() {
        // Private constructor to prevent instantiation
    }
} 