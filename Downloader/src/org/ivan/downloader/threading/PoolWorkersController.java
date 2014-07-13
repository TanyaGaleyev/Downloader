package org.ivan.downloader.threading;

import org.ivan.downloader.DownloadObserver;
import org.ivan.downloader.threading.messages.*;
import org.ivan.downloader.worker.DownloadWorker;

import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
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

    public PoolWorkersController() {}

    public void startController(DownloadObserver observer) {
        downloadObserver = observer;
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        processMessage();
                    }
                } catch (InterruptedException e) {
                    Logger.getGlobal().log(Level.INFO, "Message pool interrupted", e);
                }
            }
        });
    }

    private void processMessage() throws InterruptedException {
        try {
            MessageUnit mu = messageQueue.take();
            Message m = mu.msg;
            Object result = null;
            if (m instanceof SubmitWorkerMessage) {
                SubmitWorkerMessage swm = ((SubmitWorkerMessage) m);
                submitWorker(swm.getUid(), swm.getWorker());
            } else if (m instanceof CancelWorkerMessage) {
                CancelWorkerMessage cm = ((CancelWorkerMessage) m);
                result = cancelWorker(cm.getUid());
            } else if (m instanceof GetStateMessage) {
                GetStateMessage gsm = ((GetStateMessage) m);
                result = getWorker(gsm.getUid());
            } else if(m instanceof ShutdownMessage) {
                shutdownController();
            }
            mu.cb.process(result);
        } catch (RuntimeException e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void submitWorker(final int uid, final DownloadWorker worker) {
        Future<?> future = pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    worker.performDownload();
                    downloadObserver.onWorkerComplete(uid, worker);
                } catch (ClosedByInterruptException e) {
                    downloadObserver.onWorkerStopped(uid, worker);
                    Logger.getGlobal().log(Level.INFO, "worker canceled", e);
                } catch (IOException e) {
                    downloadObserver.onWorkerError(uid, worker, formatMessage(e));
                    Logger.getGlobal().log(Level.INFO, e.getClass().getName(), e);
                } catch (Exception e) {
                    downloadObserver.onWorkerError(uid, worker, formatMessage(e));
                    Logger.getGlobal().log(Level.SEVERE, e.getClass().getName(), e);
                } finally {
                    // need to avoid message loop put interruption exception
                    Thread.interrupted();
                    sendMessage(new CancelWorkerMessage(uid, true), new Callback<Object>() {
                        @Override
                        public void process(Object result) {
                            workerMap.remove(uid);
                        }
                    });
                }
            }
        });
        workerMap.put(uid, new WorkerPair(worker, future));
    }

    private String formatMessage(Exception e) {
        return String.format("%s: %s", e.getClass().getName(), e.getMessage() != null ? e.getMessage() : "");
    }

    private DownloadWorker cancelWorker(int uid) {
        WorkerPair pair = workerMap.get(uid);
        if(pair != null) {
            cancelWorker(pair);
            return pair.worker;
        } else {
            return null;
        }
    }

    private void cancelWorker(WorkerPair pair) {
        pair.future.cancel(true);
        pair.worker.cancel();
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
        sendMessage(new ShutdownMessage());
    }

    private void shutdownController() {
        for(WorkerPair wp : workerMap.values())
            cancelWorker(wp);
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
