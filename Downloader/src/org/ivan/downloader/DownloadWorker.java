package org.ivan.downloader;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadWorker {

    public static final int DEFAULT_BUFF_SIZE = 2048;
    private final IOAdapter ioAdapter;
    private final ProtocolHelper helper;
    private final DownloadHolder downloadHolder;
    private volatile int bytesRead;
    private volatile boolean isRangeSupported;
    private volatile int size;

    public DownloadWorker(IOAdapter ioAdapter, ProtocolHelper helper, DownloadHolder downloadHolder) {
        this(ioAdapter, helper, downloadHolder, 0, 0);
    }

    public DownloadWorker(IOAdapter ioAdapter, ProtocolHelper helper, DownloadHolder downloadHolder, int bytesRead, int size) {
        this.ioAdapter = ioAdapter;
        this.helper = helper;
        this.downloadHolder = downloadHolder;
        this.bytesRead = bytesRead;
        this.size = size;
    }

    public void performDownload() throws IOException {
        ioAdapter.open();
        byte[] requestMessage = helper.getRequestMessage(bytesRead);
        ioAdapter.write(requestMessage);
        isRangeSupported = helper.isRangeSupported(ioAdapter);
        if(size == 0) size = helper.getSize();
        byte[] buffer = new byte[DEFAULT_BUFF_SIZE];
        int nRead;
        while ((nRead = helper.readDownloadBytes(buffer, ioAdapter)) != -1) {
            downloadHolder.appendBytes(buffer, 0, nRead);
            bytesRead += nRead;
        }
    }

    public void cancel() {
        try {
            ioAdapter.close();
            downloadHolder.flush();
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, e.getMessage(), e);
        }
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public int getSize() {
        return size;
    }
}
