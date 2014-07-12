package org.ivan.downloader;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ivan on 10.07.2014.
 */
public interface IOComponent {
    IOAdapter createAdapter(URL url) throws IOException;
//    IOAdapter sendRequest(URL url, byte[] request) throws IOException;
}
