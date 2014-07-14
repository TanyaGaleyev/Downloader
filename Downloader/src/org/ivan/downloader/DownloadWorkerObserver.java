package org.ivan.downloader;

import org.ivan.downloader.worker.DownloadWorker;

/**
 * Download worker lifecycle observer
 * <p>
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 14:03
 */
public interface DownloadWorkerObserver {
    void onWorkerStopped(int id, DownloadWorker worker);
    void onWorkerComplete(int id, DownloadWorker worker);
    void onWorkerError(int id, DownloadWorker worker, String message);
}
