package org.bklab.sftp.view.panel;

import org.bklab.clevertree.node.CleverTreeNode;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.dto.NetworkQueueElement;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.panel.sftp.LocalFileSystemPanel;
import org.bklab.sftp.view.panel.sftp.NetworkQueuePanel;
import org.bklab.sftp.view.panel.sftp.RemoteFileSystemPanel;
import org.bklab.sftp.view.tab.ConnectionTabbedPane;
import org.bklab.sftp.view.tab.GroupedTabPanel;
import org.bklab.sftp.view.tab.TabPanel;
import org.bklab.ssh2.Connection;
import org.bklab.ssh2.SFTPv3Client;
import org.bklab.ssh2.SFTPv3DirectoryEntry;
import org.bklab.ssh2.SFTPv3FileAttributes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Broderick
 */
public class SftpPanel extends GroupedTabPanel implements ActionListener {
    public static final int ROW_HEIGHT = 22;
    private static final long serialVersionUID = 8054494868359029232L;
    private LocalFileSystemPanel panelLocal;
    private RemoteFileSystemPanel panelRemote;
    private SFTPv3Client client;
    private JToggleButton buttonNewtworkQueue;
    private NetworkQueuePanel panelNetworkQueue;

    SftpPanel(ConnectionTabbedPane parentPanel, List<GroupedTabPanel> group, Connection connection, CleverTreeNode node, String wd) throws IOException {
        super(TabPanel.TYPE_SFTP, parentPanel, group, node, connection);
        setOpaque(false);
        client = new SFTPv3Client(connection);
        panelLocal = new LocalFileSystemPanel(this);
        panelRemote = new RemoteFileSystemPanel(this, client, wd);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelLocal, panelRemote);
        split.setOpaque(false);
        setLayout(new BorderLayout());
        add(split, BorderLayout.CENTER);
        JToolBar toolbar = new JToolBar();
        toolbar.setOpaque(false);
        toolbar.setFloatable(false);

        buttonNewtworkQueue = new JToggleButton();
        buttonNewtworkQueue.setOpaque(false);
        buttonNewtworkQueue.setToolTipText("Network queue");
        buttonNewtworkQueue.setActionCommand("networkQueue");
        buttonNewtworkQueue.addActionListener(this);

        JButton buttonDownload = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_LEFT));
        buttonDownload.setOpaque(false);
        buttonDownload.setToolTipText("Download selected items");
        buttonDownload.setActionCommand("download");
        buttonDownload.addActionListener(this);

        JButton buttonUpload = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_RIGHT));
        buttonUpload.setOpaque(false);
        buttonUpload.setToolTipText("Upload selected items");
        buttonUpload.setActionCommand("upload");
        buttonUpload.addActionListener(this);

        JButton buttonUploadGroup = new JButton(ViewUtils.createImageIcon(DataPath.IMG_24_RIGHT_GROUP));
        buttonUploadGroup.setOpaque(false);
        buttonUploadGroup.setToolTipText("Upload selected items into group");
        buttonUploadGroup.setActionCommand("uploadGroup");
        buttonUploadGroup.addActionListener(this);

        toolbar.add(buttonNewtworkQueue);
        toolbar.addSeparator();
        toolbar.add(buttonDownload);
        toolbar.add(buttonUpload);
        toolbar.add(buttonUploadGroup);
        add(toolbar, BorderLayout.NORTH);

        panelNetworkQueue = new NetworkQueuePanel(client, this);
        split.setDividerLocation((int) (((double) parentPanel.getWidth()) / 5.0) * 2);
    }

    @Override
    public void close() {
        panelNetworkQueue.requestStop();
        while (panelNetworkQueue.getQueueStatus().equals(NetworkQueuePanel.STATUS_UPLOADING) || panelNetworkQueue.getQueueStatus().equals(NetworkQueuePanel.STATUS_DOWNLOADING)) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ignored) {
            }
            //System.out.println("STATUS "+panelNetworkQueue.getQueueStatus());
        }
        try {
            client.close();
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
    public void focus() {

    }

    private void download(String fromWorkingDirectory, SFTPv3DirectoryEntry from, File to) {
        NetworkQueueElement element = new NetworkQueueElement(NetworkQueueElement.OPERATION_DOWNLOAD, to, from, fromWorkingDirectory);
        panelNetworkQueue.addToQueue(element);
        showNetworkQueue();
    }

    public void upload(File from) {
        upload(from, panelRemote.getWd());
    }

    private void upload(File from, String to) {
        NetworkQueueElement element = new NetworkQueueElement(NetworkQueueElement.OPERATION_UPLOAD, from, null, to);
        panelNetworkQueue.addToQueue(element);
        showNetworkQueue();
    }

    private void groupUpload(File from, String to) {
        NetworkQueueElement element = new NetworkQueueElement(NetworkQueueElement.OPERATION_UPLOAD, from, null, to);
        try {
            SFTPv3FileAttributes attributes = client.lstat(to);
            if (attributes.isDirectory()) {
                panelNetworkQueue.addToQueue(element);
                showNetworkQueue();
            }
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        switch (actionCommand) {
            case "download": {
                List<SFTPv3DirectoryEntry> files = panelRemote.getSelectedFile();
                for (SFTPv3DirectoryEntry file : files) {
                    download(panelRemote.getWd(), file, panelLocal.getWd());
                }
                break;
            }
            case "upload": {
                List<File> files = panelLocal.getSelectedFile();
                for (File file : files) {
                    upload(file, panelRemote.getWd());
                }
                break;
            }
            case "uploadGroup": {
                List<File> files = panelLocal.getSelectedFile();
                for (GroupedTabPanel panel : group) {
                    if (panel.getType().equals(TabPanel.TYPE_SFTP)) {
                        SftpPanel sftpPanel = (SftpPanel) panel;
                        for (File file : files) {
                            sftpPanel.groupUpload(file, panelRemote.getWd());
                        }
                    }
                }
                break;
            }
            case "networkQueue":
                if (buttonNewtworkQueue.isSelected()) {
                    showNetworkQueue();
                } else {
                    hideNetworkQueue();
                }
                break;
        }
    }

    private void hideNetworkQueue() {
        remove(panelNetworkQueue);
        buttonNewtworkQueue.setSelected(false);
        updateUI();
    }

    private void showNetworkQueue() {
        add(panelNetworkQueue, BorderLayout.SOUTH);
        buttonNewtworkQueue.setSelected(true);
        updateUI();
    }

    public LocalFileSystemPanel getPanelLocal() {
        return panelLocal;
    }

    public RemoteFileSystemPanel getPanelRemote() {
        return panelRemote;
    }

    public void download(SFTPv3DirectoryEntry entry) {
        download(panelRemote.getWd(), entry, panelLocal.getWd());
    }
}
