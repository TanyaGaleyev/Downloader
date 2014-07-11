package org.ivan.downloader;

import org.ivan.downloader.messages.CancelWorkerMessage;
import org.ivan.downloader.messages.GetStateMessage;
import org.ivan.downloader.messages.SubmitWorkerMessage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadController implements DownloadObserver {
    public static final String DOWNLOADS_DIR = "downloads";
    private WorkersController workersController;
    private Map<Integer, DownloadHolder> holderMap = new HashMap<>();
    private List<DownloadDescriptor> descriptors = new ArrayList<>();
    private IOComponent ioComponent = new NIOComponent();
    private Map<Integer, DownloadState> notRunningDownloads = Collections.synchronizedMap(new HashMap<Integer, DownloadState>());
    private int counter = 0;

    public DownloadController() {
        new File(DOWNLOADS_DIR).mkdirs();
        workersController = PoolWorkersController.startController(this);
    }

    public DownloadDescriptor startDownload(URL url) throws IOException {
        DownloadDescriptor d = new DownloadDescriptor(url, counter++);
        descriptors.add(d);
        ProtocolHelper helper = ProtocolHelperProvider.getHelper(url.getProtocol());
        File file = new File(DOWNLOADS_DIR + File.separator + "dld" + d.getUid());
        file.delete();
        DownloadHolder downloadHolder = new FileHolder(file);
        holderMap.put(d.getUid(), downloadHolder);
        downloadHolder.init(0);
        helper.isRangeSupported(ioComponent.sendRequest(url, helper.checkRangeDownloadMessage(url)));
        workersController.sendMessage(new SubmitWorkerMessage(d.getUid(), new DownloadWorker(
                ioComponent.sendRequest(url, helper.getRequestMessage(url)),
                helper,
                downloadHolder)));
        return d;
    }

    public void pauseDownload(final DownloadDescriptor d) {
        workersController.sendMessage(new CancelWorkerMessage(d.getUid()));
    }

    public void resumeDownload(DownloadDescriptor d) throws IOException {
        DownloadState state = notRunningDownloads.remove(d.getUid());
        if(state != null && state.getStateCode() == DownloadState.StateCode.PAUSED) {
            // TODO check concurrent retrieve state here
            DownloadHolder downloadHolder = holderMap.get(d.getUid());
            downloadHolder.init(state.getBytesRead());
            URL url = d.getUrl();
            ProtocolHelper helper = ProtocolHelperProvider.getHelper(url.getProtocol());
            workersController.sendMessage(new SubmitWorkerMessage(d.getUid(), new DownloadWorker(
                    ioComponent.sendRequest(url, helper.getRequestMessage(url, state.getBytesRead())),
                    helper,
                    downloadHolder,
                    state)));
        }
    }

    public List<DownloadDescriptor> getDescriptors() {
        return descriptors;
    }

    public void requestStateAsync(DownloadDescriptor d, Callback<DownloadState> callback) {
        DownloadState state;
        if((state = notRunningDownloads.get(d.getUid())) != null)
            callback.process(state);
        else {
            workersController.sendMessage(new GetStateMessage(d.getUid()), callback);
        }
    }

    public void release() {
        workersController.stopAll();
    }

    @Override
    public void onWorkerStopped(int id, DownloadWorker worker) {
        notRunningDownloads.put(id, worker.getState());
    }
}
