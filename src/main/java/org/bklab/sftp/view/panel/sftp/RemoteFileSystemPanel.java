package org.bklab.sftp.view.panel.sftp;

import net.miginfocom.swing.MigLayout;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.dialog.DeleteDialog;
import org.bklab.sftp.view.dialog.NewFolderDialog;
import org.bklab.sftp.view.dialog.RenameDialog;
import org.bklab.sftp.view.drop.SftpTransferHandler;
import org.bklab.sftp.view.panel.SftpPanel;
import org.bklab.sftp.view.renderer.RemoteFileCellRenderer;
import org.bklab.sftp.view.rowsorter.RemoteFileSystemTableRowSorter;
import org.bklab.sftp.view.tablemodel.RemoteFileSystemTableModel;
import org.bklab.ssh2.SFTPException;
import org.bklab.ssh2.SFTPv3Client;
import org.bklab.ssh2.SFTPv3DirectoryEntry;
import org.bklab.ssh2.SFTPv3FileAttributes;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * @author Broderick
 */
public class RemoteFileSystemPanel extends JPanel implements ActionListener, MouseListener, KeyListener {
    private static final long serialVersionUID = 6692406117494277792L;

    private SFTPv3Client client;
    private SftpPanel sftpPanel;
    private String wd;
    private JTable tableFiles;
    private RemoteFileSystemTableModel tableModel;
    private RemoteFileSystemTableRowSorter tableSorter;
    private JTextField textPath;
    private JLabel labelStatus;
    private long lastKeyMs;
    private String searchString;

