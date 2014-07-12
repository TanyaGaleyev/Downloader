package org.ivan.downloader.interaction;

import java.io.Closeable;
import java.io.IOException;

/**
 * We use this interface implementations to standard IO operations in different IO implementations
 * (IO streams, NIO channels, some native IO, etc.)
 * <p>
 * Created by ivan on 10.07.2014.
 */
public interface IOAdapter extends Closeable {
    /**
     * Open connection. Should be called before read and write operations. Should not be called twice.
     * But implementations could be idempotent.
     * @throws
     */
    void open() throws IOException;

    /**
     * Read bytes to requested buffer
     * @param buffer buffer to store bytes
     * @return number of bytes read or -1 if end of stream reached
     * @throws IOException
     */
    int read(byte[] buffer) throws IOException;

    /**
     * Write bytes to corresponding output
     * <p>
     * Same as {@link IOAdapter#write(byte[] buffer, int offset, int length)}
     * with offset = 0 and length = buffer.length
     * @param buffer buffer to output
     * @return number of bytes actually written
     * @throws IOException
     */
    int write(byte[] buffer) throws IOException;

    /**
     * Write bytes to corresponding output
     * @param buffer bytes to output
     * @param offset bytes from this position will be written to output
     * @param length number of bytes to output
     * @return number of bytes actually written
     * @throws IOException
     */
    int write(byte[] buffer, int offset, int length) throws IOException;

    /**
     * After we finish with this adapter in should be close.
     * Could be idempotent.
     * <p>
     * See also default description {@link java.io.Closeable}
     * @throws IOException
     */
    @Override
    void close() throws IOException;
}
