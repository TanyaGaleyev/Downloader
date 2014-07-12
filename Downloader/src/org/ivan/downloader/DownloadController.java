package org.ivan.downloader;

import org.ivan.downloader.messages.CancelWorkerMessage;
import org.ivan.downloader.messages.GetStateMessage;
import org.ivan.downloader.messages.SubmitWorkerMessage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadController implements DownloadObserver {
    public static final String DOWNLOADS_DIR = "downloads";
    private WorkersController workersController;
    private List<DownloadDescriptor> descriptors = new ArrayList<>();
    private IOComponent ioComponent = new NIOComponent();
    private int counter = 0;
    private Map<Integer, DownloadTuple> downloadTupleMap = new ConcurrentHashMap<>();

    public DownloadController() {
        new File(DOWNLOADS_DIR).mkdirs();
        workersController = PoolWorkersController.startController(this);
    }

    public DownloadDescriptor startDownload(URL url) throws IOException {
        DownloadDescriptor d = new DownloadDescriptor(url, counter++);
        descriptors.add(d);
        DownloadTuple dl = new DownloadTuple();
        dl.stateCode = DownloadState.StateCode.NOT_STARTED;
        dl.bytesRead = 0;
        downloadTupleMap.put(d.getUid(), dl);
        synchronized (dl) {
            startDownload(d, dl);
        }
        return d;
    }

    private void startDownload(DownloadDescriptor d, DownloadTuple dl) throws IOException {
        URL url = d.getUrl();
        ProtocolHelper helper = ProtocolHelperProvider.getHelper(url);
        if(dl.holder == null)
            dl.holder = constructHolder(d);
        dl.holder.init(dl.bytesRead);
        // todo try to send request inside worker
//        helper.isRangeSupported(ioComponent.sendRequest(url, helper.checkRangeDownloadMessage(url)));
        DownloadWorker worker = new DownloadWorker(ioComponent.createAdapter(url), helper, dl.holder, dl.bytesRead, dl.size);
        dl.worker = worker;
        dl.stateCode = DownloadState.StateCode.IN_PROGRESS;
        workersController.sendMessage(new SubmitWorkerMessage(d.getUid(), worker));
    }

    private DownloadHolder constructHolder(DownloadDescriptor d) {
        File file = new File(DOWNLOADS_DIR + File.separator + "dld" + d.getUid());
        file.delete();
        return new FileHolder(file);
    }

    public void pauseDownload(final DownloadDescriptor d) {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if(dl.stateCode == DownloadState.StateCode.IN_PROGRESS) {
                dl.stateCode = DownloadState.StateCode.PAUSED;
                dl.worker = null;
                workersController.sendMessage(new CancelWorkerMessage(d.getUid(), false));
            }
        }
    }

    public void resumeDownload(DownloadDescriptor d) throws IOException {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if (dl.stateCode != DownloadState.StateCode.COMPLETE &&
                    dl.stateCode != DownloadState.StateCode.IN_PROGRESS) {
                startDownload(d, dl);
            }
        }
    }

    public void stopDownload(DownloadDescriptor d) {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if (dl.stateCode == DownloadState.StateCode.IN_PROGRESS) {
                workersController.sendMessage(new CancelWorkerMessage(d.getUid(), true));
            }
            if (dl.stateCode != DownloadState.StateCode.COMPLETE) {
                dl.stateCode = DownloadState.StateCode.NOT_STARTED;
                dl.holder.clear();
                dl.holder = null;
                dl.bytesRead = 0;
            }
        }
    }

    public List<DownloadDescriptor> getDescriptors() {
        return descriptors;
    }

    public void requestState(DownloadDescriptor d, final Callback<DownloadState> callback) {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if (dl.stateCode != DownloadState.StateCode.IN_PROGRESS) {
                DownloadState state = new DownloadState();
                state.setBytesRead(dl.bytesRead);
                state.setStateCode(dl.stateCode);
                state.setLength(dl.size);
                callback.process(state);
            } else {
                workersController.sendMessage(new GetStateMessage(d.getUid()), new Callback<DownloadWorker>() {
                    @Override
                    public void process(DownloadWorker result) {
                        DownloadState state = new DownloadState();
                        state.setBytesRead(result.getBytesRead());
                        state.setStateCode(DownloadState.StateCode.IN_PROGRESS);
                        state.setLength(result.getSize());
                        callback.process(state);
                    }
                });
            }
        }
    }

    public void release() {
        workersController.stopAll();
    }

    @Override
    public void onWorkerStopped(int id, DownloadWorker worker) {
        DownloadTuple dl = downloadTupleMap.get(id);
        synchronized (dl) {
            dl.worker = null;
            if (dl.stateCode == DownloadState.StateCode.PAUSED) {
                // todo paused error
                dl.bytesRead = worker.getBytesRead();
                dl.size = worker.getSize();
            }
        }
    }

    @Override
    public void onWorkerComplete(int id, DownloadWorker worker) {
        DownloadTuple dl = downloadTupleMap.get(id);
        synchronized (dl) {
            dl.worker = null;
            dl.stateCode = DownloadState.StateCode.COMPLETE;
            dl.bytesRead = worker.getBytesRead();
            dl.size = worker.getSize();
        }
    }

    @Override
    public void onWorkerError(int id, DownloadWorker worker) {
        DownloadTuple dl = downloadTupleMap.get(id);
        synchronized (dl) {
            dl.worker = null;
            dl.stateCode = DownloadState.StateCode.PAUSED_ERROR;
            dl.bytesRead = worker.getBytesRead();
            dl.size = worker.getSize();
        }
    }

    class DownloadTuple {
        DownloadState.StateCode stateCode = DownloadState.StateCode.NOT_STARTED;
        int bytesRead = 0;
        int size = 0;
        DownloadHolder holder = null;
        DownloadWorker worker = null;
    }
}
