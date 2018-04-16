package org.bklab.sftp.view.panel.sftp;

import net.miginfocom.swing.MigLayout;
import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.utils.WindowsShortcut;
import org.bklab.sftp.view.dialog.DeleteDialog;
import org.bklab.sftp.view.dialog.NewFolderDialog;
import org.bklab.sftp.view.dialog.RenameDialog;
import org.bklab.sftp.view.panel.SftpPanel;
import org.bklab.sftp.view.renderer.LocalFileCellRenderer;
import org.bklab.sftp.view.renderer.LocalFileSystemRootRenderer;
import org.bklab.sftp.view.rowsorter.LocalFileSystemTableRowSorter;
import org.bklab.sftp.view.tablemodel.LocalFileSystemTableModel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Broderick
 */
public class LocalFileSystemPanel extends JPanel implements ActionListener, MouseListener, KeyListener {

    private static final long serialVersionUID = 6692406117494277792L;
    private static Desktop desktop = Desktop.getDesktop();
    private SftpPanel sftpPanel;
    private JComboBox<File> comboDrive;
    private JPanel topPanel;
    private JToolBar topButtonToolbar;
    private File wd;
    private JScrollPane scrollTable;
    private JTable tableFiles;
    private LocalFileSystemTableModel tableModel;
    private LocalFileSystemTableRowSorter tableSorter;
    private List<File> roots;
    private JLabel labelDrive, labelStatus;
    private JButton buttonLevelUp, buttonRefresh, buttonRoot, buttonNewFolder, buttonDelete, buttonRename;
    private JTextField textPath;
    private JPanel bottomPanel;
    private long lastKeyMs;
    private String searchString;

