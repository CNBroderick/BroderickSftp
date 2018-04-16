package org.bklab.clevertree.node;

import org.bklab.clevertree.dto.NodeProperty;
import org.bklab.clevertree.exception.DuplicatedNodeException;
import org.bklab.clevertree.exception.PermissionDeniedException;
import org.bklab.clevertree.tree.CleverTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;

/**
 * @author Broderick
 */
public class CleverTreeNode
        extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 85926873885946941L;
    private static Toolkit toolkit = Toolkit.getDefaultToolkit();
    private Icon icon;
    private HashMap<String, NodeProperty> properties;
    private CleverTree tree;


    public CleverTreeNode(HashMap<String, NodeProperty> properties) {
        super(getSuperName(properties));
        this.properties = properties;
        if (properties.get("name") == null) {
            properties.put("name", new NodeProperty("name", "node", true));
        } else {
            ((NodeProperty) properties.get("name")).setProtected(true);
        }
        if (properties.get("title") == null) {
            properties.put("title", new NodeProperty("title", ((NodeProperty) properties.get("name")).getValue(), false));
        }
        if (properties.get("icon") != null) {
            icon = createImage(((NodeProperty) properties.get("icon")).getValue());
            ((NodeProperty) properties.get("icon")).setProtected(true);
        }
        if (properties.get("uuid") == null) {
            properties.put("uuid", new NodeProperty("uuid", UUID.randomUUID().toString(), true));
        } else {
            ((NodeProperty) properties.get("uuid")).setProtected(true);
        }
    }

    public CleverTreeNode(String title) {
        super(title);
        properties = new LinkedHashMap();
        properties.put("uuid", new NodeProperty("uuid", UUID.randomUUID().toString(), true));
        properties.put("name", new NodeProperty("name", title, true));
        properties.put("title", new NodeProperty("title", title, false));
    }


    public CleverTreeNode(String name, String title, String description, String iconPath) {
        super(title);
        properties = new LinkedHashMap();
        properties.put("uuid", new NodeProperty("uuid", UUID.randomUUID().toString(), true));
        properties.put("name", new NodeProperty("name", name, true));
        properties.put("title", new NodeProperty("title", name, false));
        properties.put("description", new NodeProperty("description", description, false));
        properties.put("icon", new NodeProperty("icon", iconPath, true));
        if (iconPath != null) {
            createImage(iconPath);
        }
    }

    private static String getSuperName(HashMap<String, NodeProperty> properties) {
        String toReturn = "node";
        if (properties.get("title") != null) {
            toReturn = ((NodeProperty) properties.get("title")).getValue();
        } else if (properties.get("name") != null) {
            toReturn = ((NodeProperty) properties.get("name")).getValue();
        }
        return toReturn;
    }

    public CleverTreeNode addChild(CleverTreeNode node)
            throws DuplicatedNodeException {
        return addChild(node, false, null);
    }


    public CleverTreeNode addChild(CleverTreeNode node, boolean expand, Comparator<CleverTreeNode> sortComparator)
            throws DuplicatedNodeException {
        if (getTree().getNodeByUuid(node.getUuid()) != null) {
            throw new DuplicatedNodeException("Duplicate uuid: " + node.getUuid());
        }
        if (getChildCount() == 0) {
            super.add(node);
        } else {
            String newName = node.getName();
            for (int i = 0; i < getChildCount(); i++) {
                if (newName.equals(((CleverTreeNode) getChildAt(i)).getName())) {
                    throw new DuplicatedNodeException("Duplicate name: " + newName);
                }
            }
            if (sortComparator == null) {
                super.add(node);
            } else {
                boolean added = false;
                for (int i = 0; i < getChildCount(); i++) {
                    if (sortComparator.compare(node, (CleverTreeNode) getChildAt(i)) < 0) {
                        insert(node, i);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    super.add(node);
                }
            }
        }
        getTree().registerNode(node);
        if (expand) {
            expand();
        }
        return node;
    }


    public CleverTreeNode collapse() {
        getTree().collapsePath(new TreePath(getPath()));
        return this;
    }

    private Icon createImage(String path) {
        Icon toReturn = null;
        if ((path != null) && (!path.trim().equals(""))) {
            toReturn = new ImageIcon(toolkit.getImage(path));
        }
        return toReturn;
    }


    public CleverTreeNode expand() {
        getTree().expandPath(new TreePath(getPath()));
        return this;
    }


    public String getDescrition() {
        return ((NodeProperty) properties.get("description")).getValue();
    }


    public Icon getIcon() {
        return icon;
    }


    public String getIconPath() {
        return ((NodeProperty) properties.get("icon")).getValue();
    }


    public String getName() {
        return ((NodeProperty) properties.get("name")).getValue();
    }

    public CleverTreeNode setName(String name)
            throws DuplicatedNodeException {
        if (!name.equals(getName())) {
            CleverTreeNode parent = (CleverTreeNode) getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                if (((CleverTreeNode) parent.getChildAt(i)).getName().equals(name)) {
                    throw new DuplicatedNodeException("Duplicate name: " + name);
                }
            }
            properties.put("name", new NodeProperty("name", name, true));
        }
        return this;
    }

    public String getNodePath() {
        StringBuilder toReturn = new StringBuilder();
        TreeNode[] paths = getPath();
        for (TreeNode path : paths) {
            toReturn.append("/" + ((CleverTreeNode) path).getPropertyValue("name"));
        }
        return toReturn.toString();
    }

    public HashMap<String, NodeProperty> getNodeProperties() {
        HashMap<String, NodeProperty> toReturn = new LinkedHashMap();
        Set<String> keys = properties.keySet();
        for (String key : keys) {
            toReturn.put(key, properties.get(key));
        }
        return toReturn;
    }

    public String getPropertyValue(String key) {
        NodeProperty property = (NodeProperty) properties.get(key);
        return property == null ? null : property.getValue();
    }

    public String getTitle() {
        return ((NodeProperty) properties.get("title")).getValue();
    }

    public CleverTreeNode setTitle(String title) {
        properties.put("title", new NodeProperty("title", title, false));
        return this;
    }

    public CleverTree getTree() {
        return ((CleverTreeNode) this.getRoot()).tree;
    }

    public CleverTreeNode setTree(CleverTree tree) {
        if (this == getRoot()) {
            this.tree = tree;
        }
        return this;
    }

    public String getUuid() {
        return ((NodeProperty) properties.get("uuid")).getValue();
    }

    public CleverTreeNode setUuid(String uuid)
            throws DuplicatedNodeException {
        if (!uuid.equals(getUuid())) {
            CleverTreeNode node = null;
            String oldUuid = null;
            node = getTree().getNodeByUuid(uuid);
            if ((node != null) && (node != this)) {
                throw new DuplicatedNodeException("Duplicate uuid: " + uuid);
            }
            oldUuid = node.getUuid();
            properties.put("uuid", new NodeProperty("uuid", uuid, true));
            getTree().unregisterNode(oldUuid);
            getTree().registerNode(node);
        }
        return this;
    }

    public CleverTreeNode remove() {
        removeFromParent();
        return this;
    }

    public CleverTreeNode select() {
        getTree().setSelectionPath(new TreePath(getPath()));
        return this;
    }

    public CleverTreeNode setDescription(String description) {
        properties.put("description", new NodeProperty("description", description, false));
        return this;
    }

    public CleverTreeNode setIcon(String icon, boolean updateProperty) {
        this.icon = createImage(icon);
        if (updateProperty) {
            properties.put("icon", new NodeProperty("icon", icon, true));
        }
        return this;
    }

    public void setParent(MutableTreeNode newParent) {
        if (newParent == null) {
            getTree().unregisterNode(getUuid());
        }
        super.setParent(newParent);
    }

    public NodeProperty setProperty(NodeProperty property)
            throws PermissionDeniedException {
        if ((property != null) && (property.getName() != null) && (property.getValue() != null)) {
            NodeProperty element = (NodeProperty) properties.get(property.getName());
            if ((element == null) || (!element.isProtected())) {
                properties.put(property.getName(), property);
            } else {
                throw new PermissionDeniedException(property.getName());
            }
        }
        return property;
    }
}
