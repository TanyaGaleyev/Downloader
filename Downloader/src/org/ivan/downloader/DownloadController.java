package org.ivan.downloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadController {
    public static final String DOWNLOADS_DIR = "downloads";
    private WorkersController workersController = new PoolWorkersController();
    private Map<Integer, DownloadHolder> holderMap = new HashMap<>();
    private List<DownloadDescriptor> descriptors = new ArrayList<>();
    private IOComponent ioComponent = new NIOComponent();
    private Map<Integer, DownloadState> notRunningDownloads = new HashMap<>();
    private int counter = 0;

    public DownloadController() {
        new File(DOWNLOADS_DIR).mkdirs();
    }

    public DownloadDescriptor startDownload(URL url) throws IOException {
        DownloadDescriptor d = new DownloadDescriptor(url, counter++);
        descriptors.add(d);
        ProtocolHelper helper = ProtocolHelperProvider.getHelper(url.getProtocol());
        DownloadHolder downloadHolder = new FileHolder(DOWNLOADS_DIR + File.separator + "dld" + d.getUid());
        holderMap.put(d.getUid(), downloadHolder);
        downloadHolder.init(0);
        helper.isRangeSupported(ioComponent.sendRequest(url, helper.checkRangeDownloadMessage(url)));
        workersController.submitWorker(d.getUid(), new DownloadWorker(
                    ioComponent.sendRequest(url, helper.getRequestMessage(url)),
                    helper,
                    downloadHolder));
        return d;
    }

    public void pauseDownload(DownloadDescriptor d) {
        workersController.cancelWorker(d.getUid());
        // TODO check concurrent retrieve state here
    }

    public void resumeDownload(DownloadDescriptor d) {

    }

    public List<DownloadDescriptor> getDescriptors() {
        return descriptors;
    }

    public DownloadState getState(DownloadDescriptor d) {
        return workersController.getWorker(d.getUid()).getState();
    }

    public void release() {
        workersController.stopAll();
    }
}
