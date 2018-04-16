package org.bklab.sftp.view.panel.sftp;

import org.bklab.sftp.configuration.DataPath;
import org.bklab.sftp.dto.NetworkQueueAsynchRequest;
import org.bklab.sftp.dto.NetworkQueueElement;
import org.bklab.sftp.utils.ViewUtils;
import org.bklab.sftp.view.panel.SftpPanel;
import org.bklab.sftp.view.renderer.NetworkFinishedCellRenderer;
import org.bklab.sftp.view.renderer.NetworkQueueCellRenderer;
import org.bklab.sftp.view.tablemodel.NetworkFinishedTableModel;
import org.bklab.sftp.view.tablemodel.NetworkQueueTableModel;
import org.bklab.ssh2.SFTPException;
import org.bklab.ssh2.SFTPv3Client;
import org.bklab.ssh2.SFTPv3DirectoryEntry;
import org.bklab.ssh2.SFTPv3FileHandle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * @author Broderick
 */
public class NetworkQueuePanel extends JPanel implements ActionListener {

    public static final String STATUS_READY = "ready";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_DOWNLOADING = "downloading";
    public static final String STATUS_UPLOADING = "uploading";
    public static final String STATUS_STOPPED = "stopped";
    public static final String STATUS_EXCPETION = "exception";
    private static final long serialVersionUID = 1591240717079821695L;
    private JPanel panelMain, panelFinished;
    private JTable tableQueue;
    private JTable tableFinished;
    private NetworkQueueTableModel tableModelQueue;
    private NetworkFinishedTableModel tableModelFinished;
    private NetworkQueueCellRenderer cellRendererQueue;
    private NetworkFinishedCellRenderer cellRendererFinished;
    private JScrollPane scrollTableQueue, scrollTableFinished;
    private SFTPv3Client client;
    private QueueWatchDog queueWatchDog;
    private SftpPanel parentPanel;
    private JToolBar toolbar;
    private JButton buttonClearFinished;

    private JTabbedPane tabs;

    private int queued, finished, exception;

    public NetworkQueuePanel(SFTPv3Client client, SftpPanel parentPanel) {
        super();
        this.client = client;
        this.queueWatchDog = new QueueWatchDog();
        this.parentPanel = parentPanel;
        this.queued = 0;
        this.finished = 0;
        this.exception = 0;
        setOpaque(false);
        setBorder(new EmptyBorder(5, 0, 0, 0));
        setLayout(new BorderLayout());
        tableModelQueue = new NetworkQueueTableModel();
        tableQueue = new JTable(tableModelQueue);
        tableModelFinished = new NetworkFinishedTableModel();
        tableFinished = new JTable(tableModelFinished);

        cellRendererQueue = new NetworkQueueCellRenderer();
        tableQueue.setDefaultRenderer(Object.class, cellRendererQueue);
        tableQueue.getTableHeader().setReorderingAllowed(false);
        tableQueue.setRowHeight(18);
        tableQueue.setShowGrid(false);
        tableQueue.setRowSelectionAllowed(false);
        tableQueue.getColumnModel().getColumn(0).setPreferredWidth(30);
        tableQueue.getColumnModel().getColumn(0).setMaxWidth(30);
        tableQueue.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableQueue.getColumnModel().getColumn(3).setMaxWidth(80);
        tableQueue.getColumnModel().getColumn(4).setPreferredWidth(80);
        tableQueue.getColumnModel().getColumn(4).setMaxWidth(80);
        tableQueue.getColumnModel().getColumn(5).setPreferredWidth(200);
        tableQueue.getColumnModel().getColumn(5).setMaxWidth(200);

        cellRendererFinished = new NetworkFinishedCellRenderer();
        tableFinished.setDefaultRenderer(Object.class, cellRendererFinished);
        tableFinished.getTableHeader().setReorderingAllowed(false);
        tableFinished.setRowHeight(18);
        tableFinished.setShowGrid(false);
        tableFinished.setRowSelectionAllowed(false);
        tableFinished.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableFinished.getColumnModel().getColumn(0).setMaxWidth(40);
        tableFinished.getColumnModel().getColumn(3).setPreferredWidth(80);
        tableFinished.getColumnModel().getColumn(3).setMaxWidth(80);
        tableFinished.getColumnModel().getColumn(4).setPreferredWidth(320);
        tableFinished.getColumnModel().getColumn(4).setMaxWidth(320);


        panelMain = new JPanel(new BorderLayout());
        panelMain.setOpaque(false);
        panelMain.setBorder(new TitledBorder("Network queue"));
        scrollTableQueue = new JScrollPane(tableQueue);

        scrollTableFinished = new JScrollPane(tableFinished);

        buttonClearFinished = new JButton(ViewUtils.createImageIcon(DataPath.IMG_16_DELETE));
        buttonClearFinished.setOpaque(false);
        buttonClearFinished.setToolTipText("Clear table");
        buttonClearFinished.setActionCommand("clearFinished");
        buttonClearFinished.addActionListener(this);
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOpaque(false);
        toolbar.add(buttonClearFinished);


        panelFinished = new JPanel(new BorderLayout());
        panelFinished.add(scrollTableFinished, BorderLayout.CENTER);
        panelFinished.add(toolbar, BorderLayout.NORTH);


        tabs = new JTabbedPane(JTabbedPane.BOTTOM);
        tabs.addTab("Queued", scrollTableQueue);
        tabs.addTab("Completed", panelFinished);

        panelMain.add(tabs, BorderLayout.CENTER);
        add(panelMain, BorderLayout.CENTER);
        queueWatchDog.execute();
        setPreferredSize(new Dimension(50, 200));
    }

