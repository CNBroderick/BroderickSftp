package org.bklab.sftp.dto;

/**
 * @author Broderick
 */
public class NetworkQueueAsynchRequest {
    private String command;
    private NetworkQueueElement element;
    private Integer row;
    private Long longValue;

    public NetworkQueueAsynchRequest(String command, Integer row, Long longValue, NetworkQueueElement element) {
        super();
        this.command = command;
        this.row = row;
        this.longValue = longValue;
        this.element = element;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public NetworkQueueElement getElement() {
        return element;
    }

    public void setElement(NetworkQueueElement element) {
        this.element = element;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    @Override
    public String toString() {
        return "NetworkQueueAsynchRequest [command=" + command + ", element=" + element + ", row=" + row
                + ", longValue=" + longValue + "]";
    }
}
