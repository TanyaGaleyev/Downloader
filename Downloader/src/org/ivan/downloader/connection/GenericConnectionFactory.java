package org.ivan.downloader.connection;

import org.ivan.downloader.io.IOFactory;
import org.ivan.downloader.protocols.ProtocolConnection;
import org.ivan.downloader.protocols.ProtocolConnectionProvider;

import java.io.IOException;
import java.net.URL;

/**
 * Generic connection factory works with different IO and Protocol parser implementations
 * <p>
 * Created by ivan on 13.07.2014.
 */
public class GenericConnectionFactory implements ConnectionFactory {
    private final IOFactory ioFactory;
    private final ProtocolConnectionProvider helperProvider;

    public GenericConnectionFactory(IOFactory ioFactory, ProtocolConnectionProvider helperProvider) {
        this.ioFactory = ioFactory;
        this.helperProvider = helperProvider;
    }

    @Override
    public ProtocolConnection createConnection(URL url) {
        return helperProvider.getHelper(url, ioFactory.createAdapter(url));
    }
}
