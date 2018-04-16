package org.bklab.sftp.view.renderer;

import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.utils.WindowsShortcut;
import org.bklab.sftp.view.widget.OverlayImageIcon;

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
public class LocalFileCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1614642796608194929L;
    private static LinkedHashSet<String> knownExtensions = null;

    public LocalFileCellRenderer() {
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
        LocalFileCellRenderer c = (LocalFileCellRenderer) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (column == 0) {
            File file = (File) value;
            if (file.isDirectory()) {
                c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER));
            } else {
                if (file.getName().endsWith(".lnk") && WindowsShortcut.isPotentialValidLink(file)) {
                    try {
                        File resolvedFile = new WindowsShortcut(file).getResolvedFile();
                        if (resolvedFile.isDirectory()) {
                            OverlayImageIcon icon = new OverlayImageIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FOLDER), ViewUtils.createImageIcon(DataPath.IMG_16_BULLET_LINK));
                            c.setIcon(icon);
                        } else if (resolvedFile.isFile()) {
                            String extension = DataUtils.getExtension(resolvedFile.getName());
                            if (knownExtensions.contains(extension)) {
                                OverlayImageIcon icon = new OverlayImageIcon(ViewUtils.createImageIcon(DataPath.IMG_16_EXT_FILE_EXTENSION_ + extension + ".png"), ViewUtils.createImageIcon(DataPath.IMG_16_BULLET_LINK));
                                c.setIcon(icon);
                            } else {
                                OverlayImageIcon icon = new OverlayImageIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FILE), ViewUtils.createImageIcon(DataPath.IMG_16_BULLET_LINK));
                                c.setIcon(icon);
                            }
                        }
                    } catch (Exception e) {
                        ExceptionManager.manageException(e);
                    }
                } else {
                    String extension = DataUtils.getExtension(file.getName());
                    if (knownExtensions.contains(extension)) {
                        c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_EXT_FILE_EXTENSION_ + extension + ".png"));
                    } else {
                        c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_FILE));
                    }
                }
            }
            c.setText(file.getName().endsWith(".lnk") ? file.getName().substring(0, file.getName().length() - 4) : file.getName());
            c.setHorizontalAlignment(LEFT);
        } else if (column == 1) {
            c.setIcon(null);
            File file = (File) table.getModel().getValueAt(table.convertRowIndexToModel(row), 0);
            if (file.isFile()) {
                if (file.getName().endsWith(".lnk") && WindowsShortcut.isPotentialValidLink(file)) {
                    c.setText("");
                } else {
                    Long size = (Long) value;
                    c.setText(DataUtils.getHumanReadableFileSize(size));
                }
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
