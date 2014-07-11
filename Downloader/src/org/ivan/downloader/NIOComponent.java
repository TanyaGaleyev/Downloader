package org.ivan.downloader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by ivan on 10.07.2014.
 */
public class NIOComponent implements IOComponent {

    @Override
    public IOWrapper sendRequest(URL url, byte[] request) throws IOException {
        final SocketChannel socketChannel = SocketChannel.open(
                new InetSocketAddress(url.getHost(), url.getPort() != -1 ? url.getPort() : url.getDefaultPort()));
        socketChannel.write(ByteBuffer.wrap(request));
        return new IOWrapper() {
            // TODO buffers sizes should fit each other
            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
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
            public void close() throws IOException {
                socketChannel.close();
            }
        };
    }
}
