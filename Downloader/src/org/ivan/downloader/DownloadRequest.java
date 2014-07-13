package org.ivan.downloader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan on 13.07.2014.
 */
public class DownloadRequest {
    private List<URL> urls = new ArrayList<>();

    public DownloadRequest(List<URL> urls) {
        this.urls = urls;
    }

    public List<URL> getUrls() {
        return urls;
    }
}
