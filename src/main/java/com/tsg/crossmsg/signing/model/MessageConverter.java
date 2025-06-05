package com.tsg.crossmsg.signing.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class MessageConverter {
    private final ObjectMapper jsonMapper;
    private final DocumentBuilder documentBuilder;
    private final TransformerFactory transformerFactory;

    public MessageConverter() throws Exception {
        this.jsonMapper = new ObjectMapper();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        this.documentBuilder = factory.newDocumentBuilder();
        this.transformerFactory = TransformerFactory.newInstance();
    }

    public String xmlToJson(String xml) throws Exception {
        Document doc = documentBuilder.parse(new InputSource(new StringReader(xml)));
        Map<String, Object> jsonMap = convertElementToMap(doc.getDocumentElement());
        return jsonMapper.writeValueAsString(jsonMap);
    }

    public String jsonToXml(String json) throws Exception {
        Map<String, Object> jsonMap = jsonMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        Document doc = documentBuilder.newDocument();
        Element root = doc.createElement("Document");
        doc.appendChild(root);

        for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
            root.appendChild(convertMapToElement(doc, entry.getValue() instanceof Map ? 
                (Map<String, Object>) entry.getValue() : Collections.singletonMap(entry.getKey(), entry.getValue()), 
                entry.getKey()));
        }

        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertElementToMap(Element element) {
        Map<String, Object> result = new LinkedHashMap<>();
        NodeList children = element.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) continue;

            Element childElement = (Element) child;
            String key = childElement.getTagName();
            Object existing = result.get(key);

            if (childElement.hasChildNodes() && childElement.getFirstChild().getNodeType() == Node.TEXT_NODE) {
                String textContent = childElement.getTextContent().trim();
                if (!textContent.isEmpty()) {
                    if (existing != null) {
                        if (existing instanceof List) {
                            ((List<Object>) existing).add(textContent);
                        } else {
                            List<Object> list = new ArrayList<>();
                            list.add(existing);
                            list.add(textContent);
                            result.put(key, list);
                        }
                    } else {
                        result.put(key, textContent);
                    }
                }
            } else {
                Map<String, Object> childMap = convertElementToMap(childElement);
                if (!childMap.isEmpty()) {
                    if (existing != null) {
                        if (existing instanceof List) {
                            ((List<Object>) existing).add(childMap);
                        } else {
                            List<Object> list = new ArrayList<>();
                            list.add(existing);
                            list.add(childMap);
                            result.put(key, list);
                        }
                    } else {
                        result.put(key, childMap);
                    }
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Element convertMapToElement(Document doc, Map<String, Object> map, String tagName) {
        Element element = doc.createElement(tagName);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List) {
                for (Object item : (List<?>) value) {
                    if (item instanceof Map) {
                        element.appendChild(convertMapToElement(doc, (Map<String, Object>) item, key));
                    } else {
                        Element child = doc.createElement(key);
                        child.setTextContent(item.toString());
                        element.appendChild(child);
                    }
                }
            } else if (value instanceof Map) {
                element.appendChild(convertMapToElement(doc, (Map<String, Object>) value, key));
            } else {
                Element child = doc.createElement(key);
                child.setTextContent(value.toString());
                element.appendChild(child);
            }
        }
        return element;
    }
}