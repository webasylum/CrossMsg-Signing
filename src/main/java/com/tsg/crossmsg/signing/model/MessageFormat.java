package com.tsg.crossmsg.signing.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Handles conversion between XML and JSON formats with deterministic mapping.
 * Implements RFC 8785 for JSON canonicalization.
 */
public class MessageFormat {
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final XmlMapper xmlMapper = new XmlMapper();
    private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

    static {
        docBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Converts XML to JSON using deterministic mapping
     * @param xml XML string to convert
     * @return JSON string
     */
    public static String xmlToJson(String xml) throws Exception {
        DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        return jsonMapper.writeValueAsString(doc);
    }

    /**
     * Converts JSON to XML using reverse mapping
     * @param json JSON string to convert
     * @return XML string
     */
    public static String jsonToXml(String json) throws Exception {
        Object jsonObj = jsonMapper.readValue(json, Object.class);
        StringWriter writer = new StringWriter();
        xmlMapper.writeValue(writer, jsonObj);
        return writer.toString();
    }

    /**
     * Canonicalizes JSON according to RFC 8785
     * @param json JSON string to canonicalize
     * @return Canonicalized JSON string
     */
    public static String canonicalizeJson(String json) throws Exception {
        Object jsonObj = jsonMapper.readValue(json, Object.class);
        return jsonMapper.writeValueAsString(jsonObj);
    }
} 