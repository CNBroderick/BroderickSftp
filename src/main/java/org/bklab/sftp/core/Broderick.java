package org.bklab.sftp.core;

import net.miginfocom.swing.MigLayout;
import org.bklab.clevertree.dto.NodeProperty;
import org.bklab.clevertree.dto.SecurityData;
import org.bklab.clevertree.exception.SecurityException;
import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.clevertree.tree.CleverTree;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.configuration.Settings;
import org.bklab.sftp.locale.i18n;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.panel.ConnectionsPanel;
import org.bklab.sftp.view.tab.ConnectionTabbedPane;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project Name: Broderick Sftp -- Broderick Labs
 * Project Version: 1.0.0 release
 * Project creators: Broderick Labs --bkLab.org, broderick.cn, bz8.org
 * FeedBack E-mail: labs@broderick.cn, z@broderick.cn, z@bklab.org, z@bz8.org
 * Project Start Time: Wednesday August 09, 2017 CST
 * Project Finished Time:
 * version: 0.1.0 Saturday September 30, 2017 CST
 * Update List:
 * 1. Empty Broderick Sftp Client frame is built via Java swing;
 * 2. Broderick SFTP Server is built, using for manage Broderick ECS(Qingdao)'s Tomcat Web Server Resources;
 * 3. Login Function can be used.
 * <p>
 * version: 0.2.0 Friday October 20, 2017 CST
 * Update List:
 * 1. File translates button can be used.
 * <p>
 * version: 0.3.0 Wednesday November 08, 2017 CST
 * Update List:
 * 1. CleverTree Developed!
 * 2. Broderick Sftp File Explorer(B S F E) with GUI Developed!
 * 3. B S F E can be used via clever tree.
 * <p>
 * version: 0.4.0 Thursday November 30, 2017 CST
 * Update List:
 * 1. VT100 Text Area is built;
 * 2. Sftp Console is built and it can be selected via VT100.
 * Known issue:
 * 1. VT100 Text Area only support ACSii.
 * <p>
 * version: 0.5.0 Sunday The Last Of 2017 CST
 * Update List:
 * The main framework is basically completed.
 * <p>
 * version: 0.6.0 Thursday January 25, 2018 CST
 * Update List:
 * The main function is basically completed via ssh2 assistant.
 * <p>
 * version: 1.0.0 Wednesday February 14, 2018 CST
 * Update List:
 * Configuration, Connection etc. very important files is  IS ENCRYPTED!!!
 * <p>
 * version: 1.1.0 Thursday February 27, 2018 CST
 * Update List:
 * Fix a lots of problems;
 * <p>
 * Known problems:
 * There will be a protocol error when SFTP communication via command;
 *
 * @author Broderick
 */

public class Broderick extends JFrame implements WindowListener {
    public static final String PROGRAM_NAME = i18n.programName;
    public static final String PROGRAM_NAME_SHORT = i18n.programNameShort;
    public static final String PROGRAM_VERSION = "1.1.0";
    private static final long serialVersionUID = 1631020594944116850L;
    private static final String PWD_VECTOR = "bklab.org";

    private String password;

    private File connectionsFile;

    private ConnectionTabbedPane tabs;
    private ConnectionsPanel panelConnections;