    public void addToQueue(NetworkQueueElement element) {
        queued++;
        tabs.setTitleAt(0, "Queued (" + queued + ")");
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        tableModelQueue.addRow(new Object[]{element, element.getFrom(), element.getTo(), element.getTotalSize(), element.getActualSize(), progressBar});
        tableQueue.updateUI();
    }

    public void requestStop() {
        queueWatchDog.requestStop();
    }

    public String getQueueStatus() {
        return queueWatchDog.getStatus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("clearFinished")) {
            finished = 0;
            exception = 0;
            tabs.setTitleAt(1, "Completed");
            tableModelFinished.setRowCount(0);
            tableFinished.updateUI();
        }

    }

    private class QueueWatchDog extends SwingWorker<Void, NetworkQueueAsynchRequest> {
        private String status;
        private boolean stopRequested;

        public QueueWatchDog() {
            this.status = STATUS_READY;
            this.stopRequested = false;
        }

        private void requestStop() {
            stopRequested = true;
        }


        @Override
        protected Void doInBackground() throws Exception {
            status = STATUS_RUNNING;
            while (status.equals(STATUS_RUNNING)) {
                while (tableQueue.getRowCount() > 0) {
                    NetworkQueueElement element = null;
                    try {
                        element = (NetworkQueueElement) tableQueue.getValueAt(0, 0);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        element = null;
                    }
                    if (element != null && element.getStatus().equals(NetworkQueueElement.STATUS_READY)) {
                        try {
                            element.setStatus(NetworkQueueElement.STATUS_RUNNING);
                            if (element.getOperation().equals(NetworkQueueElement.OPERATION_DOWNLOAD)) {
                                if (element.getRemoteFile().attributes.isRegularFile()) {
                                    status = STATUS_DOWNLOADING;
                                    SFTPv3FileHandle handle = client.openFileRO(element.getFrom());
                                    byte[] data = new byte[32768];
                                    long offset = 0;
                                    int readed = 0;
                                    FileOutputStream fos = new FileOutputStream(element.getTo());
                                    while (readed != -1) {
                                        readed = client.read(handle, offset, data, 0, data.length);
                                        offset += readed;
                                        element.setActualSize((long) offset);
                                        if (readed != -1) {
                                            fos.write(data, 0, readed);
                                        }
                                        publish(new NetworkQueueAsynchRequest("value", 0, offset, element));
                                    }
                                    fos.close();
                                    client.closeFile(handle);
                                    publish(new NetworkQueueAsynchRequest("finished", 0, null, element));
                                    element.setStatus(NetworkQueueElement.STATUS_FINISHED);
                                    status = STATUS_RUNNING;
                                } else if (element.getRemoteFile().attributes.isDirectory()) {
                                    File dir = new File(element.getTo());
                                    dir.mkdir();
                                    @SuppressWarnings("unchecked")
                                    Vector<SFTPv3DirectoryEntry> entries = client.ls(element.getFrom());
                                    for (SFTPv3DirectoryEntry entry : entries) {
                                        if (entry.filename.equals(".") == false && entry.filename.equals("..") == false) {
                                            NetworkQueueElement toAdd = new NetworkQueueElement(element.getOperation(), dir, entry, element.getRemoteWD() + "/" + element.getRemoteFile().filename);
                                            publish(new NetworkQueueAsynchRequest("add", null, null, toAdd));
                                        }
                                    }
                                    publish(new NetworkQueueAsynchRequest("finished", 0, null, element));
                                    element.setStatus(NetworkQueueElement.STATUS_FINISHED);

                                } else if (element.getRemoteFile().attributes.isSymlink()) {
                                    publish(new NetworkQueueAsynchRequest("remove", 0, null, element));
                                } else {
                                    publish(new NetworkQueueAsynchRequest("remove", 0, null, element));
                                }
                            } else if (element.getOperation().equals(NetworkQueueElement.OPERATION_UPLOAD)) {
                                if (element.getLocalFile().isFile()) {
                                    status = STATUS_UPLOADING;
                                    SFTPv3FileHandle handle = client.createFileTruncate(element.getTo());
                                    FileInputStream fis = new FileInputStream(element.getLocalFile());
                                    byte[] data = new byte[32768];
                                    long offset = 0;
                                    int readed = 0;
                                    while (readed != -1) {
                                        readed = fis.read(data, 0, data.length);
                                        if (readed != -1) {
                                            client.write(handle, offset, data, 0, readed);
                                            offset += readed;
                                            publish(new NetworkQueueAsynchRequest("value", 0, offset, element));
                                        }
                                    }
                                    fis.close();
                                    client.closeFile(handle);
                                    publish(new NetworkQueueAsynchRequest("finished", 0, null, element));
                                    element.setStatus(NetworkQueueElement.STATUS_FINISHED);
                                    status = STATUS_RUNNING;
                                } else if (element.getLocalFile().isDirectory()) {
                                    String dir = element.getTo();
                                    try {
                                        client.ls(dir);
                                    } catch (SFTPException e) {
                                        if (e.getServerErrorCode() == 2) {
                                            client.mkdir(dir, 0755);
                                        } else {
                                            throw e;
                                        }
                                    }
                                    List<File> entries = Arrays.asList(element.getLocalFile().listFiles());
                                    for (File entry : entries) {
                                        NetworkQueueElement toAdd = new NetworkQueueElement(element.getOperation(), entry, null, dir);
                                        publish(new NetworkQueueAsynchRequest("add", null, null, toAdd));
                                    }
                                    publish(new NetworkQueueAsynchRequest("finished", 0, null, element));
                                    element.setStatus(NetworkQueueElement.STATUS_FINISHED);
                                } else {
                                    publish(new NetworkQueueAsynchRequest("remove", 0, null, element));
                                }
                            } else {
                                publish(new NetworkQueueAsynchRequest("remove", 0, null, element));
                            }
                        } catch (SFTPException e) {
                            element.setException(e);
                            element.setExceptionMessage(e.getServerErrorMessage());
                            element.setStatus(NetworkQueueElement.STATUS_EXCEPTION);
                            status = STATUS_RUNNING;
                            publish(new NetworkQueueAsynchRequest("finished", 0, null, element));
                        } catch (Exception e) {
                            element.setException(e);
                            element.setExceptionMessage(e.getLocalizedMessage());
                            element.setStatus(NetworkQueueElement.STATUS_EXCEPTION);
                            status = STATUS_RUNNING;
                            publish(new NetworkQueueAsynchRequest("finished", 0, null, element));
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    ;
                }
                if (stopRequested) {
                    //System.out.println("STOP Requested succesfully");
                    status = STATUS_STOPPED;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                ;
            }
            return null;
        }

        private String getStatus() {
            return status;
        }

        @Override
        protected void process(List<NetworkQueueAsynchRequest> requests) {
            for (NetworkQueueAsynchRequest request : requests) {
                //System.out.println(request);
                if (request.getCommand().equals("add")) {
                    addToQueue(request.getElement());
                } else if (request.getCommand().equals("value")) {
                    request.getElement().setActualSize(request.getLongValue());
                    tableQueue.updateUI();
                } else if (request.getCommand().equals("finished")) {
                    queued--;
                    if (queued > 0) {
                        tabs.setTitleAt(0, "Queued (" + queued + ")");
                    } else {
                        tabs.setTitleAt(0, "Queued");
                    }
                    NetworkQueueElement element = request.getElement();
                    tableModelQueue.removeRow(request.getRow());
                    tableQueue.updateUI();
                    tableModelFinished.addRow(new Object[]{element, element.getFrom(), element.getTo(), element.getTotalSize(), element.getExceptionMessage()});
                    tableFinished.updateUI();
                    if (request.getElement().getOperation().equals(NetworkQueueElement.OPERATION_DOWNLOAD)) {
                        if (parentPanel.getPanelLocal().getWd().equals(request.getElement().getLocalFile())) {
                            parentPanel.getPanelLocal().listFiles();
                        }
                    } else if (request.getElement().getOperation().equals(NetworkQueueElement.OPERATION_UPLOAD)) {
                        if (parentPanel.getPanelRemote().getWd().equals(request.getElement().getRemoteWD())) {
                            parentPanel.getPanelRemote().listFiles();
                        }
                    }
                    finished++;
                    if (element.getStatus() == NetworkQueueElement.STATUS_EXCEPTION) {
                        exception++;
                    }
                    if (exception > 0) {
                        tabs.setTitleAt(1, "Completed (" + finished + " - " + exception + " exceptions)");
                    } else {
                        tabs.setTitleAt(1, "Completed (" + finished + ")");
                    }
                } else if (request.getCommand().equals("remove")) {
                    queued--;
                    if (queued > 0) {
                        tabs.setTitleAt(0, "Queued (" + queued + ")");
                    } else {
                        tabs.setTitleAt(0, "Queued");
                    }
                    tableModelQueue.removeRow(request.getRow());
                    tableQueue.updateUI();
                }
            }
        }
    }
}
