package org.bklab.sftp.view.tablemodel;

import javax.swing.table.DefaultTableModel;

/**
 * @author Broderick
 */
public class NetworkFinishedTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 2419343878230162070L;

    public NetworkFinishedTableModel() {
        setColumnIdentifiers(new String[]{"Result", "From", "To", "Size", "Message"});

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
