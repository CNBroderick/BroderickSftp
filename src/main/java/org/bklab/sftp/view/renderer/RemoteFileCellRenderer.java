package org.bklab.sftp.view.renderer;

import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.panel.sftp.RemoteFileSystemPanel;
import org.bklab.sftp.view.widget.OverlayImageIcon;
import org.bklab.ssh2.SFTPv3DirectoryEntry;
import org.bklab.ssh2.SFTPv3FileAttributes;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * @author Broderick
 */
public class RemoteFileCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1614642796608194929L;
    private static LinkedHashSet<String> knownExtensions = null;
    private RemoteFileSystemPanel panel;

    public RemoteFileCellRenderer(RemoteFileSystemPanel panel) {
        this.panel = panel;
        if (knownExtensions == null) {
            knownExtensions = new LinkedHashSet<>();
            File[] files = new File(DataPath.IMG_16_EXT).listFiles((dir, name) -> name.endsWith(".png"));
            for (File file : Objects.requireNonNull(files)) {
                String fileName = file.getName();
                fileName = fileName.substring(fileName.lastIndexOf("_") + 1);
                fileName = fileName.substring(0, fileName.indexOf("."));
                knownExtensions.add(fileName);
            }
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        RemoteFileCellRenderer c = (RemoteFileCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column == 0) {
            SFTPv3DirectoryEntry file = (SFTPv3DirectoryEntry) value;
            SFTPv3FileAttributes attributes = file.attributes;
            if (file.attributes.isSymlink()) {
                SFTPv3FileAttributes resolveAttributes = panel.resolveLink(file);
                if (resolveAttributes.isDirectory()) {
                    OverlayImageIcon icon = new OverlayImageIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER), ViewUtils.createImageIcon(DataPath.IMG_16_BULLET_LINK));
                    c.setIcon(icon);
                } else if (resolveAttributes.isRegularFile()) {
                    String extension = DataUtils.getExtension(file.filename);
                    if (knownExtensions.contains(extension)) {
                        OverlayImageIcon icon = new OverlayImageIcon(ViewUtils.createImageIcon(DataPath.IMG_16_EXT_FILE_EXTENSION_ + extension + ".png"), ViewUtils.createImageIcon(DataPath.IMG_16_BULLET_LINK));
                        c.setIcon(icon);
                    } else {
                        OverlayImageIcon icon = new OverlayImageIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FILE), ViewUtils.createImageIcon(DataPath.IMG_16_BULLET_LINK));
                        c.setIcon(icon);
                    }
                }
            } else if (attributes.isDirectory()) {
                c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER));
            } else if (attributes.isRegularFile()) {
                String extension = DataUtils.getExtension(file.filename);
                if (knownExtensions.contains(extension)) {
                    c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_EXT_FILE_EXTENSION_ + extension + ".png"));
                } else {
                    c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FILE));
                }
            }
            c.setText(file.filename);
            c.setHorizontalAlignment(LEFT);
        } else if (column == 1) {
            c.setIcon(null);
            SFTPv3DirectoryEntry file = (SFTPv3DirectoryEntry) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
            if (file.attributes.isRegularFile()) {
                Long size = (Long) value;
                c.setText(DataUtils.getHumanReadableFileSize(size));
                c.setHorizontalAlignment(RIGHT);
            } else {
                c.setText("");
                c.setHorizontalAlignment(RIGHT);
            }
        } else if (column == 2) {
            c.setIcon(null);
            Date date = (Date) value;
            c.setText(DataUtils.dateFormat(date));
            c.setHorizontalAlignment(LEFT);
        }
        return c;
    }
}
