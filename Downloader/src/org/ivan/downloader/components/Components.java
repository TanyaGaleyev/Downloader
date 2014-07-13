package org.ivan.downloader.components;

import org.ivan.downloader.connection.IOAdapter;
import org.ivan.downloader.protocols.ProtocolHelper;

/**
 * Created by ivan on 13.07.2014.
 */
public class Components {
    private IOAdapter ioAdapter;
    private ProtocolHelper helper;

    public Components(IOAdapter ioAdapter, ProtocolHelper helper) {
        this.ioAdapter = ioAdapter;
        this.helper = helper;
    }

    public IOAdapter getIOAdapter() {
        return ioAdapter;
    }

    public ProtocolHelper getHelper() {
        return helper;
    }
}
