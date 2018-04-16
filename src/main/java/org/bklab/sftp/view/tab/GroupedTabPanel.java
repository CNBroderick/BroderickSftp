package org.bklab.sftp.view.tab;

import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.ssh2.Connection;

import java.util.List;

/**
 * @author Broderick
 */
public abstract class GroupedTabPanel extends TabPanel {
    private static final long serialVersionUID = 4102143430284110449L;
    protected List<GroupedTabPanel> group;

    public GroupedTabPanel(String type, ConnectionTabbedPane parentPanel, List<GroupedTabPanel> group, CleverTreeNode node, Connection connection) {
        super(type, parentPanel, node, connection);
        this.group = group;
    }

    public List<GroupedTabPanel> getGroup() {
        return group;
    }
}
