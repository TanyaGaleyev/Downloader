package org.ivan.downloader.protocols;

import java.io.IOException;

/**
 * Encapsulates concrete protocol download interaction
 * (configure connection and receive content).
 * Checks if range is supported by peer, send download request to peer, parse download bytes.
 * <p>
 * Created by ivan on 10.07.2014.
 */
public interface ProtocolConnection {
    void connect() throws IOException;
    void disconnect() throws IOException;
    /**
     * Sends download from specified offset request to server
     * @param offset position to continue download
     * @throws IOException
     */
    void requestDownload(int offset) throws IOException;
    /**
     * Read and parse download bytes from the remote peer
     * @param buffer a buffer to store download bytes
     * @return number of bytes saved to the buffer or -1 if end of stream reached
     * @throws IOException
     */
    int readDownloadBytes(byte[] buffer) throws IOException;
    /**
     * Determine if range is supported by a remote peer
     * @return true if partial download is supported or false otherwise (or unknown)
     * @throws IOException
     */
    boolean isRangeSupported() throws IOException;
    /**
     * Determine download size in bytes if is capable too
     * @return download size or 0 if size is unknown
     */
    int getSize();
}
