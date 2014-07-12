package org.ivan.downloader;

import java.net.URL;

/**
 * Created by ivan on 10.07.2014.
 */
public class ProtocolHelperProvider {
    public static ProtocolHelper getHelper(URL url) {
        if(url.getProtocol().trim().equalsIgnoreCase("http"))
            return new HttpHelper(url);
        else
            throw new IllegalArgumentException("Unknown protocol" + url.getProtocol());
    }
}
