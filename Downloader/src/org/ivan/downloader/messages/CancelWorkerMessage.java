package org.ivan.downloader.messages;

/**
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 12:12
 */
public class CancelWorkerMessage implements Message {
    private int uid;

    public CancelWorkerMessage(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }
}
