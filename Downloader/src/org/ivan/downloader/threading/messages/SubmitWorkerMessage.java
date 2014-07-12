package org.ivan.downloader.threading.messages;

import org.ivan.downloader.threading.DownloadWorker;

/**
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 12:11
 */
public class SubmitWorkerMessage implements Message {
    private DownloadWorker worker;
    private int uid;

    public SubmitWorkerMessage(int uid, DownloadWorker worker) {
        this.worker = worker;
        this.uid = uid;
    }

    public DownloadWorker getWorker() {
        return worker;
    }

    public int getUid() {
        return uid;
    }
}
