package org.ivan.downloader;

import java.io.IOException;

/**
 * Created by ivan on 10.07.2014.
 */
public interface DownloadHolder {
    void init(long offset) throws IOException;
    void appendBytes(byte[] buffer, int offset, int length) throws IOException;
    void flush() throws IOException;
}
