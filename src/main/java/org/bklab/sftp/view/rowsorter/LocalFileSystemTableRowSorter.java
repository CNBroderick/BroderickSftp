package org.bklab.sftp.view.rowsorter;

import org.bklab.sftp.utils.WindowsShortcut;
import org.bklab.sftp.view.tablemodel.LocalFileSystemTableModel;

import javax.swing.table.TableRowSorter;
import java.io.File;
import java.util.Comparator;
import java.util.Date;

/**
 * @author Broderick
 */
public class LocalFileSystemTableRowSorter extends TableRowSorter<LocalFileSystemTableModel> {
    public LocalFileSystemTableRowSorter(LocalFileSystemTableModel model) {
        setModel(model);
        setComparator(0, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                String o1Type = getType(o1, true);
                String o2Type = getType(o2, true);
                if (o1Type.equals("L")) {
                    try {
                        File attributes = new WindowsShortcut(o1).getResolvedFile();
                        if (attributes != null) {
                            o1Type = getType(attributes, false);
                            //System.out.println(o1.getAbsolutePath()+" - "+attributes.getAbsolutePath()+" - "+o1Type);
                        } else {
                            o1Type = getType(o1, false);
                        }
                    } catch (Exception e) {
                        o1Type = getType(o1, false);
                    }
                }
                if (o2Type.equals("L")) {
                    try {
                        File attributes = new WindowsShortcut(o2).getResolvedFile();
                        if (attributes != null) {
                            o2Type = getType(attributes, false);
                            //System.out.println(o2.getAbsolutePath()+" - "+attributes.getAbsolutePath()+" - "+o2Type);
                        } else {
                            o2Type = getType(o1, false);
                        }
                    } catch (Exception e) {
                        o2Type = getType(o2, false);
                    }
                }
                if (o1Type.equals(o2Type)) {
                    String firstName = o1.getName().toLowerCase();
                    String secondName = o2.getName().toLowerCase();
                    if (firstName.endsWith(".lnk")) {
                        firstName = firstName.substring(0, firstName.length() - 4);
                    }
                    if (secondName.endsWith(".lnk")) {
                        secondName = secondName.substring(0, secondName.length() - 4);
                    }
                    return firstName.compareTo(secondName);
                } else {
                    return o1Type.compareTo(o2Type);
                }
            }

            private String getType(File file, boolean checkLink) {
                String toReturn = "";
                if (checkLink) {
                    try {
                        if (file.getName().endsWith(".lnk") && WindowsShortcut.isPotentialValidLink(file)) {
                            WindowsShortcut link = new WindowsShortcut(file);
                            File resolved = link.getResolvedFile();
                            if (resolved.exists()) {
                                toReturn = "L";
                            } else {
                                toReturn = "F";
                            }
                        } else if (file.isDirectory()) {
                            toReturn = "D";
                        } else if (file.isFile()) {
                            toReturn = "F";
                        }
                    } catch (Exception e) {
                        toReturn = "F";
                    }
                } else if (file.isDirectory()) {
                    toReturn = "D";
                } else if (file.isFile()) {
                    toReturn = "F";
                }
                return toReturn;
            }
        });
        setComparator(1, (Comparator<Long>) Long::compareTo);
        setComparator(2, (Comparator<Date>) Date::compareTo);
    }

}
