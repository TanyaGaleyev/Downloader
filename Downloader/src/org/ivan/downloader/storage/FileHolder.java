package org.ivan.downloader.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Holder that store download bytes to filesystem
 * <p>
 * Created by ivan on 10.07.2014.
 */
public class FileHolder implements DownloadHolder {
    private final File file;

    public FileHolder(String fileName) {
        this.file = new File(fileName);
    }

    public FileHolder(File file) {
        this.file = file;
    }

    private FileChannel output;

    @Override
    public void init(long offset) throws IOException {
        if(output != null) throw new IllegalStateException("could not init when output already open");
        output = new RandomAccessFile(file, "rw").getChannel();
        output.position(offset);
    }

    @Override
    public int appendBytes(byte[] buffer, int offset, int length) throws IOException {
        return output.write(ByteBuffer.wrap(buffer, offset, length));
    }

    @Override
    public void flush() throws IOException {
        if(output == null) return;
        output.close();
        output = null;
    }

    @Override
    public void clear() {
        file.delete();
    }
}
