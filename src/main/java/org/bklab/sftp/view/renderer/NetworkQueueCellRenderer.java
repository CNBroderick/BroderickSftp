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
public class NetworkQueueCellRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 493496088781223953L;

    public NetworkQueueCellRenderer() {
        super();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component toReturn = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        NetworkQueueCellRenderer c = (NetworkQueueCellRenderer) toReturn;
        if (column == 0) {
            NetworkQueueElement convertedValue = (NetworkQueueElement) value;
            c.setText("");
            c.setHorizontalAlignment(JLabel.CENTER);
            if (convertedValue.getOperation().equals(NetworkQueueElement.OPERATION_DOWNLOAD)) {
                c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_ARROW_LEFT));
            } else if (convertedValue.getOperation().equals(NetworkQueueElement.OPERATION_UPLOAD)) {
                c.setIcon(ViewUtils.createImageIcon(DataPath.IMG_16_ARROW_RIGHT));
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
            c.setHorizontalAlignment(JLabel.RIGHT);
            c.setText(DataUtils.getHumanReadableFileSize(element.getActualSize()));
            c.setIcon(null);
        } else if (column == 5) {
            NetworkQueueElement element = (NetworkQueueElement) table.getValueAt(row, 0);
            c.setHorizontalAlignment(JLabel.CENTER);
            JProgressBar convertedValue = (JProgressBar) value;
            long total = element.getTotalSize();
            long actual = element.getActualSize();
            int percent = (int) (((double) actual / (double) total) * 100);
            convertedValue.setValue(percent);
            convertedValue.setString(percent + "%");
            toReturn = convertedValue;
        }
        return toReturn;
    }
}
