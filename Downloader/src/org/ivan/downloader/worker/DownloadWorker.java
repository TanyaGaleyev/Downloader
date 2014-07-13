package org.ivan.downloader.worker;

import org.ivan.downloader.connection.IOAdapter;
import org.ivan.downloader.protocols.ProtocolHelper;
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
        helper.requestDownload(ioAdapter, bytesRead);
        isRangeSupported = helper.isRangeSupported(ioAdapter);
        if(size <= 0) size = helper.getSize();
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
