package org.ivan.downloader.protocols;

import org.ivan.downloader.interaction.IOAdapter;

import java.io.IOException;

/**
 * Encapsulates concrete protocol download interaction.
 * May keep state and so should be used with single {@link org.ivan.downloader.interaction.IOAdapter}.
 * Checks if range is supported by peer, send download request to peer, parse download bytes.
 * <p>
 * Created by ivan on 10.07.2014.
 */
public interface ProtocolHelper {
    byte[] getRequestMessage(int offset, int length);
    byte[] getRequestMessage(int offset);
    byte[] getRequestMessage();
    /**
     * Read and parse download bytes from the remote peer
     * @param buffer a buffer to store download bytes
     * @param ioAdapter an adapter of the remote peer
     * @return number of bytes saved to the buffer or -1 if end of stream reached
     * @throws IOException
     */
    int readDownloadBytes(byte[] buffer, IOAdapter ioAdapter) throws IOException;
    byte[] checkRangeDownloadMessage();
    /**
     * Determine if range is supported by a remote peer
     * @param ioAdapter an adapter of the remote peer
     * @return true if partial download is supported or false otherwise (or unknown)
     * @throws IOException
     */
    boolean isRangeSupported(IOAdapter ioAdapter) throws IOException;
    /**
     * Determine download size in bytes if is capable too
     * @return download size or 0 if size is unknown
     */
    int getSize();
}
