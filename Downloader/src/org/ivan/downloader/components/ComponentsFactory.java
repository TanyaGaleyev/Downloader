package org.ivan.downloader.components;

import java.io.IOException;
import java.net.URL;

/**
 * Created by ivan on 13.07.2014.
 */
public interface ComponentsFactory {
    Components createComponents(URL url) throws IOException;
}
