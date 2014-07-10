package org.ivan.downloader;

/**
 * Created by ivan on 10.07.2014.
 */
public class ProtocolHelperProvider {
    public static ProtocolHelper getHelper(String protocol) {
        if(protocol.trim().equalsIgnoreCase("http"))
            return new HttpHelper();
        else
            throw new IllegalArgumentException("Unknown protocol" + protocol);
    }
}
