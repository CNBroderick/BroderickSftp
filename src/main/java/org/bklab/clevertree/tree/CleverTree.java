package org.bklab.clevertree.tree;

import org.apache.commons.io.FileUtils;
import org.bklab.clevertree.dto.NodeProperty;
import org.bklab.clevertree.dto.SecurityData;
import org.bklab.clevertree.exception.BadPathException;
import org.bklab.clevertree.exception.DuplicatedNodeException;
import org.bklab.clevertree.exception.SecurityException;
import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.clevertree.renderer.CleverTreeCellRenderer;
import org.bklab.clevertree.security.SecurityHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * @author Broderick
 */
public class CleverTree
        extends JTree {
    private static final long serialVersionUID = 571040437564328672L;
    private Map<String, CleverTreeNode> nodesMap;
    private CleverTreeNode root;

    public CleverTree(CleverTreeNode root) {
        super(root);
        this.root = root;
        nodesMap = new LinkedHashMap();
        root.setTree(this);
        setCellRenderer(new CleverTreeCellRenderer());
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public static CleverTree loadFromXML(File xml)
            throws DocumentException, IOException {
        CleverTree toReturn = null;
        try {
            toReturn = loadFromXML(xml, null);
        } catch (SecurityException e) {
        }

        return toReturn;
    }

    public static CleverTree loadFromXML(File xml, SecurityData encryptionData)
            throws DocumentException, IOException, SecurityException {
        CleverTree toReturn = null;
        SAXReader reader = new SAXReader();
        Document document = null;
        if (encryptionData == null) {
            document = reader.read(xml);
        } else {
            SecurityHelper encryptionHelper = new SecurityHelper(encryptionData);
            document = reader.read(new ByteArrayInputStream(encryptionHelper.decrypt(FileUtils.readFileToByteArray(xml))));
        }
        Element xmlRoot = document.getRootElement();
        Element xmlNode = xmlRoot.element("node");
        try {
            CleverTreeNode root = parseRoot(xmlNode);
            Element xmlChildren = xmlNode.element("children");
            toReturn = new CleverTree(root);
            if (xmlChildren != null) {
                List<Element> xmlNodeList = xmlChildren.elements("node");
                if (xmlNodeList != null) {
                    for (int i = 0; i < xmlNodeList.size(); i++) {
                        Element xmlChild = (Element) xmlNodeList.get(i);
                        parseNode(root, xmlChild);
                    }
                }
            }
        } catch (DuplicatedNodeException e) {
            throw new DocumentException(e);
        }
        return toReturn;
    }

    private static void parseNode(CleverTreeNode parent, Element xmlNode) throws DuplicatedNodeException {
        Element xmlProperties = xmlNode.element("properties");
        CleverTreeNode node = null;
        HashMap<String, NodeProperty> properties = new LinkedHashMap();
        if (xmlProperties != null) {
            List<Element> xmlPropertyList = xmlProperties.elements("property");
            if (xmlPropertyList != null) {
                for (int i = 0; i < xmlPropertyList.size(); i++) {
                    Element xmlProperty = (Element) xmlPropertyList.get(i);
                    String name = xmlProperty.attributeValue("name");
                    String value = xmlProperty.attributeValue("value");
                    boolean propertyProtected = Boolean.parseBoolean(xmlProperty.attributeValue("protected"));
                    properties.put(name, new NodeProperty(name, value, propertyProtected));
                }
            }
        }
        node = new CleverTreeNode(properties);
        parent.addChild(node);

        Element xmlChildren = xmlNode.element("children");
        if (xmlChildren != null) {
            List<Element> xmlNodeList = xmlChildren.elements("node");
            if (xmlNodeList != null) {
                for (int i = 0; i < xmlNodeList.size(); i++) {
                    Element xmlChild = (Element) xmlNodeList.get(i);
                    parseNode(node, xmlChild);
                }
            }
        }
    }

    private static CleverTreeNode parseRoot(Element xmlNode) throws DuplicatedNodeException {
        CleverTreeNode toReturn = null;
        Element xmlProperties = xmlNode.element("properties");
        if (xmlProperties != null) {
            List<Element> xmlPropertyList = xmlProperties.elements("property");
            if (xmlPropertyList != null) {
                HashMap<String, NodeProperty> properties = new LinkedHashMap();
                for (int i = 0; i < xmlPropertyList.size(); i++) {
                    Element xmlProperty = (Element) xmlPropertyList.get(i);
                    String name = xmlProperty.attributeValue("name");
                    String value = xmlProperty.attributeValue("value");
                    boolean propertyProtected = Boolean.parseBoolean(xmlProperty.attributeValue("protected"));
                    properties.put(name, new NodeProperty(name, value, propertyProtected));
                }
                toReturn = new CleverTreeNode(properties);
            }
        }
        return toReturn;
    }

    public CleverTreeNode getNodeByPath(String path)
            throws BadPathException {
        validatePath(path);
        String[] paths = path.substring(1).split("/");
        CleverTreeNode elem = getRoot();
        if (paths.length != 1) {
            for (int i = 1; i < paths.length; i++) {
                boolean found = false;
                for (int j = 0; j < elem.getChildCount(); j++) {
                    if (((CleverTreeNode) elem.getChildAt(j)).getName().equals(paths[i])) {
                        elem = (CleverTreeNode) elem.getChildAt(j);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    elem = null;
                    break;
                }

            }
        } else if (!paths[0].equals(root.getName())) {
            elem = null;
        }
        return elem;
    }


    public CleverTreeNode getNodeByUuid(String uuid) {
        CleverTreeNode toReturn = (CleverTreeNode) nodesMap.get(uuid);
        return toReturn;
    }


    public CleverTreeNode getRoot() {
        return root;
    }


    public CleverTreeNode getSelectedNode() {
        CleverTreeNode toReturn = null;
        if (getSelectionPath() == null) {
            toReturn = null;
        } else {
            toReturn = (CleverTreeNode) getSelectionPath().getLastPathComponent();
        }
        return toReturn;
    }


    public CleverTreeNode registerNode(CleverTreeNode node) {
        return (CleverTreeNode) nodesMap.put(node.getUuid(), node);
    }


    public void saveAsXML(File xml)
            throws IOException {
        try {
            saveAsXML(xml, null);
        } catch (SecurityException e) {
        }
    }


    public void saveAsXML(File xml, SecurityData encryptionData)
            throws IOException, SecurityException {
        Document document = DocumentHelper.createDocument();
        Element xmlRoot = document.addElement("smartree");
        writeNode(xmlRoot, root);

        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = null;
        if (encryptionData == null) {
            writer = new XMLWriter(new FileWriter(xml), format);
            writer.write(document);
            writer.close();
        } else {
            SecurityHelper encryptionHelper = new SecurityHelper(encryptionData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer = new XMLWriter(baos, format);
            writer.write(document);
            writer.close();
            FileUtils.writeByteArrayToFile(xml, encryptionHelper.encrypt(baos.toByteArray()));
        }
    }


    public CleverTreeNode unregisterNode(String uuid) {
        return (CleverTreeNode) nodesMap.remove(uuid);
    }

    private void validatePath(String xpath) throws BadPathException {
        boolean valid = true;
        if ((xpath == null) || (xpath.trim().equals(""))) {
            valid = false;
        } else if ((!xpath.startsWith("/")) || (xpath.endsWith("/"))) {
            valid = false;
        }
        if (!valid) {
            throw new BadPathException(xpath);
        }
    }

    private void writeNode(Element xmlParent, CleverTreeNode node) {
        Element xmlNode = xmlParent.addElement("node");
        HashMap<String, NodeProperty> nodeProperties = node.getNodeProperties();
        Element xmlProperties;
        if ((nodeProperties != null) && (nodeProperties.size() > 0)) {
            xmlProperties = xmlNode.addElement("properties");
            Set<String> keys = nodeProperties.keySet();
            for (String key : keys) {
                xmlProperties.addElement("property").addAttribute("name", key).addAttribute("value", ((NodeProperty) nodeProperties.get(key)).getValue()).addAttribute("protected", ((NodeProperty) nodeProperties.get(key)).isProtected() + "");
            }
        }
        if (node.getChildCount() > 0) {
            Element xmlChildren = xmlNode.addElement("children");
            for (int i = 0; i < node.getChildCount(); i++) {
                writeNode(xmlChildren, (CleverTreeNode) node.getChildAt(i));
            }
        }
    }
}
