package org.ivan.downloader;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by ivan on 10.07.2014.
 */
public interface IOWrapper extends Closeable {
    int read(byte[] buffer) throws IOException;
    @Override
    void close() throws IOException;
}
