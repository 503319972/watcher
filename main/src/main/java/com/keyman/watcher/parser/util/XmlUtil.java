package com.keyman.watcher.parser.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.vavr.control.Try;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class XmlUtil {
    private static final Logger LOG = LoggerFactory.getLogger(XmlUtil.class);

    private static final XmlMapper XML_MAPPER;

    static {
        XML_MAPPER = new XmlMapper();
        XML_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private XmlUtil() {
    }

    public static Document loadXML(String xml) {
        return loadXML(xml, null);
    }

    public static Document loadXML(String xml, Map<String, ElementHandler> handlers) {
        try {
            SAXReader reader = new SAXReader();
            if (handlers != null) {
                handlers.forEach(reader::addHandler);
            }

            return reader.read(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception var3) {
            throw new RuntimeException("failed to loadXML " + xml, var3);
        }
    }

    public static boolean isXMLFormat(String xml) {
        try {
            loadXML(xml);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static Document loadXML(byte[] xml) {
        return Try.of(() -> loadXML(new String(xml, StandardCharsets.UTF_8))).get();
    }

    public static Document loadXML(InputStream is) {
        return Try.of(() -> {
            SAXReader reader = new SAXReader();
            return reader.read(is);
        }).get();
    }

    public static void setAttribute(Node node, String name, String value) {
        Element element = (Element)node;
        if (element.attribute(name) != null) {
            element.attribute(name).setValue(value);
        } else {
            element.addAttribute(name, value);
        }

    }

    public static void appendAttribute(Node node, String name, String value) {
        Element element = (Element)node;
        Attribute lp = element.attribute(name);
        if (lp != null) {
            lp.setValue(lp.getStringValue() + value);
        } else {
            element.addAttribute(name, value);
        }

    }

    public static String getAttributeValue(Node node, String name) {
        Element element = (Element)node;
        return element != null && element.attribute(name) != null ? element.attributeValue(name) : "";
    }

    public static String getXmlNodeValue(Node node) {
        return node != null ? node.getText() : "";
    }

    public static void setXmlNodeValue(Node node, String value) {
        if (node != null) {
            node.setText(value);
        }

    }

    public static Node addElement(Element element, String name) {
        return element.addElement(name);
    }

    public static String getString(Document doc, String path) {
        if (doc != null) {
            Node node = doc.selectSingleNode(path);
            return getXmlNodeValue(node);
        } else {
            return null;
        }
    }

    public static String getString(Node node, String path) {
        if (node != null) {
            Node childnode = node.selectSingleNode(path);
            return getXmlNodeValue(childnode);
        } else {
            return null;
        }
    }

    public static String getString(Node node, String path, String attr) {
        if (node != null) {
            Node childnode = node.selectSingleNode(path);
            return getAttributeValue(childnode, attr);
        } else {
            return null;
        }
    }

    public static Integer getInt(Node node, String path, int defaultv) {
        Node childnode = node.selectSingleNode(path);
        String txt = getXmlNodeValue(childnode);

        try {
            return Integer.parseInt(txt);
        } catch (Exception var6) {
            return defaultv;
        }
    }

    public static String getAttributeStrValue(Node node, String attrName) {
        Element element = (Element)node;
        return element != null && element.attribute(attrName) != null ? element.attributeValue(attrName) : null;
    }

    public static Number getAttributeNumValue(Node node, String attrName) {
        Element element = (Element)node;
        return node != null && element.attribute(attrName) != null ? node.numberValueOf("@" + attrName) : null;
    }

    public static <T> T fromXml(String xml, Class<T> clazz) {
        try {
            return XML_MAPPER.readValue(xml, clazz);
        } catch (Exception var3) {
            LOG.error("Parse xml to bean ERROR, xml: " + xml, var3);
            return null;
        }
    }

    public static <T> String toXml(T data) {
        try {
            return XML_MAPPER.writeValueAsString(data);
        } catch (Exception var2) {
            throw new RuntimeException("Convert to xml ERROR.", var2);
        }
    }

    public static String asXml(Document document) {
        OutputFormat format = new OutputFormat();
        format.setEncoding("UTF-8");
        format.setExpandEmptyElements(true);
        StringWriter out = new StringWriter();
        XMLWriter writer = new XMLWriter(out, format);

        try {
            writer.write(document);
            writer.flush();
        } catch (Exception var5) {
            throw new RuntimeException("Stringify to xml ERROR.", var5);
        }

        return out.toString();
    }
}

