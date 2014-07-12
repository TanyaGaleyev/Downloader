package org.ivan.downloader;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ivan on 10.07.2014.
 */
public interface ProtocolHelper {
    byte[] getRequestMessage(int offset, int length);
    byte[] getRequestMessage(int offset);
    byte[] getRequestMessage();
    int readDownloadBytes(byte[] buffer, IOAdapter ioAdapter) throws IOException;
    byte[] checkRangeDownloadMessage();
    boolean isRangeSupported(IOAdapter ioAdapter) throws IOException;
    int getSize();
}
