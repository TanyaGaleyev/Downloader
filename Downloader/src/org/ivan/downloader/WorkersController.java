package org.ivan.downloader;

/**
 * Created by ivan on 10.07.2014.
 */
public interface WorkersController {
    void submitWorker(int uid, DownloadWorker worker);
    DownloadWorker cancelWorker(int uid);
    DownloadWorker getWorker(int uid);
    void stopAll();
}
