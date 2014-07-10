package org.ivan.downloader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ivan on 10.07.2014.
 */
public class PoolWorkersController implements WorkersController {
    private ExecutorService pool = Executors.newCachedThreadPool();
    private Map<Integer, WorkerPair> workerMap = new HashMap<>();

    @Override
    public void submitWorker(final int uid, final DownloadWorker worker) {
        Future<?> future = pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    worker.performDownload();
                } catch (Exception e) {
                    Logger.getLogger(PoolWorkersController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                } finally {
                    cancelWorker(uid);
                }
            }
        });
        workerMap.put(uid, new WorkerPair(worker, future));
    }

    // TODO smells like synchronization
    @Override
    public DownloadWorker cancelWorker(int uid) {
        WorkerPair pair = workerMap.remove(uid);
        if(pair != null) {
            pair.worker.cancel();
            pair.future.cancel(true);
            return pair.worker;
        } else {
            return null;
        }
    }

    @Override
    public DownloadWorker getWorker(int uid) {
        return workerMap.get(uid).worker;
    }

    @Override
    public void stopAll() {
        pool.shutdownNow();
    }

    private class WorkerPair {
        DownloadWorker worker;
        Future<?> future;

        private WorkerPair(DownloadWorker worker, Future<?> future) {
            this.worker = worker;
            this.future = future;
        }
    }
}
