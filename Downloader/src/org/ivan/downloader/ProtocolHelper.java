package org.ivan.downloader;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ivan on 10.07.2014.
 */
public interface ProtocolHelper {
    byte[] getRequestMessage(URL url, int offset, int length);
    byte[] getRequestMessage(URL url, int offset);
    byte[] getRequestMessage(URL url);
    int readDownloadBytes(byte[] buffer, IOWrapper ioWrapper) throws IOException;
    byte[] checkRangeDownloadMessage(URL url);
    boolean isRangeSupported(IOWrapper ioWrapper) throws IOException;
}
