package org.ivan.downloader;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadState {
    private StateCode stateCode = StateCode.NOT_STARTED;
    private String message = "";
    private int bytesRead = 0;
    private int size = -1;

    public DownloadState() {}

    public DownloadState(StateCode stateCode, String message, int bytesRead, int size) {
        this.stateCode = stateCode;
        this.message = message;
        this.bytesRead = bytesRead;
        this.size = size;
    }

    public DownloadState(StateCode stateCode, int bytesRead, int size) {
        this(stateCode, "", bytesRead, size);
    }

    public DownloadState(DownloadState origin) {
        this(origin.stateCode, origin.message, origin.bytesRead, origin.size);
    }

    public StateCode getStateCode() {
        return stateCode;
    }

//    public void setStateCode(StateCode stateCode) {
//        this.stateCode = stateCode;
//    }

    public String getMessage() {
        return message;
    }

//    public void setMessage(String message) {
//        this.message = message;
//    }

    public void setStatus(StateCode stateCode) {
        setStatus(stateCode, "");
    }

    public void setStatus(StateCode stateCode, String message) {
        this.stateCode = stateCode;
        this.message = message;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public enum StateCode {
        NOT_STARTED, IN_PROGRESS, PAUSED, PAUSED_ERROR, COMPLETE;
    }

    @Override
    public String toString() {
        return String.format("%s(%s) downloaded: %d, size: %d", stateCode.toString(), message, bytesRead, size);
    }
}
