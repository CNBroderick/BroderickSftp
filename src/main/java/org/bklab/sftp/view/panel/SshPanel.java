package org.bklab.sftp.view.panel;

import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.panel.ssh.CommandPanel;
import org.bklab.sftp.view.tab.ConnectionTabbedPane;
import org.bklab.sftp.view.tab.GroupedTabPanel;
import org.bklab.sftp.view.tab.TabPanel;
import org.bklab.sftp.view.widget.VT100TextArea;
import org.bklab.ssh2.ChannelCondition;
import org.bklab.ssh2.Connection;
import org.bklab.ssh2.Session;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Broderick
 */
public class SshPanel extends GroupedTabPanel implements ActionListener {
    private static final long serialVersionUID = 1153764214131263313L;
    private VT100TextArea terminalArea;

    private InputStream in;
    private OutputStream out;

    private SessionMonitor localMonitor;
    private JToggleButton buttonCommand;
    private CommandPanel panelCommand;
    private JScrollBar scrollbarVertical;

    SshPanel(ConnectionTabbedPane parentPanel, List<GroupedTabPanel> group, Connection connection, CleverTreeNode node) throws Exception {
        super(TabPanel.TYPE_SSH, parentPanel, group, node, connection);
        this.session = connection.openSession();
        setLayout(new BorderLayout());
        setOpaque(false);

        JToolBar toolbar = new JToolBar();
        toolbar.setOpaque(false);
        toolbar.setFloatable(false);

        buttonCommand = new JToggleButton(ViewUtils.createImageIcon(DataPath.IMG_24_GEARS));
        buttonCommand.setOpaque(false);
        buttonCommand.setToolTipText("Execute command");
        buttonCommand.setActionCommand("command");
        buttonCommand.addActionListener(this);
        toolbar.add(buttonCommand);

        JButton buttonSftp = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_FOLDERS_EXPLORER));
        buttonSftp.setOpaque(false);
        buttonSftp.setToolTipText("Open SFTP");
        buttonSftp.setActionCommand("sftp");
        buttonSftp.addActionListener(this);
        toolbar.add(buttonSftp);

        panelCommand = new CommandPanel(this);

        add(toolbar, BorderLayout.NORTH);

        localMonitor = new SessionMonitor(this, session);
        localMonitor.start();
        int x = (int) (((double) parentPanel.getWidth() - 20) / 8.0);
        int y = (int) (((double) parentPanel.getHeight() - 80) / 20.0);
        session.requestPTY("vt100", x, y, 0, 0, null);
        session.startShell();
        in = session.getStdout();
        out = session.getStdin();
        terminalArea = new VT100TextArea(y, x, out);
        JScrollPane scrollPane = new JScrollPane(terminalArea);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(new MatteBorder(0, 0, 0, 0, Color.BLACK));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollbarVertical = scrollPane.getVerticalScrollBar();
        add(scrollPane, BorderLayout.CENTER);
        RemoteConsumer remoteConsumer = new RemoteConsumer();
        remoteConsumer.start();

        String commandFilePath = node.getPropertyValue("commands");
        if (commandFilePath != null) {
            File commandFile = new File(commandFilePath);
            if (commandFile.exists() && commandFile.isFile()) {
                command(commandFile, false);
            } else {
                JOptionPane.showMessageDialog(null, "Custom commands file not found:\n" + commandFilePath, "SSH Connection", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private OutputStream getOut() {
        return out;
    }

    @Override
    public void close() {
        localMonitor.mustClose = false;
        try {
            session.close();
        } catch (Exception ignored) {
        }
        try {
            connection.close();
        } catch (Exception ignored) {
        }
        parentPanel.remove(this);
        parentPanel.dispose(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (actionCommand.equals("command")) {
            if (buttonCommand.isSelected()) {
                showCommand();
//				for(GroupedTabPanel panel:getGroup()){
//					((SshPanel)panel).showCommand();
//				}
            } else {
                removeCommand();
//				for(GroupedTabPanel panel:getGroup()){
//					((SshPanel)panel).removeCommand();
//				}
            }
            updateUI();

        } else if (actionCommand.equals("sftp")) {
            parentPanel.getFrame().getConnectionsPanel().sftp(node, "/");
        }
    }

    private void showCommand() {
        buttonCommand.setSelected(true);
        add(panelCommand, BorderLayout.SOUTH);
        updateUI();
        SwingUtilities.invokeLater(() -> scrollbarVertical.setValue(scrollbarVertical.getMaximum()));
    }

    private void removeCommand() {
        buttonCommand.setSelected(false);
        remove(panelCommand);
        updateUI();
        SwingUtilities.invokeLater(() -> scrollbarVertical.setValue(scrollbarVertical.getMaximum()));
    }

    public void focus() {
        terminalArea.requestFocus();
    }

    private void command(File file, boolean executeOnGroup) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                command(line, executeOnGroup);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException exception) {
            ExceptionManager.manageException(exception);
        }
    }

    public void command(String command, boolean executeOnGroup) {
        try {
            ArrayList<String> commandToExecute = new ArrayList<>();
            if (command.contains("{[(") && command.contains(")]}")) {
                String preCommand = command.substring(0, command.indexOf("{[("));
                String parameters = command.substring(command.indexOf("{[(") + 3, command.lastIndexOf(")]}"));
                String postCommand = command.substring(command.lastIndexOf(")]}") + 3);
                String[] values;
                if (parameters.startsWith("{") && parameters.endsWith("}")) {
                    parameters = parameters.substring(1, parameters.length() - 1);
                    values = parameters.split(",");
                    for (String value : values) commandToExecute.add(preCommand + value + postCommand);
                } else if (parameters.startsWith("pause ")) {
                    values = parameters.split(" ");
                    if (values.length > 0) {
                        if (DataUtils.isInteger(values[1])) {
                            try {
                                Thread.sleep(Integer.parseInt(values[1]));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } else {
                commandToExecute.add(command);
            }
            if (commandToExecute.size() > 0) {
                if (executeOnGroup) {
                    int i = 0;
                    for (GroupedTabPanel panel : getGroup()) {
                        ((SshPanel) panel).getOut().write((commandToExecute.get(i % commandToExecute.size()) + "\n").getBytes());
                        i++;
                    }
                } else {
                    getOut().write((commandToExecute.get(0) + "\n").getBytes());
                }
            }
        } catch (Exception e) {
            ExceptionManager.manageException(e);
        }
    }

    private class RemoteConsumer extends Thread {
        RemoteConsumer() {
            super();
        }

        public void run() {
            boolean running = true;
            byte[] buff = new byte[8192];
            try {
                while (running) {
                    int len = in.read(buff);
                    if (len == -1) {
                        running = false;
                    } else {
                        addText(buff, len);
                    }
                }
            } catch (Exception e) {
                ExceptionManager.manageException(e);
            }
        }

        private void addText(byte[] data, int offset) {
            terminalArea.setData(data, offset);
            scrollbarVertical.setValue(scrollbarVertical.getMaximum());
        }
    }

    private class SessionMonitor extends Thread {
        private TabPanel connectionTab;
        private Session session;
        private boolean mustClose;

        SessionMonitor(TabPanel connectionTab, Session session) {
            super();
            this.connectionTab = connectionTab;
            this.session = session;
            this.mustClose = true;
        }

        @Override
        public void run() {
            session.waitForCondition(ChannelCondition.EOF, 0);
            if (mustClose) {
                connectionTab.close();
            }
        }
    }
}
