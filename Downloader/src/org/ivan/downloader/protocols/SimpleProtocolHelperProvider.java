package org.ivan.downloader.protocols;

import java.net.URL;

/**
 * Creates helpers for different protocols.
 * @see ProtocolHelper
 * <p>
 * Created by ivan on 10.07.2014.
 */
public class SimpleProtocolHelperProvider implements ProtocolHelperProvider {
    @Override
    public ProtocolHelper getHelper(URL url) {
        if(url.getProtocol().trim().equalsIgnoreCase("http"))
            return new HttpHelper(url);
        else
            throw new IllegalArgumentException("Unknown protocol " + url.getProtocol());
    }
}
