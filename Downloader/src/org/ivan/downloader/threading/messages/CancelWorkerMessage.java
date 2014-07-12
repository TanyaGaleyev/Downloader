package org.ivan.downloader.threading.messages;

/**
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 12:12
 */
public class CancelWorkerMessage implements Message {
    private int uid;
    private boolean stop;

    public CancelWorkerMessage(int uid, boolean stop) {
        this.uid = uid;
        this.stop = stop;
    }

    public int getUid() {
        return uid;
    }

    public boolean isStop() {
        return stop;
    }
}
