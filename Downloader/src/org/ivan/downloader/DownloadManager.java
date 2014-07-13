package org.ivan.downloader;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 13.07.2014.
 */
public interface DownloadManager {
    List<DownloadDescriptor> startDownload(DownloadRequest request) throws IOException;

    DownloadDescriptor startDownload(URL url) throws IOException;

    void pauseDownload(DownloadDescriptor d);

    void resumeDownload(DownloadDescriptor d) throws IOException;

    void stopDownload(DownloadDescriptor d);

    Map<Integer, DownloadDescriptor> getDescriptors();

    DownloadState requestState(DownloadDescriptor d);

    void release();

    DownloadResponse getDownload(DownloadDescriptor dd);
}
