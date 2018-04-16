package org.bklab.sftp.view.tablemodel;

import javax.swing.table.DefaultTableModel;

/**
 * @author Broderick
 */
public class NetworkQueueTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 2419343878230162070L;

    public NetworkQueueTableModel() {
        setColumnIdentifiers(new String[]{"Op.", "From", "To", "Size", "Actual", "Progress"});

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
