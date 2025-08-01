package pers.clare.hisql.method;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@UtilityClass
@Log4j2
public class SQLInjector {
    private static final DocumentBuilder documentBuilder;

    static {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    public static Map<String, String> getContents(String root, Class<?> clazz) {
        String xmlPath = toPath(root, clazz);
        Map<String, String> map = new HashMap<>();
        try (InputStream is = clazz.getClassLoader().getResourceAsStream(xmlPath)) {
            if (is == null) return map;
            log.debug("load {}", xmlPath);
            Document doc = documentBuilder.parse(is);
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            Node node;
            String content;
            for (int i = 0; i < nodeList.getLength(); i++) {
                node = nodeList.item(i);
                if (!Objects.equals(Node.ELEMENT_NODE, node.getNodeType())) continue;
                content = node.getTextContent().trim();
                if (content.isEmpty()) continue;
                map.put(node.getNodeName(), content);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return map;
    }

    private static String toPath(String root, Class<?> clazz) {
        StringBuilder path = new StringBuilder(root);
        if (!root.endsWith("/")) {
            path.append("/");
        }
        return path.append(clazz.getSimpleName())
                .append(".xml")
                .toString();
    }
}
