package org.ivan.downloader.connection;

import java.io.IOException;
import java.net.URL;

/**
 * Factory to create IO adapter to use in Downloader.
 * <p>
 * In general we create single adapter for single download.
 * Downloader use this factory to be able switch underlying IO implementation transparently.
 * <p>
 * Created by ivan on 10.07.2014.
 */
public interface IOComponent {
    /**
     * Creates IO adapter for given url. For example it could be TCP socket for single client-server session.
     * @param url address to connect
     * @return IO adapter for requested url
     * @throws IOException
     */
    IOAdapter createAdapter(URL url) throws IOException;
}
