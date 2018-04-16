package org.bklab.sftp.utils;

import org.bklab.clevertree.node.CleverTreeNode;

import java.util.Comparator;

/**
 * @author Broderick
 */
public class ConnectionNodesComparator implements Comparator<CleverTreeNode> {
    @Override
    public int compare(CleverTreeNode o1, CleverTreeNode o2) {
        if (o1.getPropertyValue("type").equals(o2.getPropertyValue("type"))) {
            return o1.getName().compareTo(o2.getName());
        } else if (o1.getPropertyValue("type").equals("group")) {
            return -1;
        } else {
            return 1;
        }
    }
}
