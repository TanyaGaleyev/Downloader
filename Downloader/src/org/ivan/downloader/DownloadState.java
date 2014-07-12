package org.ivan.downloader;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadState {
    private StateCode stateCode = StateCode.NOT_STARTED;
    private String message = "";
    private int bytesRead = 0;
    private int length = -1;

    public DownloadState() {}

    public DownloadState(StateCode stateCode, String message, int bytesRead, int length) {
        this.stateCode = stateCode;
        this.message = message;
        this.bytesRead = bytesRead;
        this.length = length;
    }

    public DownloadState(StateCode stateCode, int bytesRead, int length) {
        this.stateCode = stateCode;
        this.bytesRead = bytesRead;
        this.length = length;
    }

    public StateCode getStateCode() {
        return stateCode;
    }

    public void setStateCode(StateCode stateCode) {
        this.stateCode = stateCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public enum StateCode {
        NOT_STARTED, IN_PROGRESS, PAUSED, PAUSED_ERROR, CANCELED, COMPLETE;
    }

    @Override
    public String toString() {
        return String.format("%s(%s) downloaded: %d, length: %d", stateCode.toString(), message, bytesRead, length);
    }
}