    public RemoteFileSystemPanel(SftpPanel sftpPanel, SFTPv3Client client, String wd) {
        super();
        this.sftpPanel = sftpPanel;
        this.client = client;
        this.wd = wd;
        if (this.wd == null) {
            this.wd = "/";
        }
        setOpaque(false);
        setLayout(new BorderLayout());
        tableModel = new RemoteFileSystemTableModel();
        tableSorter = new RemoteFileSystemTableRowSorter(tableModel, this);
        tableFiles = new JTable(tableModel);
        tableFiles.setDefaultRenderer(Object.class, new RemoteFileCellRenderer(this));
        tableFiles.getTableHeader().setReorderingAllowed(false);
        tableFiles.setRowSorter(tableSorter);
        tableFiles.setRowHeight(18);
        tableFiles.setShowGrid(false);
        tableFiles.addMouseListener(this);
        tableFiles.addKeyListener(this);
        tableFiles.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableFiles.getColumnModel().getColumn(1).setMaxWidth(80);
        tableFiles.setDropMode(DropMode.ON);
        tableFiles.setTransferHandler(new SftpTransferHandler(sftpPanel));
        //tableFiles.setDragEnabled(true);
        JScrollPane scrollTable = new JScrollPane(tableFiles);
        labelStatus = new JLabel(" ");
        add(scrollTable, BorderLayout.CENTER);
        textPath = new JTextField();
        textPath.addKeyListener(this);
        JComboBox<String> comboHidden = new JComboBox<>(new String[]{"hidden"});
        comboHidden.setVisible(false);
        comboHidden.setPreferredSize(new Dimension(10, SftpPanel.ROW_HEIGHT));

        JButton buttonLevelUp = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER_LEVEL_UP));
        buttonLevelUp.addActionListener(this);
        buttonLevelUp.setActionCommand("levelUp");

        JButton buttonRefresh = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_ARROW_REFRESH));
        buttonRefresh.setToolTipText("Refresh");
        buttonRefresh.setOpaque(false);
        buttonRefresh.setActionCommand("refresh");
        buttonRefresh.addActionListener(this);
        JButton buttonRoot = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER_ROOT));
        buttonRoot.setToolTipText("Go to root");
        buttonRoot.setOpaque(false);
        buttonRoot.setActionCommand("root");
        buttonRoot.addActionListener(this);
        JButton buttonNewFolder = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER_NEW));
        buttonNewFolder.setToolTipText("New folder");
        buttonNewFolder.setOpaque(false);
        buttonNewFolder.setActionCommand("newFolder");
        buttonNewFolder.addActionListener(this);
        JButton buttonDelete = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_DELETE));
        buttonDelete.setToolTipText("Delete");
        buttonDelete.setOpaque(false);
        buttonDelete.setActionCommand("delete");
        buttonDelete.addActionListener(this);
        JButton buttonRename = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_RENAME));
        buttonRename.setToolTipText("Rename");
        buttonRename.setOpaque(false);
        buttonRename.setActionCommand("rename");
        buttonRename.addActionListener(this);

        JToolBar topButtonToolbar = new JToolBar();
        topButtonToolbar.setBorder(new EmptyBorder(0, 0, 0, 0));
        topButtonToolbar.add(buttonRefresh, "width 28!");
        topButtonToolbar.add(buttonRoot, "width 28!");
        topButtonToolbar.add(buttonNewFolder, "width 28!");
        topButtonToolbar.add(buttonDelete, "width 28!");
        topButtonToolbar.add(buttonRename, "width 28!");

        JPanel topPanel = new JPanel(new MigLayout("wrap 2"));
        topPanel.add(topButtonToolbar, "span");
        topPanel.add(comboHidden, "span");
        topPanel.add(buttonLevelUp, "width 28!");
        topPanel.add(textPath, "width :4000:4000");
        add(topPanel, BorderLayout.NORTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

        bottomPanel.add(labelStatus, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        listFiles();
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();

        int columnIndexToSort = 0;
        sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));

        tableSorter.setSortKeys(sortKeys);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "levelUp":
                levelUp();
                break;
            case "refresh":
                listFiles();
                break;
            case "root":
                root();
                break;
            case "newFolder":
                newFolder();
                break;
            case "delete":
                delete();
                break;
            case "rename":
                rename();
                break;
        }
    }

    private void rename() {
        try {
            List<SFTPv3DirectoryEntry> selectedFiles = getSelectedFile();
            if (selectedFiles.size() > 0) {
                if (selectedFiles.size() == 1) {
                    SFTPv3DirectoryEntry toRename = selectedFiles.get(0);
                    RenameDialog dialog = new RenameDialog(toRename.filename);
                    if (dialog.isValidated()) {
                        @SuppressWarnings("unchecked")
                        Vector<SFTPv3DirectoryEntry> entries = client.ls(wd);
                        boolean alreadyExists = false;
                        String newName = dialog.getTextName().getText();
                        for (SFTPv3DirectoryEntry entry : entries) {
                            if (entry.filename.equals(newName)) {
                                alreadyExists = true;
                                break;
                            }
                        }
                        if (alreadyExists) {
                            JOptionPane.showMessageDialog(null, "Name already exists", "Rename", JOptionPane.WARNING_MESSAGE);
                        } else {
                            client.mv(wd + "/" + toRename.filename, wd + "/" + newName);
                            listFiles();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Please select only one item", "Rename", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select one item", "Rename", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
    }

    private void delete() {
        try {
            List<SFTPv3DirectoryEntry> entries = getSelectedFile();
            if (entries.size() > 0) {
                DeleteDialog dialog = new DeleteDialog();
                String answer = dialog.getAnswer();
                if (answer.equals(DeleteDialog.ANSWER_YES)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    for (SFTPv3DirectoryEntry entry : entries) {
                        deleteRecursive(wd, entry);
                    }
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    listFiles();
                }
            }
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
    }

    private void deleteRecursive(String cwd, SFTPv3DirectoryEntry entry) throws IOException {
        if (entry.attributes.isDirectory()) {
            @SuppressWarnings("unchecked")
            Vector<SFTPv3DirectoryEntry> entries = client.ls(cwd + "/" + entry.filename);
            for (SFTPv3DirectoryEntry e : entries) {
                if (!e.filename.equals(".") && !e.filename.equals("..")) {
                    deleteRecursive(cwd + "/" + entry.filename, e);
                }
            }
            try {
                client.rmdir(cwd + "/" + entry.filename);
            } catch (SFTPException e) {
                if (e.getServerErrorCode() == 3) {
                    JOptionPane.showMessageDialog(null, "Permission denied deleting directory " + cwd + "/" + entry.filename, "Delete", JOptionPane.WARNING_MESSAGE);
                } else {
                    throw e;
                }
            }
        } else if (entry.attributes.isRegularFile() || entry.attributes.isSymlink()) {
            try {
                client.rm(cwd + "/" + entry.filename);
            } catch (SFTPException e) {
                if (e.getServerErrorCode() == 3) {
                    JOptionPane.showMessageDialog(null, "Permission denied deleting file " + cwd + "/" + entry.filename, "Delete", JOptionPane.WARNING_MESSAGE);
                } else {
                    throw e;
                }
            }
        }
    }

    private void levelUp() {
        wd = wd.substring(0, wd.lastIndexOf("/"));
        if (wd.equals("")) {
            wd = "/";
        }
        listFiles();
    }

    private void root() {
        wd = "/";
        listFiles();
    }

    private void newFolder() {
        try {
            NewFolderDialog dialog = new NewFolderDialog();
            if (dialog.isValidated()) {
                boolean alreadyExists = false;
                String name = dialog.getTextName().getText();
                @SuppressWarnings("unchecked")
                Vector<SFTPv3DirectoryEntry> entries = client.ls(wd);
                for (SFTPv3DirectoryEntry entry : entries) {
                    if (entry.filename.equals(name)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (alreadyExists) {
                    JOptionPane.showMessageDialog(null, "Name already exists", "New folder", JOptionPane.WARNING_MESSAGE);
                } else {
                    String toCreate = wd + "/" + name;
                    client.mkdir(toCreate, 0755);
                    listFiles();
                }
            }
        } catch (SFTPException e) {
            if (e.getServerErrorCode() == 3) {
                JOptionPane.showMessageDialog(null, "Permission denined", "New folder", JOptionPane.WARNING_MESSAGE);
            } else {
                System.err.println("Server error code: " + e.getServerErrorCode());
                e.printStackTrace();
            }
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
    }

    void listFiles() {
        try {
            while (wd.length() > 1 && wd.endsWith("/")) {
                wd = wd.substring(0, wd.length() - 1);
            }
            textPath.setText(wd);
            @SuppressWarnings("unchecked")
            Vector<SFTPv3DirectoryEntry> entries = client.ls(wd);
            tableModel.setRowCount(0);
            int nFile = 0;
            int nDirectory = 0;
            int nSymlink = 0;
            int nTotal = 0;
            for (SFTPv3DirectoryEntry entry : entries) {
                if (!entry.filename.equals(".") && !entry.filename.equals("..")) {
                    nTotal++;
                    //System.out.print(entry.filename+" "+entry.attributes.permissions+" ");
                    if (entry.attributes.isDirectory()) {
                        nDirectory++;
                    } else if (entry.attributes.isRegularFile()) {
                        nFile++;
                    } else if (entry.attributes.isSymlink()) {
                        nSymlink++;
                    }
                    //System.out.println();
                    tableModel.addRow(new Object[]{entry, entry.attributes.isRegularFile() ? entry.attributes.size : Long.valueOf(0), new Date((long) (entry.attributes.mtime) * 1000L)});
                }
            }
            labelStatus.setText(" " + nTotal + " items (" + nDirectory + " directories, " + nFile + " files, " + nSymlink + " symbolic links)");
            tableSorter.sort();
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == tableFiles) {
            if (e.getClickCount() > 1) {
                explore();
            }
        }
    }

    private void explore() {
        SFTPv3DirectoryEntry entry = (SFTPv3DirectoryEntry) tableModel.getValueAt(tableFiles.convertRowIndexToModel(tableFiles.getSelectedRow()), 0);
        if (entry.attributes.isSymlink()) {
            String realPath = realLinkPath(entry);
            if (!realPath.startsWith("/")) {
                realPath = wd + "/" + realPath;
            }
            SFTPv3FileAttributes attributes = null;
            try {
                attributes = client.stat(realPath);
            } catch (IOException e) {
                ExceptionManager.manageException(e);
            }
            if (attributes != null) {
                if (attributes.isDirectory()) {
                    wd = realPath;
                    listFiles();
                }
            }
        } else if (entry.attributes.isDirectory()) {
            if (wd.equals("/")) {
                wd = "/" + entry.filename;
            } else {
                wd = wd + "/" + entry.filename;
            }
            listFiles();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == textPath) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                try {
                    client.ls(textPath.getText());
                    wd = textPath.getText();
                } catch (SFTPException exception) {
                    if (exception.getServerErrorCode() != 2) {
                        ExceptionManager.manageException(exception);
                    }
                } catch (IOException exception) {
                    ExceptionManager.manageException(exception);
                }
                listFiles();
            }
        } else if (e.getSource() == tableFiles) {
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                levelUp();
            } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (getSelectedFile().size() == 1) {
                    explore();
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_F2) {
                if (getSelectedFile().size() == 1) {
                    rename();
                    e.consume();
                }
            } else if (e.getKeyCode() == KeyEvent.VK_F5) {
                listFiles();
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_F6) {
                List<SFTPv3DirectoryEntry> files = getSelectedFile();
                for (SFTPv3DirectoryEntry file : files) {
                    sftpPanel.download(file);
                }
                e.consume();
            } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                delete();
                e.consume();
            } else if (DataUtils.isPrintableChar(e.getKeyChar())) {
                long time = System.currentTimeMillis();
                if ((time - lastKeyMs) > 1500) {
                    searchString = "";
                }
                lastKeyMs = time;
                searchString += e.getKeyChar();
                //System.out.println("Search for "+searchString);
                for (int i = 0; i < tableFiles.getRowCount(); i++) {
                    SFTPv3DirectoryEntry entry = ((SFTPv3DirectoryEntry) tableFiles.getValueAt(i, 0));
                    //System.out.println(i+" "+file.getName());
                    if (entry.filename.toLowerCase().startsWith(searchString.toLowerCase())) {
                        tableFiles.getSelectionModel().setSelectionInterval(i, i);
                        //System.out.println("Found "+i);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub

    }

    public List<SFTPv3DirectoryEntry> getSelectedFile() {
        List<SFTPv3DirectoryEntry> toReturn = new ArrayList<>();
        int[] rows = tableFiles.getSelectedRows();
        if (rows != null) {
            for (int row : rows) {
                toReturn.add((SFTPv3DirectoryEntry) (tableModel.getValueAt(tableFiles.convertRowIndexToModel(row), 0)));
            }
        }
        return toReturn;
    }

    public String getWd() {
        return wd;
    }

    public JTable getTableFiles() {
        return tableFiles;
    }

    private String realLinkPath(SFTPv3DirectoryEntry file) {
        String toReturn = null;
        try {
            toReturn = client.readLink(wd + "/" + file.filename);
        } catch (SFTPException e) {
            if (e.getServerErrorCode() != 5) {
                System.err.println("Server error code: " + e.getServerErrorCode());
                ExceptionManager.manageException(e);
            }
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
        return toReturn;
    }

    public SFTPv3FileAttributes remoteFileStat(String realPath) throws IOException {
        return client.stat(realPath);
    }

    public SFTPv3FileAttributes resolveLink(SFTPv3DirectoryEntry entry) {
        SFTPv3FileAttributes toReturn = null;
        try {
            String path = client.readLink(wd + "/" + entry.filename);
            if (!path.startsWith("/")) {
                path = wd + "/" + path;
            }
            toReturn = client.stat(path);
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
        return toReturn;
    }
}
