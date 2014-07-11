package org.ivan.downloader;

import org.ivan.downloader.messages.Message;

/**
 * Created by ivan on 10.07.2014.
 */
public interface WorkersController {
    void sendMessage(Message message);
    <T> void sendMessage(Message message, Callback<T> callback);
    void stopAll();
}
