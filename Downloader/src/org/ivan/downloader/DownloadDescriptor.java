package org.ivan.downloader;

import java.net.URL;
import java.util.Date;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadDescriptor {
    private URL url;
    private int uid;
    private Date startDate;

    public DownloadDescriptor(URL url, int uid) {
        this.url = url;
        this.uid = uid;
        startDate = new Date();
    }

    public URL getUrl() {
        return url;
    }

    public int getUid() {
        return uid;
    }

    public Date getStartDate() {
        return startDate;
    }
}
