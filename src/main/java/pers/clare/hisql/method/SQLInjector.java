package pers.clare.hisql.method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SQLInjector {
    private static final Logger log = LogManager.getLogger();

    private static final Pattern SPACE_PATTERN = Pattern.compile("(\\s|\t|\r|\n)+");
    private static final DocumentBuilder documentBuilder;

    static {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new Error(e.getMessage(), e);
        }
    }

    private SQLInjector() {
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
                if (Node.ELEMENT_NODE != node.getNodeType()) continue;
                content = node.getTextContent();
                if (!StringUtils.hasLength(content)) continue;
                map.put(node.getNodeName(), SPACE_PATTERN.matcher(content).replaceAll(" ").trim());
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
