package org.bklab.sftp.view.panel;

import org.bklab.clevertree.dto.NodeProperty;
import org.bklab.clevertree.exception.DuplicatedNodeException;
import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.clevertree.tree.CleverTree;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.core.Broderick;
import org.bklab.sftp.core.connection.ConnectionManager;
import org.bklab.sftp.utils.ConnectionNodesComparator;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.dialog.AboutDialog;
import org.bklab.sftp.view.dialog.NewConnectionDialog;
import org.bklab.sftp.view.dialog.NewGroupDialog;
import org.bklab.sftp.view.tab.ConnectionTabbedPane;
import org.bklab.sftp.view.tab.GroupedTabPanel;
import org.bklab.sftp.view.tab.TabPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Broderick
 */
public class ConnectionsPanel extends TabPanel implements ActionListener, TreeSelectionListener, MouseListener {

    private static final long serialVersionUID = 6277265887166890684L;
    private CleverTree tree;
    private String connectionType;

    private JButton buttonDelete;
    private JButton buttonSsh;
    private JButton buttonSftp;

    public ConnectionsPanel(ConnectionTabbedPane parentPanel, CleverTree tree) {
        super(TabPanel.TYPE_CONNECTIONS, parentPanel, null, null);
        connectionType = "none";
        setLayout(new BorderLayout());

        setBorder(new EmptyBorder(0, 5, 5, 5));
        this.tree = tree;

        tree.setBorder(new EmptyBorder(5, 5, 5, 5));
        tree.addTreeSelectionListener(this);
        tree.addMouseListener(this);
        JPanel panelTree = new JPanel(new BorderLayout());
        panelTree.add(tree, BorderLayout.CENTER);
        panelTree.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        add(panelTree, BorderLayout.CENTER);

        buttonSsh = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_TERMINAL));
        buttonSsh.setOpaque(false);
        buttonSsh.setEnabled(false);
        buttonSsh.setToolTipText("Open SSH");
        buttonSsh.setActionCommand("ssh");
        buttonSsh.addActionListener(this);

        buttonSftp = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_FOLDERS_EXPLORER));
        buttonSftp.setOpaque(false);
        buttonSftp.setEnabled(false);
        buttonSftp.setToolTipText("Open SFTP");
        buttonSftp.setActionCommand("sftp");
        buttonSftp.addActionListener(this);

        JButton buttonAbout = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_INFORMATIONS));
        buttonAbout.setOpaque(false);
        buttonAbout.setToolTipText("About " + Broderick.PROGRAM_NAME + "...");
        buttonAbout.setActionCommand("about");
        buttonAbout.addActionListener(this);

        JButton buttonNewConnection = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_CONNECTION_NEW));
        buttonNewConnection.setOpaque(false);
        buttonNewConnection.setToolTipText("New connection...");
        buttonNewConnection.setActionCommand("newConnection");
        buttonNewConnection.addActionListener(this);

        JButton buttonNewGroup = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_FOLDER_NEW));
        buttonNewGroup.setOpaque(false);
        buttonNewGroup.setToolTipText("New group...");
        buttonNewGroup.setActionCommand("newGroup");
        buttonNewGroup.addActionListener(this);

        buttonDelete = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_DELETE));
        buttonDelete.setOpaque(false);
        buttonDelete.setEnabled(false);
        buttonDelete.setToolTipText("Delete");
        buttonDelete.setActionCommand("delete");
        buttonDelete.addActionListener(this);


        JToolBar toolbar = new JToolBar();
        toolbar.setOpaque(false);
        toolbar.setFloatable(false);
        toolbar.add(buttonSsh);
        //toolbar.addSeparator();
        toolbar.add(buttonSftp);
        toolbar.addSeparator();
        toolbar.add(buttonNewConnection);
        toolbar.add(buttonNewGroup);
        //toolbar.addSeparator();
        toolbar.add(buttonDelete);
        toolbar.addSeparator();
        toolbar.add(buttonAbout);
        setOpaque(false);
        add(toolbar, BorderLayout.NORTH);
    }

    @Override
    public void close() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "ssh":
                ssh(tree.getSelectedNode());
                break;
            case "sftp":
                sftp(tree.getSelectedNode(), "/");
                break;
            case "newConnection":
                newConnection();
                break;
            case "newGroup":
                newGroup();
                break;
            case "delete":
                delete();
                break;
            case "about":
                about();
                break;
        }
    }

    private void about() {
        new AboutDialog(parentPanel.getFrame());
    }

    private void newConnection() {
        CleverTreeNode parent = findParentGroup();
        NewConnectionDialog dialog = new NewConnectionDialog(parentPanel.getFrame(), parent);
        if (dialog.isValidated()) {
            String name = dialog.getTextName().getText();
            String host = dialog.getTextHost().getText();
            String sshPort = dialog.getTextSshPort().getText();
            String username = dialog.getTextUsername().getText();
            String password = new String(dialog.getTextPassword().getPassword());
            File customCommandFile = dialog.getCustomCommandFile();
            HashMap<String, NodeProperty> properties = new LinkedHashMap<>();
            properties.put("name", new NodeProperty("name", name, true));
            properties.put("type", new NodeProperty("type", "connection"));
            properties.put("host", new NodeProperty("host", host));
            properties.put("sshPort", new NodeProperty("sshPort", sshPort));
            properties.put("username", new NodeProperty("username", username));
            properties.put("password", new NodeProperty("password", password));
            properties.put("description", new NodeProperty("description", username + "@" + host));
            properties.put("icon", new NodeProperty("icon", DataPath.IMG_16_TERMINAL_OFF, true));
            properties.put("color", new NodeProperty("color", dialog.getColor()));
            if (customCommandFile != null) {
                String localPath = "commands" + File.separator + customCommandFile.getName();
                File localFile = new File(localPath);
                if (localFile.exists() && localFile.isFile() && localFile.getAbsolutePath().equals(customCommandFile.getAbsolutePath())) {
                    properties.put("commands", new NodeProperty("commands", localPath));
                } else {
                    properties.put("commands", new NodeProperty("commands", customCommandFile.getAbsolutePath()));
                }
            }
            CleverTreeNode toAdd = new CleverTreeNode(properties);
            try {
                parent.addChild(toAdd, true, new ConnectionNodesComparator());
                tree.setSelectionPath(new TreePath(toAdd.getPath()));
                tree.updateUI();
                try {
                    parentPanel.getFrame().saveXml(tree);
                } catch (Exception exception) {
                    ExceptionManager.manageException(exception);
                }
            } catch (DuplicatedNodeException e) {
                JOptionPane.showMessageDialog(this, "Name already exists", "New connection: Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void newGroup() {
        CleverTreeNode parent = findParentGroup();
        NewGroupDialog dialog = new NewGroupDialog(parentPanel.getFrame(), parent);
        if (dialog.isValidated()) {
            String name = dialog.getNameText().getText();
            HashMap<String, NodeProperty> properties = new LinkedHashMap<>();
            properties.put("name", new NodeProperty("name", name, true));
            properties.put("type", new NodeProperty("type", "group"));
            properties.put("description", new NodeProperty("description", name));
            properties.put("icon", new NodeProperty("icon", DataPath.IMG_16_FOLDER, true));
            properties.put("color", new NodeProperty("color", dialog.getColor()));
            CleverTreeNode toAdd = new CleverTreeNode(properties);
            try {
                parent.addChild(toAdd, true, new ConnectionNodesComparator());
                tree.setSelectionPath(new TreePath(toAdd.getPath()));
                tree.updateUI();
                try {
                    parentPanel.getFrame().saveXml(tree);
                } catch (Exception exception) {
                    ExceptionManager.manageException(exception);
                }
            } catch (DuplicatedNodeException e) {
                JOptionPane.showMessageDialog(this, "Name already exists", "New connection: Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private CleverTreeNode findParentGroup() {
        CleverTreeNode toReturn = tree.getSelectedNode();
        if (toReturn == null || toReturn.getParent() == null) {
            toReturn = tree.getRoot();
        } else {
            while (!toReturn.getPropertyValue("type").equals("group")) {
                toReturn = (CleverTreeNode) toReturn.getParent();
            }
        }
        return toReturn;
    }

    private void delete() {
        CleverTreeNode selectedNode = tree.getSelectedNode();
        if (selectedNode != null) {
            if (selectedNode != tree.getRoot()) {
                int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + selectedNode.getName() + "?", "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    ((CleverTreeNode) selectedNode.getParent()).select();
                    selectedNode.remove();
                    tree.updateUI();
                    try {
                        parentPanel.getFrame().saveXml(tree);
                    } catch (Exception e) {
                        ExceptionManager.manageException(e);
                    }
                }
            }
        }
    }

    private void sshGroup(List<GroupedTabPanel> panelGroup, CleverTreeNode selectedNode, String color) {
        for (int i = 0; i < selectedNode.getChildCount(); i++) {
            CleverTreeNode node = (CleverTreeNode) selectedNode.getChildAt(i);
            if (node.getPropertyValue("type").equals("group")) {
                sshGroup(panelGroup, node, color);
            } else if (node.getPropertyValue("type").equals("connection")) {
                sshSingle(panelGroup, node, color);
            }
        }
    }

    private void sftpGroup(List<GroupedTabPanel> panelGroup, CleverTreeNode selectedNode, String color, String wd) {
        for (int i = 0; i < selectedNode.getChildCount(); i++) {
            CleverTreeNode node = (CleverTreeNode) selectedNode.getChildAt(i);
            if (node.getPropertyValue("type").equals("group")) {
                sftpGroup(panelGroup, node, color, wd);
            } else if (node.getPropertyValue("type").equals("connection")) {
                sftpSingle(panelGroup, node, color, wd);
            }
        }
    }

    private void sshSingle(final List<GroupedTabPanel> group, final CleverTreeNode node, final String color) {
        final ConnectionManager connectionThread = new ConnectionManager(parentPanel.getFrame(), node);
        connectionThread.start();
        new Thread(() -> {
            while (connectionThread.getResult() == null) {
                try {
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
            }
            if (connectionThread.getResult()) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        final SshPanel sshPanel = new SshPanel(parentPanel, group, connectionThread.getConnection(), node);
                        parentPanel.addTab(node.getName(), sshPanel);
                        int i = parentPanel.getTabCount() - 1;
                        JLabel label = new JLabel(node.getName(), ViewUtils.createImageIcon(DataPath.IMG_16_TERMINAL), JLabel.LEFT);
                        parentPanel.setTabComponentAt(i, label);
                        if (!color.equals("default")) {
                            label.setOpaque(true);
                            label.setBackground(DataUtils.hexToColor(color));
                        } else {
                            label.setOpaque(false);
                        }
                        node.setIcon(DataPath.IMG_16_TERMINAL_ON, false);
                        group.add(sshPanel);
                        parentPanel.setSelectedIndex(parentPanel.getTabCount() - 1);
                        SwingUtilities.invokeLater(() -> parentPanel.updateUI());
                    } catch (Exception e) {
                        ExceptionManager.manageException(e);
                    }
                });
            }
        }).start();
    }

    private void sftpSingle(final List<GroupedTabPanel> group, final CleverTreeNode node, final String color, final String wd) {
        final ConnectionManager connectionThread = new ConnectionManager(parentPanel.getFrame(), node);
        connectionThread.start();
        new Thread(() -> {
            while (connectionThread.getResult() == null) {
                try {
                    Thread.sleep(100);
                } catch (Exception ignored) {
                }
            }
            if (connectionThread.getResult()) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        SftpPanel sftpPanel = new SftpPanel(parentPanel, group, connectionThread.getConnection(), node, wd);
                        parentPanel.addTab(node.getName(), sftpPanel);
                        int i = parentPanel.getTabCount() - 1;
                        JLabel label = new JLabel(node.getName(), ViewUtils.createImageIcon(DataPath.IMG_16_FOLDERS_EXPLORER), JLabel.LEFT);
                        parentPanel.setTabComponentAt(i, label);
                        if (!color.equals("default")) {
                            label.setOpaque(true);
                            label.setBackground(DataUtils.hexToColor(color));
                        } else {
                            label.setOpaque(false);
                        }
                        node.setIcon(DataPath.IMG_16_TERMINAL_ON, false);
                        group.add(sftpPanel);
                        parentPanel.setSelectedIndex(parentPanel.getTabCount() - 1);
                        parentPanel.updateUI();
                    } catch (Exception e) {
                        ExceptionManager.manageException(e);
                    }
                });
            }
        }).start();
    }

    public CleverTree getTree() {
        return tree;
    }

    public void setTree(CleverTree tree) {
        this.tree = tree;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        CleverTreeNode selectedNode = tree.getSelectedNode();
        if (selectedNode != null) {
            String type = selectedNode.getPropertyValue("type");
            if (type != null) {
                if (selectedNode == tree.getRoot()) {
                    connectNone();
                } else if (type.equals("connection")) {
                    connectSingle();
                } else if (type.equals("group")) {
                    connectGroup();
                } else {
                    connectNone();
                }
            }
        } else {
            connectNone();
        }
    }


    private void connectSingle() {
        connectionType = "single";
        buttonSsh.setIcon(ViewUtils.createImageIcon(DataPath.IMG_24_TERMINAL));
        buttonSsh.setEnabled(true);
        buttonSftp.setIcon(ViewUtils.createImageIcon(DataPath.IMG_24_FOLDERS_EXPLORER));
        buttonSftp.setEnabled(true);
        buttonDelete.setEnabled(true);
    }

    private void connectGroup() {
        connectionType = "group";
        buttonSsh.setIcon(ViewUtils.createImageIcon(DataPath.IMG_24_TERMINAL_GROUP));
        buttonSsh.setEnabled(true);
        buttonSftp.setIcon(ViewUtils.createImageIcon(DataPath.IMG_24_FOLDERS_EXPLORER));
        buttonSftp.setEnabled(true);
        buttonDelete.setEnabled(true);
    }

    private void connectNone() {
        connectionType = "none";
        buttonSsh.setIcon(ViewUtils.createImageIcon(DataPath.IMG_24_TERMINAL));
        buttonSsh.setEnabled(false);
        buttonSftp.setIcon(ViewUtils.createImageIcon(DataPath.IMG_24_FOLDERS_EXPLORER));
        buttonSftp.setEnabled(false);
        buttonDelete.setEnabled(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == tree && e.getClickCount() >= 2) {
            if (connectionType.equals("single")) {
                ssh(tree.getSelectedNode());
            }
        }
    }

    private void ssh(CleverTreeNode node) {
        if (connectionType.equals("single")) {
            List<GroupedTabPanel> list = new ArrayList<>();
            sshSingle(list, node, node.getPropertyValue("color"));
            parentPanel.getPanelGroups().add(list);
        } else if (connectionType.equals("group")) {
            List<GroupedTabPanel> list = new ArrayList<>();
            sshGroup(list, node, node.getPropertyValue("color"));
            parentPanel.getPanelGroups().add(list);
        }
    }

    public void sftp(CleverTreeNode node, String wd) {
        if (connectionType.equals("single")) {
            List<GroupedTabPanel> list = new ArrayList<>();
            sftpSingle(list, node, node.getPropertyValue("color"), wd);
            parentPanel.getPanelGroups().add(list);
        } else if (connectionType.equals("group")) {
            List<GroupedTabPanel> list = new ArrayList<>();
            sftpGroup(list, node, node.getPropertyValue("color"), wd);
            parentPanel.getPanelGroups().add(list);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void focus() {
        tree.requestFocus();
    }
}
