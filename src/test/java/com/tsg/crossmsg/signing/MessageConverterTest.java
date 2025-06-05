package com.tsg.crossmsg.signing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsg.crossmsg.signing.model.MessageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public class MessageConverterTest {
    private MessageConverter converter;
    private String xmlSample;
    private String jsonSample;
    private ObjectMapper jsonMapper;
    private DocumentBuilder documentBuilder;

    @BeforeEach
    void setUp() throws Exception {
        converter = new MessageConverter();
        jsonMapper = new ObjectMapper();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        documentBuilder = factory.newDocumentBuilder();
        
        // Load sample files from test resources
        xmlSample = new String(Files.readAllBytes(Paths.get(getClass().getResource("/iso/SinglePriority_Inbound_pacs.008.xml").toURI())));
        jsonSample = new String(Files.readAllBytes(Paths.get(getClass().getResource("/iso/SinglePriority_Inbound-pacs008.json").toURI())));
    }

    @Test
    void testXmlToJsonConversion() throws Exception {
        // Convert XML to JSON
        String convertedJson = converter.xmlToJson(xmlSample);
        
        // Parse both JSON strings to maps for comparison
        Map<String, Object> originalJson = jsonMapper.readValue(jsonSample, Map.class);
        Map<String, Object> convertedJsonMap = jsonMapper.readValue(convertedJson, Map.class);
        
        // Compare the maps
        assertEquals(originalJson.size(), convertedJsonMap.size(), "JSON maps should have the same size");
        assertEquals(originalJson.keySet(), convertedJsonMap.keySet(), "JSON maps should have the same keys");
    }

    @Test
    void testJsonToXmlConversion() throws Exception {
        // Convert JSON to XML
        String convertedXml = converter.jsonToXml(jsonSample);
        
        // Parse both XML strings to DOM for comparison
        Document originalDoc = documentBuilder.parse(new InputSource(new StringReader(xmlSample)));
        Document convertedDoc = documentBuilder.parse(new InputSource(new StringReader(convertedXml)));
        
        // Compare root elements
        Element originalRoot = originalDoc.getDocumentElement();
        Element convertedRoot = convertedDoc.getDocumentElement();
        
        assertEquals(originalRoot.getTagName(), convertedRoot.getTagName(), "Root elements should have the same name");
        compareElements(originalRoot, convertedRoot);
    }

    @Test
    void testRoundTripConversion() throws Exception {
        // Convert XML to JSON and back to XML
        String json = converter.xmlToJson(xmlSample);
        String finalXml = converter.jsonToXml(json);
        
        // Parse both XML strings to DOM for comparison
        Document originalDoc = documentBuilder.parse(new InputSource(new StringReader(xmlSample)));
        Document finalDoc = documentBuilder.parse(new InputSource(new StringReader(finalXml)));
        
        // Compare the documents
        Element originalRoot = originalDoc.getDocumentElement();
        Element finalRoot = finalDoc.getDocumentElement();
        
        assertEquals(originalRoot.getTagName(), finalRoot.getTagName(), "Root elements should have the same name");
        compareElements(originalRoot, finalRoot);
    }

    private void compareElements(Element e1, Element e2) {
        assertEquals(e1.getTagName(), e2.getTagName(), "Elements should have the same name");
        
        NodeList children1 = e1.getChildNodes();
        NodeList children2 = e2.getChildNodes();
        
        int elementCount1 = countElements(children1);
        int elementCount2 = countElements(children2);
        
        assertEquals(elementCount1, elementCount2, 
            String.format("Elements <%s> should have the same number of children", e1.getTagName()));
        
        // Compare child elements
        for (int i = 0, j = 0; i < children1.getLength() && j < children2.getLength();) {
            if (children1.item(i).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                i++;
                continue;
            }
            if (children2.item(j).getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                j++;
                continue;
            }
            compareElements((Element)children1.item(i), (Element)children2.item(j));
            i++;
            j++;
        }
    }

    private int countElements(NodeList nodes) {
        int count = 0;
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                count++;
            }
        }
        return count;
    }
} 