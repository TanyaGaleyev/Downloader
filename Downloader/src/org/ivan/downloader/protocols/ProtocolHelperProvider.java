package org.ivan.downloader.protocols;

import java.net.URL;

/**
 * Created by ivan on 13.07.2014.
 */
public interface ProtocolHelperProvider {
    ProtocolHelper getHelper(URL url);
}
