package org.ivan.downloader.threading.messages;

/**
 * User: Ivan Pavlukhin
 * Date: 11.07.2014
 * Time: 13:05
 */
public class GetStateMessage implements Message {
    private int uid;

    public GetStateMessage(int uid) {
        this.uid = uid;
    }

    public int getUid() {
        return uid;
    }
}
