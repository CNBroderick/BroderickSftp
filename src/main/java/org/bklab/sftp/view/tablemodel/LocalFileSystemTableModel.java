package org.bklab.sftp.view.tablemodel;

import javax.swing.table.DefaultTableModel;

/**
 * @author Broderick
 */
public class LocalFileSystemTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 8824577335354392969L;

    public LocalFileSystemTableModel() {
        setColumnIdentifiers(new String[]{"Name", "Size", "Modified"});

    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

}
