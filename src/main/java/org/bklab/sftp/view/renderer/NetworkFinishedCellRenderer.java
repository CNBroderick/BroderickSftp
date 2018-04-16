package org.bklab.sftp.view.renderer;

import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.dto.NetworkQueueElement;
import org.bklab.sftp.utils.DataUtils;
import org.bklab.sftp.utils.ViewUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * @author Broderick
 */
public class NetworkFinishedCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 493496088781223953L;

    public NetworkFinishedCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component toReturn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        NetworkFinishedCellRenderer c = (NetworkFinishedCellRenderer) toReturn;
        if (column == 0) {
            NetworkQueueElement convertedValue = (NetworkQueueElement) value;
            c.setText("");
            c.setHorizontalAlignment(JLabel.CENTER);
            if (convertedValue.getStatus().equals(NetworkQueueElement.STATUS_FINISHED)) {
                c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_OK_D));
            } else if (convertedValue.getStatus().equals(NetworkQueueElement.STATUS_EXCEPTION)) {
                c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_CROSS));
            }
        } else if (column == 1) {
            c.setHorizontalAlignment(JLabel.LEFT);
            c.setIcon(null);
        } else if (column == 2) {
            c.setHorizontalAlignment(JLabel.LEFT);
            c.setIcon(null);
        } else if (column == 3) {
            c.setHorizontalAlignment(JLabel.RIGHT);
            Long convertedValue = (Long) value;
            c.setText(DataUtils.getHumanReadableFileSize(convertedValue));
            c.setIcon(null);
        } else if (column == 4) {
            NetworkQueueElement element = (NetworkQueueElement) table.getValueAt(row, 0);
            c.setHorizontalAlignment(JLabel.LEFT);
            c.setText(element.getExceptionMessage());
            c.setIcon(null);
        }
        return toReturn;
    }
}
