package org.bklab.clevertree.renderer;

import org.bklab.clevertree.node.CleverTreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * @author Broderick
 */
public class CleverTreeCellRenderer
        extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 7350580228898007692L;

    public CleverTreeCellRenderer() {
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if ((value instanceof CleverTreeNode)) {
            CleverTreeNode treeNode = (CleverTreeNode) value;
            if (treeNode.getIcon() != null) {
                setIcon(treeNode.getIcon());
            }
            setText(treeNode.getPropertyValue("title"));
            setToolTipText(treeNode.getPropertyValue("description"));
        }
        return this;
    }
}
