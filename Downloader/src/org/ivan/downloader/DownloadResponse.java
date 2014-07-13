package org.ivan.downloader;

import org.ivan.downloader.storage.DownloadHolder;

/**
 * Created by ivan on 13.07.2014.
 */
public class DownloadResponse {
    private DownloadHolder holder;
    private DownloadState.StateCode stateCode;

    public DownloadResponse(DownloadHolder holder, DownloadState.StateCode stateCode) {
        this.holder = holder;
        this.stateCode = stateCode;
    }
}
