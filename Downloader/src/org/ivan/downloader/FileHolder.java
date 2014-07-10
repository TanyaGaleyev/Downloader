package org.ivan.downloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by ivan on 10.07.2014.
 */
public class FileHolder implements DownloadHolder {
    private final String fileName;

    public FileHolder(String fileName) {
        this.fileName = fileName;
    }

    private FileChannel output;

    @Override
    public void init(long offset) throws IOException {
        if(output != null) throw new IllegalStateException("could not init when output already open");
        output = new RandomAccessFile(fileName, "rw").getChannel();
        output.position(offset);
    }

    @Override
    public void appendBytes(byte[] buffer, int offset, int length) throws IOException {
        output.write(ByteBuffer.wrap(buffer, offset, length));
    }

    @Override
    public void flush() throws IOException {
        if(output == null) return;
        output.close();
        output = null;
    }
}
