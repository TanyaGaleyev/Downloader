package org.ivan.downloader;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ivan on 10.07.2014.
 */
public interface IOComponent {
    IOWrapper sendRequest(URL url, byte[] request) throws IOException;
}