    public Broderick() throws Exception {
        super();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
        List<Image> icons;
        File[] iconFiles = new File(DataPath.DATA_ICON).listFiles(file -> file.isFile() && file.getName().endsWith(".png"));
        icons = Arrays.stream(iconFiles).map(iconFile -> ViewUtils.createImage(DataPath.DATA_ICON + iconFile.getName())).collect(Collectors.toList());
        setIconImages(icons);
        String connectionsDir = Settings.get("connectionsDir");
        if (connectionsDir == null || connectionsDir.trim().equals("")
                || new File(connectionsDir + "/connections.xml").exists() == false) {
            connectionsDir = null;
            if (new File("connections/connections.xml").exists()) {
                int answer = JOptionPane.showConfirmDialog(null,
                        i18n.useDefaultConnection,
                        PROGRAM_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    connectionsDir = "connections";
                }
            }
            if (connectionsDir == null) {
                int answer = JOptionPane.showConfirmDialog(null,
                        i18n.useDefaultConnectionFolderPath, PROGRAM_NAME,
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    connectionsDir = "connections";
                } else {
                    JFileChooser connectionsDirChooser = new JFileChooser("connections");
                    connectionsDirChooser.setDialogTitle(i18n.chooseConnectionFolder);
                    connectionsDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    connectionsDirChooser.setAcceptAllFileFilterUsed(false);
                    connectionsDirChooser.setMultiSelectionEnabled(false);
                    connectionsDirChooser.setFileFilter(new FileFilter() {
                        @Override
                        public String getDescription() {
                            return "Connections folder";
                        }

                        @Override
                        public boolean accept(File f) {
                            return f.isDirectory();
                        }
                    });
                    answer = connectionsDirChooser.showOpenDialog(null);
                    if (answer == JFileChooser.APPROVE_OPTION) {
                        connectionsDir = connectionsDirChooser.getSelectedFile().getAbsolutePath();
                    } else {
                        connectionsDir = "connections";
                        JOptionPane.showMessageDialog(null,
                                i18n.connectionFolderSetDefaultPath,
                                PROGRAM_NAME, JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
            Settings.addProperty("connectionsDir", connectionsDir);
        }
        connectionsFile = new File(connectionsDir + "/connections.xml");
        tabs = new ConnectionTabbedPane(this);
        CleverTree tree;
        try {
            if (!connectionsFile.exists()) {
                password = Settings.get("password");
                if (password == null || password.trim().equals("")) {
                    JPanel panel = new JPanel(new MigLayout("wrap 1"));
                    final JPasswordField pwd = new JPasswordField(20);
                    pwd.addAncestorListener(new AncestorListener() {
                        @Override
                        public void ancestorRemoved(AncestorEvent event) {
                        }

                        @Override
                        public void ancestorMoved(AncestorEvent event) {
                        }

                        @Override
                        public void ancestorAdded(AncestorEvent event) {
                            pwd.requestFocus();
                        }
                    });
                    panel.add(new JLabel(i18n.inputConnectionFolderPassword));
                    panel.add(pwd);
                    JOptionPane.showMessageDialog(this, panel, PROGRAM_NAME, JOptionPane.QUESTION_MESSAGE);
                    password = new String(pwd.getPassword());
                    if (password.equals("")) {
                        throw new SecurityException(null);
                    }
                    JPanel panel2 = new JPanel(new MigLayout("wrap 1"));
                    final JPasswordField pwd2 = new JPasswordField(20);
                    pwd2.addAncestorListener(new AncestorListener() {
                        @Override
                        public void ancestorRemoved(AncestorEvent event) {
                        }

                        @Override
                        public void ancestorMoved(AncestorEvent event) {
                        }

                        @Override
                        public void ancestorAdded(AncestorEvent event) {
                            pwd2.requestFocus();
                        }
                    });
                    panel2.add(new JLabel(i18n.reinputConnectionFolderPassword));
                    panel2.add(pwd2);
                    JOptionPane.showMessageDialog(this, panel2, PROGRAM_NAME, JOptionPane.QUESTION_MESSAGE);
                    String password2 = new String(pwd2.getPassword());
                    if (password2.equals("") || password.equals(password2) == false) {
                        throw new SecurityException(null);
                    }
                }
                CleverTreeNode root = new CleverTreeNode("Connections");
                root.setIcon(DataPath.IMG_16_CONNECTIONS, true);
                root.setProperty(new NodeProperty("type", "group"));
                tree = new CleverTree(root);
                saveXml(tree);
            } else {
                password = Settings.get("password");
                if (password == null || password.trim().equals("")) {
                    JPanel panel = new JPanel(new MigLayout("wrap 1"));
                    final JPasswordField pwd = new JPasswordField(20);
                    pwd.addAncestorListener(new AncestorListener() {
                        @Override
                        public void ancestorRemoved(AncestorEvent event) {
                        }

                        @Override
                        public void ancestorMoved(AncestorEvent event) {
                        }

                        @Override
                        public void ancestorAdded(AncestorEvent event) {
                            pwd.requestFocus();
                        }
                    });
                    panel.add(new JLabel(i18n.inputPasswordForConnectionFile));
                    panel.add(pwd);
                    JOptionPane.showMessageDialog(this, panel, PROGRAM_NAME, JOptionPane.QUESTION_MESSAGE);
                    password = new String(pwd.getPassword());
                    if (password.equals("")) {
                        throw new SecurityException(null);
                    }
                }
                tree = CleverTree.loadFromXML(connectionsFile, new SecurityData(PWD_VECTOR, password));
                tree.getRoot().expand();
            }
        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(this, i18n.incorrectPassword, PROGRAM_NAME, JOptionPane.WARNING_MESSAGE);
            throw e;
        }
        panelConnections = new ConnectionsPanel(tabs, tree);
        addWindowListener(this);
        setTitle(PROGRAM_NAME + " " + PROGRAM_VERSION);
        setLayout(new BorderLayout());
        setSize(Settings.getInteger("defaultWidth"), Settings.getInteger("defaultHeight"));
        setLocationRelativeTo(null);
        if (Settings.getBoolean("startMaximized")) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        tabs.addTab("Connections", ViewUtils.createImageIcon(DataPath.IMG_16_CONNECTIONS), panelConnections);
        add(tabs, BorderLayout.CENTER);
        setVisible(true);
        panelConnections.focus();
    }

    public static void main(String[] args) {
        try {
            new Broderick();
        } catch (SecurityException e) {
            System.exit(0);
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        while (tabs.getTabCount() > 1) {
            tabs.getComponentAt(1).close();
        }
        dispose();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    public void saveXml(CleverTree tree) throws Exception {
        tree.saveAsXML(connectionsFile, new SecurityData(PWD_VECTOR, password));
    }

    public ConnectionsPanel getConnectionsPanel() {
        return panelConnections;
    }

}
