package org.ivan.downloader;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by ivan on 10.07.2014.
 */
public interface IOAdapter extends Closeable {
    void open() throws IOException;
    int read(byte[] buffer) throws IOException;
    int write(byte[] buffer) throws IOException;
    int write(byte[] buffer, int offset, int length) throws IOException;
    @Override
    void close() throws IOException;
}
