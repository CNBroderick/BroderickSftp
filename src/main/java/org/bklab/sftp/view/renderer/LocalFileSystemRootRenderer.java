package org.bklab.sftp.view.renderer;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;

/**
 * @author Broderick
 */
public class LocalFileSystemRootRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1576278460147090333L;
    private static LinkedHashMap<File, String[]> data = new LinkedHashMap<>();
    private static LinkedHashMap<File, Icon> icons = new LinkedHashMap<>();
    private FileSystemView fsv;

    public LocalFileSystemRootRenderer() {
        fsv = FileSystemView.getFileSystemView();
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        File file = (File) value;
        String[] dataString = data.get(file);
        Icon icon = icons.get(file);
        if (dataString == null) {
            dataString = new String[]{fsv.getSystemTypeDescription(file), fsv.getSystemDisplayName(file), fsv.getSystemDisplayName(file)};
            icon = fsv.getSystemIcon(file);
            data.put(file, dataString);
            icons.put(file, icon);
        }
        setIcon(icon);
        setText(dataString[0] + " - " + file + (dataString[1].equals("") ? "" : (" - " + dataString[2])));
        return this;
    }

    public void refreshCache() {
        data.clear();
        icons.clear();
    }
}
