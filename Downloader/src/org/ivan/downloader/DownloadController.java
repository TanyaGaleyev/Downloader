package org.ivan.downloader;

import org.ivan.downloader.components.Components;
import org.ivan.downloader.components.ComponentsFactory;
import org.ivan.downloader.storage.DownloadHolder;
import org.ivan.downloader.storage.FileHolder;
import org.ivan.downloader.threading.PoolWorkersController;
import org.ivan.downloader.threading.WorkersController;
import org.ivan.downloader.threading.messages.CancelWorkerMessage;
import org.ivan.downloader.threading.messages.SubmitWorkerMessage;
import org.ivan.downloader.worker.DownloadWorker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ivan on 10.07.2014.
 */
public class DownloadController implements DownloadManager, DownloadObserver {
    public static final String DOWNLOADS_DIR = "downloads";
    private WorkersController workersController;
    private Map<Integer, DownloadDescriptor> descriptors = new ConcurrentHashMap<>();
    private ComponentsFactory componentsFactory;
    private int counter = 0;
    private Map<Integer, DownloadTuple> downloadTupleMap = new ConcurrentHashMap<>();

    public DownloadController(ComponentsFactory componentsFactory, WorkersController workersController) {
        this.componentsFactory = componentsFactory;
        this.workersController = workersController;
        new File(DOWNLOADS_DIR).mkdirs();
        workersController.startController(this);
    }

    @Override
    public List<DownloadDescriptor> startDownload(DownloadRequest request) throws IOException {
        List<DownloadDescriptor> descrs = new ArrayList<>();
        for(URL url : request.getUrls())
            descrs.add(startDownload(url));
        return descrs;
    }

    @Override
    public DownloadDescriptor startDownload(URL url) throws IOException {
        DownloadDescriptor d = new DownloadDescriptor(url, counter++);
        descriptors.put(d.getUid(), d);
        DownloadTuple dl = new DownloadTuple();
        dl.state.setStatus(DownloadState.StateCode.NOT_STARTED);
        dl.state.setBytesRead(0);
        downloadTupleMap.put(d.getUid(), dl);
        synchronized (dl) {
            startDownload(d, dl);
        }
        return d;
    }

    private void startDownload(DownloadDescriptor d, DownloadTuple dl) throws IOException {
        URL url = d.getUrl();
        if(dl.holder == null)
            dl.holder = constructHolder(d);
        dl.holder.init(dl.state.getBytesRead());
        Components components = componentsFactory.createComponents(url);
        DownloadWorker worker = new DownloadWorker(
                components.getIOAdapter(), components.getHelper(), dl.holder, dl.state.getBytesRead(), dl.state.getSize());
        dl.worker = worker;
        dl.state.setStatus(DownloadState.StateCode.IN_PROGRESS);
        workersController.sendMessage(new SubmitWorkerMessage(d.getUid(), worker));
    }

    private DownloadHolder constructHolder(DownloadDescriptor d) {
        String[] pathParts = d.getUrl().getPath().split("/");
        String filename = pathParts[pathParts.length - 1];
        filename = filename.split("\\?")[0];
        File file = new File(DOWNLOADS_DIR + File.separator + filename);
        file.delete();
        return new FileHolder(file);
    }

    @Override
    public void pauseDownload(final DownloadDescriptor d) {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if(dl.state.getStateCode() == DownloadState.StateCode.IN_PROGRESS) {
                dl.state.setStatus(DownloadState.StateCode.PAUSED);
                workersController.sendMessage(new CancelWorkerMessage(d.getUid(), false));
            }
        }
    }

    @Override
    public void resumeDownload(DownloadDescriptor d) throws IOException {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if (dl.state.getStateCode() != DownloadState.StateCode.COMPLETE &&
                    dl.state.getStateCode() != DownloadState.StateCode.IN_PROGRESS) {
                startDownload(d, dl);
            }
        }
    }

    @Override
    public void stopDownload(DownloadDescriptor d) {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if (dl.state.getStateCode() == DownloadState.StateCode.IN_PROGRESS) {
                dl.state.setStatus(DownloadState.StateCode.NOT_STARTED);
                workersController.sendMessage(new CancelWorkerMessage(d.getUid(), true));
            } else if (dl.state.getStateCode() != DownloadState.StateCode.COMPLETE &&
                    dl.state.getStateCode() != DownloadState.StateCode.NOT_STARTED) {
                dl.state.setStatus(DownloadState.StateCode.NOT_STARTED);
                clearDownload(dl);
            }
        }
    }

    @Override
    public Map<Integer, DownloadDescriptor> getDescriptors() {
        return descriptors;
    }

    @Override
    public DownloadState requestState(DownloadDescriptor d) {
        DownloadTuple dl = downloadTupleMap.get(d.getUid());
        synchronized (dl) {
            if (dl.worker == null) {
                return new DownloadState(dl.state);
            } else {
                return new DownloadState(
                        DownloadState.StateCode.IN_PROGRESS, dl.worker.getBytesRead(), dl.worker.getSize());
            }
        }
    }

    @Override
    public void release() {
        workersController.stopAll();
    }

    @Override
    public void onWorkerStopped(int id, DownloadWorker worker) {
        DownloadTuple dl = downloadTupleMap.get(id);
        synchronized (dl) {
            dl.worker = null;
            if (dl.state.getStateCode() == DownloadState.StateCode.PAUSED) {
                dl.state.setBytesRead(worker.getBytesRead());
                dl.state.setSize(worker.getSize());
            } else if (dl.state.getStateCode() == DownloadState.StateCode.NOT_STARTED) {
                clearDownload(dl);
            }
        }
    }

    @Override
    public void onWorkerComplete(int id, DownloadWorker worker) {
        DownloadTuple dl = downloadTupleMap.get(id);
        synchronized (dl) {
            dl.worker = null;
            dl.state.setStatus(DownloadState.StateCode.COMPLETE);
            dl.state.setBytesRead(worker.getBytesRead());
            dl.state.setSize(worker.getSize());
            descriptors.get(id).setEndDate(new Date());
        }
    }

    @Override
    public void onWorkerError(int id, DownloadWorker worker, String message) {
        DownloadTuple dl = downloadTupleMap.get(id);
        synchronized (dl) {
            dl.worker = null;
            dl.state.setStatus(DownloadState.StateCode.PAUSED_ERROR, message);
            dl.state.setBytesRead(worker.getBytesRead());
            dl.state.setSize(worker.getSize());
        }
    }

    private void clearDownload(DownloadTuple dl) {
        dl.state.setBytesRead(0);
        dl.holder.clear();
        dl.holder = null;
    }

    @Override
    public DownloadResponse getDownload(DownloadDescriptor dd) {
        DownloadTuple dl = downloadTupleMap.get(dd.getUid());
        synchronized (dl) {
            if(dl.state.getStateCode() == DownloadState.StateCode.COMPLETE)
                return new DownloadResponse(dl.holder, dl.state.getStateCode());
            else
                return new DownloadResponse(null, dl.state.getStateCode());
        }
    }

    class DownloadTuple {
        DownloadState state = new DownloadState();
        DownloadHolder holder = null;
        DownloadWorker worker = null;
    }
}
