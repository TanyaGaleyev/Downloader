package org.ivan.downloader;

/**
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 14:03
 */
public interface DownloadObserver {
    void onWorkerStopped(int id, DownloadWorker worker);
}
