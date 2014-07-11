package org.ivan.downloader;

import org.ivan.downloader.messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ivan on 10.07.2014.
 */
public class PoolWorkersController implements WorkersController {
    private ExecutorService pool = Executors.newCachedThreadPool();
    private Map<Integer, WorkerPair> workerMap = new HashMap<>();
    private BlockingQueue<MessageUnit> messageQueue = new LinkedBlockingQueue<>();
    private DownloadObserver downloadObserver;
    private static class MessageUnit {
        Message msg;
        Callback cb;

        private MessageUnit(Message msg, Callback cb) {
            this.msg = msg;
            this.cb = cb;
        }
    }

    private PoolWorkersController() {}

    public static PoolWorkersController startController(DownloadObserver observer) {
        final PoolWorkersController controller = new PoolWorkersController();
        controller.downloadObserver = observer;
        controller.pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        controller.processMessage();
                    }
                } catch (InterruptedException e) {
                    Logger.getLogger(PoolWorkersController.class.getName()).log(Level.WARNING, e.getMessage(), e);
                }
            }
        });
        return controller;
    }

    private void processMessage() throws InterruptedException {
        try {
            MessageUnit mu = messageQueue.take();
            Message m = mu.msg;
            Object result = null;
            if (m instanceof SubmitWorkerMessage) {
                SubmitWorkerMessage sm = ((SubmitWorkerMessage) m);
                submitWorker(sm.getUid(), sm.getWorker());
            } else if (m instanceof CancelWorkerMessage) {
                CancelWorkerMessage cm = ((CancelWorkerMessage) m);
                result = cancelWorker(cm.getUid());
            } else if (m instanceof GetStateMessage) {
                GetStateMessage gsm = ((GetStateMessage) m);
                result = getWorker(gsm.getUid()).getState();
            }
            mu.cb.process(result);
        } catch (RuntimeException e) {
            Logger.getLogger(PoolWorkersController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void submitWorker(final int uid, final DownloadWorker worker) {
        Future<?> future = pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    worker.performDownload();
                } catch (Exception e) {
                    Logger.getLogger(PoolWorkersController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                } finally {
                    Thread.interrupted();
                    sendMessage(new EmptyMessage(), new Callback<Object>() {
                        @Override
                        public void process(Object result) {
                            workerMap.remove(uid);
                            worker.cancel();
                            downloadObserver.onWorkerStopped(uid, worker);
                        }
                    });
                }
            }
        });
        workerMap.put(uid, new WorkerPair(worker, future));
    }

    // TODO smells like synchronization
    private DownloadWorker cancelWorker(int uid) {
        WorkerPair pair = workerMap.get(uid);
        if(pair != null) {
            pair.future.cancel(true);
            pair.worker.cancel();
            return pair.worker;
        } else {
            return null;
        }
    }

    @Override
    public void sendMessage(Message message) {
        sendMessage(message, new Callback<Object>() {
            @Override
            public void process(Object result) {}
        });
    }

    @Override
    public <T> void sendMessage(Message message, Callback<T> callback) {
        try {
            messageQueue.put(new MessageUnit(message, callback));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private DownloadWorker getWorker(int uid) {
        WorkerPair pair = workerMap.get(uid);
        return pair == null ? null : pair.worker;
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
