package org.ivan.downloader.protocols;

import org.ivan.downloader.io.IOAdapter;

import java.net.URL;

/**
 * Creates connections for different protocols.
 * The main goal of this interface is to decouple IO implementation from protocol interaction implementation
 * <p>
 * Created by ivan on 13.07.2014.
 */
public interface ProtocolConnectionProvider {
    ProtocolConnection getHelper(URL url, IOAdapter ioAdapter);
}
