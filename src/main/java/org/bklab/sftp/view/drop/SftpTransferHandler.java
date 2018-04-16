package org.bklab.sftp.view.drop;

import org.bklab.sftp.utils.ExceptionManager;
import org.bklab.sftp.view.panel.SftpPanel;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Broderick
 */
public class SftpTransferHandler extends TransferHandler {

    private static final long serialVersionUID = 718192578905737175L;
    private int action;
    private SftpPanel panel;

    public SftpTransferHandler(SftpPanel panel) {
        this.action = TransferHandler.COPY;
        this.panel = panel;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (support.isDrop() && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            boolean actionSupported = (action & support.getSourceDropActions()) == action;
            if (actionSupported) {
                support.setDropAction(action);
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        // if we can't handle the import, say so
        if (!canImport(support)) {
            return false;
        }
        List<File> fileList = null;
        try {
            fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException e) {
            //NOOP
        } catch (IOException e) {
            ExceptionManager.manageException(e);
        }
        for (File file : fileList) {
            panel.upload(file);
        }
        return true;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }
}