    public LocalFileSystemPanel(SftpPanel sftpPanel) {
        super();
        this.sftpPanel = sftpPanel;
        setOpaque(false);
        setLayout(new BorderLayout());
        topPanel = new JPanel(new MigLayout("wrap 2"));
        tableModel = new LocalFileSystemTableModel();
        tableSorter = new LocalFileSystemTableRowSorter(tableModel);
        tableFiles = new JTable(tableModel);
        tableFiles.setDefaultRenderer(Object.class, new LocalFileCellRenderer());
        tableFiles.getTableHeader().setReorderingAllowed(false);
        tableFiles.setRowSorter(tableSorter);
        tableFiles.setRowHeight(18);
        tableFiles.setShowGrid(false);
        tableFiles.addMouseListener(this);
        tableFiles.addKeyListener(this);
        tableFiles.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableFiles.getColumnModel().getColumn(1).setMaxWidth(80);
        scrollTable = new JScrollPane(tableFiles);
        add(scrollTable, BorderLayout.CENTER);
        labelStatus = new JLabel(" ");
        comboDrive = new JComboBox<>();
        comboDrive.setActionCommand("changeDrive");
        comboDrive.addActionListener(this);
        comboDrive.setPreferredSize(new Dimension(10, SftpPanel.ROW_HEIGHT));
        roots = Arrays.asList(File.listRoots());
        wd = roots.get(0);
        textPath = new JTextField();
        textPath.addKeyListener(this);
        for (File root : roots) {
            comboDrive.addItem(root);
        }
        buttonRefresh = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_ARROW_REFRESH));
        buttonRefresh.setToolTipText("Refresh");
        buttonRefresh.setOpaque(false);
        buttonRefresh.setActionCommand("refresh");
        buttonRefresh.addActionListener(this);
        buttonRoot = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER_ROOT));
        buttonRoot.setToolTipText("Go to root");
        buttonRoot.setOpaque(false);
        buttonRoot.setActionCommand("root");
        buttonRoot.addActionListener(this);
        buttonNewFolder = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER_NEW));
        buttonNewFolder.setToolTipText("New folder");
        buttonNewFolder.setOpaque(false);
        buttonNewFolder.setActionCommand("newFolder");
        buttonNewFolder.addActionListener(this);
        buttonDelete = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_DELETE));
        buttonDelete.setToolTipText("Delete");
        buttonDelete.setOpaque(false);
        buttonDelete.setActionCommand("delete");
        buttonDelete.addActionListener(this);
        buttonRename = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_RENAME));
        buttonRename.setToolTipText("Rename");
        buttonRename.setOpaque(false);
        buttonRename.setActionCommand("rename");
        buttonRename.addActionListener(this);


        topButtonToolbar = new JToolBar();
        topButtonToolbar.setBorder(new EmptyBorder(0, 0, 0, 0));
        topButtonToolbar.add(buttonRefresh, "width 28!");
        topButtonToolbar.add(buttonRoot, "width 28!");
        topButtonToolbar.add(buttonNewFolder, "width 28!");
        topButtonToolbar.add(buttonDelete, "width 28!");
        topButtonToolbar.add(buttonRename, "width 28!");

        comboDrive.setRenderer(new LocalFileSystemRootRenderer());
        labelDrive = new JLabel(ViewUtils.createImageIcon(DataPath.IMG_16_DRIVE));
        buttonLevelUp = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER_LEVEL_UP));
        buttonLevelUp.addActionListener(this);
        buttonLevelUp.setToolTipText("Level up");
        buttonLevelUp.setActionCommand("levelUp");

        topPanel.add(topButtonToolbar, "span");
        topPanel.add(labelDrive, "width 28!");
        topPanel.add(comboDrive, "width :4000:4000");
        topPanel.add(buttonLevelUp, "width 28!");
        topPanel.add(textPath, "width :4000:4000");
        add(topPanel, BorderLayout.NORTH);

        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));

        bottomPanel.add(labelStatus, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        listFiles();
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();

        int columnIndexToSort = 0;
        sortKeys.add(new RowSorter.SortKey(columnIndexToSort, SortOrder.ASCENDING));

        tableSorter.setSortKeys(sortKeys);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "changeDrive":
                if (!wd.getAbsolutePath().startsWith(((File) comboDrive.getSelectedItem()).getAbsolutePath())) {
                    wd = (File) comboDrive.getSelectedItem();
                }
                listFiles();
                break;
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
        List<File> selectedFiles = getSelectedFile();
        if (selectedFiles.size() > 0) {
            if (selectedFiles.size() == 1) {
                File toRename = selectedFiles.get(0);
                RenameDialog dialog = new RenameDialog(toRename.getName());
                if (dialog.isValidated()) {
                    File[] files = wd.listFiles();
                    boolean alreadyExists = false;
                    String newName = dialog.getTextName().getText();
                    for (File file : files) {
                        if (file.getName().equals(newName)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (alreadyExists) {
                        JOptionPane.showMessageDialog(null, "Name already exists", "Rename", JOptionPane.WARNING_MESSAGE);
                    } else {
                        File rename = new File(toRename.getParentFile().getAbsolutePath() + File.separator + newName);
                        toRename.renameTo(rename);
                        listFiles();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Please select only one item", "Rename", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select one item", "Rename", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void delete() {
        List<File> files = getSelectedFile();
        if (files.size() > 0) {
            DeleteDialog dialog = new DeleteDialog();
            String answer = dialog.getAnswer();
            if (answer.equals(DeleteDialog.ANSWER_YES)) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                for (File file : files) {
                    deleteRecursive(file);
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                listFiles();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select one item", "Delete", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                deleteRecursive(f);
            }
            file.delete();
        } else if (file.isFile()) {
            file.delete();
        }

    }

    private void levelUp() {
        if (!roots.contains(wd)) {
            wd = wd.getParentFile();
            listFiles();
        }
    }

    private void root() {
        wd = (File) comboDrive.getSelectedItem();
        listFiles();
    }

    private void newFolder() {
        NewFolderDialog dialog = new NewFolderDialog();
        if (dialog.isValidated()) {
            boolean alreadyExists = false;
            String name = dialog.getTextName().getText();
            File[] files = wd.listFiles();
            for (File file : files) {
                if (file.getName().equals(name)) {
                    alreadyExists = true;
                    break;
                }
            }
            if (alreadyExists) {
                JOptionPane.showMessageDialog(null, "Name already exists", "New folder", JOptionPane.WARNING_MESSAGE);
            } else {
                File toCrate = new File(wd.getAbsolutePath() + File.separator + name);
                toCrate.mkdir();
                listFiles();
            }
        }
    }

    public void listFiles() {
        try {
            searchString = "";
            wd = wd.getCanonicalFile();
            String absolutePath = wd.getAbsolutePath();
            for (int i = 0; i < comboDrive.getItemCount(); i++) {
                if (absolutePath.startsWith(comboDrive.getItemAt(i).getAbsolutePath())) {
                    comboDrive.setSelectedIndex(i);
                }
            }
            File[] files = wd.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.isHidden();
                }
            });
            tableModel.setRowCount(0);
            if (files != null) {
                textPath.setText(wd.getAbsolutePath());
                int nFile = 0;
                int nDirectory = 0;
                int nLink = 0;
                int nTotal = 0;
                for (File file : files) {
                    if (file.isDirectory()) {
                        nDirectory++;
                    } else if (file.isFile()) {
                        if (file.getName().endsWith(".lnk")) {
                            if (WindowsShortcut.isPotentialValidLink(file)) {
                                WindowsShortcut link = new WindowsShortcut(file);
                                File resolved = link.getResolvedFile();
                                if (resolved != null && resolved.exists()) {
                                    nLink++;
                                } else {
                                    nFile++;
                                }
                            } else {
                                nFile++;
                            }
                        } else {
                            nFile++;
                        }
                    }
                    tableModel.addRow(new Object[]{file, file.isFile() ? new Long(file.length()) : new Long(0), new Date(file.lastModified())});
                    nTotal++;
                }
                labelStatus.setText(" " + (nTotal) + " items (" + nDirectory + " directories, " + nFile + " files, " + nLink + " links)");
            }
            tableSorter.sort();
        } catch (Exception e) {
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
        File file = (File) tableModel.getValueAt(tableFiles.convertRowIndexToModel(tableFiles.getSelectedRow()), 0);
        if (file.isDirectory()) {
            wd = file;
            listFiles();
        } else if (file.isFile()) {
            try {
                if (file.getName().endsWith(".lnk") && WindowsShortcut.isPotentialValidLink(file)) {
                    File resolvedFile = new WindowsShortcut(file).getResolvedFile();
                    if (resolvedFile.isDirectory()) {
                        wd = resolvedFile;
                        listFiles();
                    } else {
                        desktop.open(file);
                    }
                } else {
                    desktop.open(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getSource() == textPath) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                File file = new File(textPath.getText());
                if (file.exists() && file.isDirectory() && file.canRead()) {
                    wd = file;
                    listFiles();
                } else {
                    textPath.setText(wd.getAbsolutePath());
                }
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
                List<File> files = getSelectedFile();
                for (File file : files) {
                    sftpPanel.upload(file);
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
                    File file = ((File) tableFiles.getValueAt(i, 0));
                    //System.out.println(i+" "+file.getName());
                    if (file.getName().toLowerCase().startsWith(searchString.toLowerCase())) {
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
    }

    public List<File> getSelectedFile() {
        List<File> toReturn = new ArrayList<File>();
        int[] rows = tableFiles.getSelectedRows();
        if (rows != null) {
            for (int row : rows) {
                toReturn.add((File) (tableModel.getValueAt(tableFiles.convertRowIndexToModel(row), 0)));
            }
        }
        return toReturn;
    }

    public JTable getTableFiles() {
        return tableFiles;
    }

    public File getWd() {
        return wd;
    }
}
