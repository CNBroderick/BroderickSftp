package org.bklab.sftp.dto;

import org.bklab.ssh2.SFTPv3DirectoryEntry;

import java.io.File;

/**
 * @author Broderick
 */
public class NetworkQueueElement {
    public static final String OPERATION_UPLOAD = "upload";
    public static final String OPERATION_DOWNLOAD = "download";
    public static final String STATUS_READY = "ready";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_FINISHED = "finished";
    public static final String STATUS_EXCEPTION = "exception";
    private String operation;
    private String status;
    private File localFile;
    private SFTPv3DirectoryEntry remoteFile;
    private String remoteWD;
    private Long totalSize;
    private Long actualSize;
    private String from;
    private String to;
    private Exception exception;
    private String exceptionMessage;

    public NetworkQueueElement(String operation, File localFile, SFTPv3DirectoryEntry remoteFile, String remoteWD) {
        super();
        this.operation = operation;
        this.localFile = localFile;
        this.remoteFile = remoteFile;
        this.remoteWD = remoteWD;
        this.actualSize = 0L;
        this.status = STATUS_READY;
        this.exceptionMessage = "";
        if (operation.equals(OPERATION_DOWNLOAD)) {
            this.from = remoteWD + (remoteWD.endsWith("/") ? "" : "/") + remoteFile.filename;
            this.to = localFile.getAbsolutePath() + (localFile.getAbsolutePath().endsWith(File.separator) ? "" : File.separator) + remoteFile.filename;
            this.totalSize = remoteFile.attributes.size;
        } else {
            this.from = localFile.getAbsolutePath();
            this.to = remoteWD + (remoteWD.endsWith("/") ? "" : "/") + localFile.getName();
            this.totalSize = localFile.length();
        }
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public File getLocalFile() {
        return localFile;
    }

    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }

    public SFTPv3DirectoryEntry getRemoteFile() {
        return remoteFile;
    }

    public void setRemoteFile(SFTPv3DirectoryEntry remoteFile) {
        this.remoteFile = remoteFile;
    }

    public String getRemoteWD() {
        return remoteWD;
    }

    public void setRemoteWD(String remoteWD) {
        this.remoteWD = remoteWD;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public Long getActualSize() {
        return actualSize;
    }

    public void setActualSize(Long actualSize) {
        this.actualSize = actualSize;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
