package org.ivan.downloader.storage;

import java.io.IOException;

/**
 * Represents storage for downloads. It could be file, buffer in memory, pipe to remote storage.
 * This holder is reusable to support downloading by parts.
 * <p>
 * General usage strategy is {@code init -> appendBytes* -> flush}.
 * <p>
 * Created by ivan on 10.07.2014.
 */
public interface DownloadHolder {
    /**
     * Init peer to storage with given offset. Offset is used to be able resume downloads.
     * @param offset position at which we will continue write download
     * @throws IOException
     */
    void init(long offset) throws IOException;
    /**
     * Appends bytes to storage represented by this holder
     * @param buffer byte array that will be appended to
     * @param offset first byte to write position
     * @param length number of bytes to append
     * @return number of bytes actually appended
     * @throws IOException
     */
    int appendBytes(byte[] buffer, int offset, int length) throws IOException;
    /**
     * Write remain in internal buffers bytes to storage and
     * release external resources associated with this holder if any
     * @throws IOException
     */
    void flush() throws IOException;
    /**
     * Delete external peer storage if capable. It is useful when we need to remove partial content.
     */
    void clear();
}
