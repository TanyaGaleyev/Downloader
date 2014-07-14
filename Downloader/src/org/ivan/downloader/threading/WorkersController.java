package org.ivan.downloader.threading;

import org.ivan.downloader.DownloadWorkerObserver;
import org.ivan.downloader.threading.messages.Message;

/**
 * Designed to asynchronously control download workers. It most likely implements Message Loop.
 * <p>
 * Created by ivan on 10.07.2014.
 */
public interface WorkersController {
    void startController(DownloadWorkerObserver observer);
    void sendMessage(Message message);
    <T> void sendMessage(Message message, Callback<T> callback);
    void stopAll();
}
