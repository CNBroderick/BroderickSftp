package org.bklab.sftp.view.tablemodel;

import javax.swing.table.DefaultTableModel;

/**
 * @author Broderick
 */
public class RemoteFileSystemTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 3564783661478352933L;

    public RemoteFileSystemTableModel() {
        setColumnIdentifiers(new String[]{"Name", "Size", "Modified"});
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
