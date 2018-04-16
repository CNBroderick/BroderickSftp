package org.bklab.sftp.view.tab;

import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.ssh2.Connection;
import org.bklab.ssh2.Session;

import javax.swing.*;

/**
 * @author Broderick
 */
public abstract class TabPanel extends JPanel {
    public static final String TYPE_CONNECTIONS = "CONNECTIONS";
    public static final String TYPE_SSH = "SSH";
    public static final String TYPE_SFTP = "SFTP";

    private static final long serialVersionUID = 7172200593953753443L;
    private String type;
    protected CleverTreeNode node;
    protected ConnectionTabbedPane parentPanel;
    protected Connection connection;
    protected Session session;


    public TabPanel(String type, ConnectionTabbedPane parentPanel, CleverTreeNode node, Connection connection) {
        super();
        this.type = type;
        this.parentPanel = parentPanel;
        this.connection = connection;
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public ConnectionTabbedPane getParentPanel() {
        return parentPanel;
    }

    public void setParentPanel(ConnectionTabbedPane parentPanel) {
        this.parentPanel = parentPanel;
    }

    public CleverTreeNode getNode() {
        return node;
    }

    public abstract void close();

    public abstract void focus();

}
