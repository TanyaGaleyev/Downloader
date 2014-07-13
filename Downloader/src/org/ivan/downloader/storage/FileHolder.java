package org.ivan.downloader.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
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

    private FileChannel channel;

    @Override
    public void init(long offset) throws IOException {
        if(channel != null) throw new IllegalStateException("could not init when channel already open");
        channel = new RandomAccessFile(file, "rw").getChannel();
        channel.position(offset);
    }

    @Override
    public int appendBytes(byte[] buffer, int offset, int length) throws IOException {
        return channel.write(ByteBuffer.wrap(buffer, offset, length));
    }

    private InputStream inputStream = null;
    @Override
    public int readBytes(byte[] buffer) throws IOException {
        // TODO replace with read direct from channel, wrap with InputStream for simplicity
        if(inputStream == null) inputStream = Channels.newInputStream(channel);
        return inputStream.read(buffer);
    }

    @Override
    public void flush() throws IOException {
        if(channel == null) return;
        channel.close();
        channel = null;
    }

    @Override
    public void clear() {
        file.delete();
    }
}
