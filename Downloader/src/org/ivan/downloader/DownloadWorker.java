package org.ivan.downloader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadWorker {

    public static final int DEFAULT_BUFF_SIZE = 2048;
    private final IOWrapper ioWrapper;
    private final ProtocolHelper helper;
    private final DownloadHolder downloadHolder;
    private volatile DownloadState state = new DownloadState();

    public DownloadWorker(IOWrapper ioWrapper, ProtocolHelper helper, DownloadHolder downloadHolder) {
        this.ioWrapper = ioWrapper;
        this.helper = helper;
        this.downloadHolder = downloadHolder;
    }

    public void performDownload() {
        byte[] buffer = new byte[DEFAULT_BUFF_SIZE];
        int nRead;
        try {
            while ((nRead = helper.readDownloadBytes(buffer, ioWrapper)) != -1 && !Thread.interrupted()) {
                downloadHolder.appendBytes(buffer, 0, nRead);
                updateState(nRead);
            }
            updateStateFinish();
        } catch (IOException e) {
            updateStateError("Exception occured " + e.getMessage());
            Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void updateStateFinish() {
        state = new DownloadState(
                DownloadState.StateCode.COMPLETE,
                DownloadState.StateCode.COMPLETE.toString(),
                state.getBytesRead(),
                state.getLength()
        );
    }

    private void updateStateError(String errorMessage) {
        state = new DownloadState(
                DownloadState.StateCode.PAUSED_ERROR,
                errorMessage,
                state.getBytesRead(),
                state.getLength()
        );
    }

    private void updateState(int nRead) {
        state = new DownloadState(
                DownloadState.StateCode.IN_PROGRESS,
                DownloadState.StateCode.IN_PROGRESS.toString(),
                state.getBytesRead() + nRead,
                state.getLength()
        );
    }

    public void cancel() {
        try {
            ioWrapper.close();
            downloadHolder.flush();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
        }
    }

    public DownloadState getState() {
        return state;
    }
}
