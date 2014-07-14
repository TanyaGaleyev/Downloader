package org.ivan.downloader.worker;

import org.ivan.downloader.protocols.ProtocolConnection;
import org.ivan.downloader.storage.DownloadHolder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that performs download process.
 * <p>
 * Inside this class we use download abstraction at three aspects:
 * <ol>
 *     <li>download peer IO implementation;</li>
 *     <li>download protocol;</li>
 *     <li>storage to which download are saved.</li>
 * </ol>
 * Each aspect is independent and we could create different combination of IO implementations and chosen protocols
 * <p>
 * Created by ivan on 10.07.2014.
 */
public class DownloadWorker {

    public static final int DEFAULT_BUFF_SIZE = 2048;
    private final ProtocolConnection connection;
    private final DownloadHolder downloadHolder;
    private volatile int bytesRead;
    private volatile boolean isRangeSupported;
    private volatile int size;

    public DownloadWorker(ProtocolConnection connection, DownloadHolder downloadHolder) {
        this(connection, downloadHolder, 0, 0);
    }

    public DownloadWorker(ProtocolConnection connection, DownloadHolder downloadHolder, int bytesRead, int size) {
        this.connection = connection;
        this.downloadHolder = downloadHolder;
        this.bytesRead = bytesRead;
        this.size = size;
    }

    public void performDownload() throws IOException {
        connection.connect();
        connection.requestDownload(bytesRead);
        isRangeSupported = connection.isRangeSupported();
        if(size <= 0) size = connection.getSize();
        downloadHolder.init(bytesRead);
        byte[] buffer = new byte[DEFAULT_BUFF_SIZE];
        int nRead;
        while ((nRead = connection.readDownloadBytes(buffer)) != -1) {
            downloadHolder.appendBytes(buffer, 0, nRead);
            bytesRead += nRead;
        }
    }

    public void cancel() {
        try {
            connection.disconnect();
            downloadHolder.flush();
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, e.getMessage(), e);
        }
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public int getSize() {
        return size;
    }

    public boolean isRangeSupported() {
        return isRangeSupported;
    }
}
