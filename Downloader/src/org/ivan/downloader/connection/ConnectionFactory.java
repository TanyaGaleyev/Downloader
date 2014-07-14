package org.ivan.downloader.connection;

import org.ivan.downloader.protocols.ProtocolConnection;

import java.io.IOException;
import java.net.URL;

/**
 * Abstract factory providing connection
 * <p>
 * Created by ivan on 13.07.2014.
 */
public interface ConnectionFactory {
    ProtocolConnection createConnection(URL url);
}
