package org.ivan.downloader.protocols;

import org.ivan.downloader.io.IOAdapter;

import java.net.URL;

/**
 * Creates connections for different protocols. Used as sample implementation.
 * <p>
 * Created by ivan on 10.07.2014.
 * @see ProtocolConnection
 */
public class SimpleProtocolConnectionProvider implements ProtocolConnectionProvider {
    @Override
    public ProtocolConnection getHelper(URL url, IOAdapter ioAdapter) {
        if(url.getProtocol().trim().equalsIgnoreCase("http"))
            return new HttpConnection(url, ioAdapter);
        else
            throw new IllegalArgumentException("Unknown protocol " + url.getProtocol());
    }
}
