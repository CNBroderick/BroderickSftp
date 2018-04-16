package org.bklab.sftp.view.rowsorter;

import org.bklab.sftp.view.panel.sftp.RemoteFileSystemPanel;
import org.bklab.sftp.view.tablemodel.RemoteFileSystemTableModel;
import org.bklab.ssh2.SFTPv3DirectoryEntry;
import org.bklab.ssh2.SFTPv3FileAttributes;

import javax.swing.table.TableRowSorter;
import java.util.Comparator;

/**
 * @author Broderick
 */
public class RemoteFileSystemTableRowSorter extends TableRowSorter<RemoteFileSystemTableModel> {
    public RemoteFileSystemTableRowSorter(RemoteFileSystemTableModel model, final RemoteFileSystemPanel panel) {
        setModel(model);
        setComparator(0, new Comparator<SFTPv3DirectoryEntry>() {
            @Override
            public int compare(SFTPv3DirectoryEntry o1, SFTPv3DirectoryEntry o2) {
                String o1Type = getType(o1.attributes, true);
                String o2Type = getType(o2.attributes, true);
                if (o1Type.equals("L")) {
                    SFTPv3FileAttributes attributes = panel.resolveLink(o1);
                    if (attributes != null) {
                        o1Type = getType(attributes, false);
                    } else {
                        o1Type = getType(o1.attributes, false);
                    }
                }
                if (o2Type.equals("L")) {
                    SFTPv3FileAttributes attributes = panel.resolveLink(o2);
                    if (attributes != null) {
                        o2Type = getType(attributes, false);
                    } else {
                        o2Type = getType(o2.attributes, false);
                    }
                }
                if (o1Type.equals(o2Type)) {
                    return o1.filename.toLowerCase().compareTo(o2.filename.toLowerCase());
                } else {
                    return o1Type.compareTo(o2Type);
                }
            }

            private String getType(SFTPv3FileAttributes attributes, boolean checkLink) {
                String toReturn = "";
                if (checkLink && attributes.isSymlink()) {
                    toReturn = "L";
                } else if (attributes.isDirectory()) {
                    toReturn = "D";
                } else if (attributes.isRegularFile()) {
                    toReturn = "F";
                }
                return toReturn;
            }
        });
        setComparator(1, Comparator.naturalOrder());
        setComparator(2, Comparator.naturalOrder());
    }

}
