package org.ivan.downloader;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Core component for client programmers. Provides all functionality to perform downloads.
 * <p>
 * Created by ivan on 13.07.2014.
 */
public interface DownloadManager {
    /**
     * Start downloads for a given request
     * @param request a download request
     * @return list of download sessions descriptors created for the request
     */
    List<DownloadDescriptor> startDownload(DownloadRequest request);

    /**
     * Start download for a given url
     * @param url an url to download from
     * @return download sessions descriptors created for the request
     */
    DownloadDescriptor startDownload(URL url);

    /**
     * Pause specified download session
     * @param d session descriptor
     */
    void pauseDownload(DownloadDescriptor d);

    /**
     * Resume specified download session
     * @param d session descriptor
     */
    void resumeDownload(DownloadDescriptor d);

    /**
     * Stop specified download session
     * @param d session descriptor
     */
    void stopDownload(DownloadDescriptor d);

    /**
     * @return list of download descriptors submitted to this manager
     */
    Map<Integer, DownloadDescriptor> getDescriptors();

    /**
     * Requests download seesion state
     * @param d session descriptor
     * @return requested state
     */
    DownloadState requestState(DownloadDescriptor d);

    /**
     * Release all resources associated with this DownloadManager
     */
    void release();

    /**
     * Request download result
     * <p>
     * WARNING:
     * <p>
     * Actual download result could be absent if download was not complete yet.
     * Make sure that result is available with {@link DownloadResponse#isReady()} call
     * @param dd download session descriptor
     * @return download result
     */
    DownloadResponse getDownload(DownloadDescriptor dd);
}
