package org.ivan.downloader.interaction;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO factory that creates adapters to {@link java.nio.channels.SocketChannel}
 * <p>
 * Created by ivan on 10.07.2014.
 */
public class NIOComponent implements IOComponent {
    @Override
    public IOAdapter createAdapter(final URL url) throws IOException {
        return new IOAdapter() {
            SocketChannel socketChannel;
            // TODO buffers sizes should fit each other
            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);

            @Override
            public void open() throws IOException {
                socketChannel = SocketChannel.open(
                        new InetSocketAddress(url.getHost(), url.getPort() != -1 ? url.getPort() : url.getDefaultPort()));
            }

            @Override
            public int read(byte[] buffer) throws IOException {
                int nRead = socketChannel.read(byteBuffer);
                byteBuffer.flip();
                if(nRead > 0 || byteBuffer.limit() > 0) {
                    nRead = Math.min(byteBuffer.limit(), buffer.length);
                    byteBuffer.get(buffer, 0, nRead);
                    byteBuffer.compact();
                }
                return nRead;
            }

            @Override
            public int write(byte[] buffer) throws IOException {
                return write(buffer, 0, buffer.length);
            }

            @Override
            public int write(byte[] buffer, int offset, int length) throws IOException {
                return socketChannel.write(ByteBuffer.wrap(buffer, offset, length));
            }

            @Override
            public void close() throws IOException {
                if(socketChannel != null) socketChannel.close();
            }
        };
    }
}
