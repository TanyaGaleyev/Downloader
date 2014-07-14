package org.ivan.downloader.threading;

import org.ivan.downloader.DownloadWorkerObserver;
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
 * {@link org.ivan.downloader.threading.WorkersController} implementation that uses
 * cached thread pool and a message queue. Each {@link org.ivan.downloader.worker.DownloadWorker}
 * executes in separate thread. Message queue is used to control workers lifecycle.
 * Message queue thread is interruptible. After it finishes it requests shutdown for all workers.
 * <p>
 * Each worker thread works fully independently. It is designed to execute each download session entirely.
 * In other words worker thread does not wait for some events from outside.
 * It fully manages external resources (files, sockets) associated with it.
 * It only could be interrupted to pause download.
 * <p>
 * External components interact with worker thread through {@link org.ivan.downloader.DownloadWorkerObserver} notifications.
 * Such notifications are spawned when worker lifecycle event occurs.
 * This events is download completion, error occurrence, external download cancel.
 * <p>
 * Created by ivan on 10.07.2014.
 */
public class PoolWorkersController implements WorkersController {
    private ExecutorService pool = Executors.newCachedThreadPool();
    private Map<Integer, WorkerPair> workerMap = new HashMap<>();
    private BlockingQueue<MessageUnit> messageQueue = new LinkedBlockingQueue<>();
    private DownloadWorkerObserver downloadWorkerObserver;
    private static class MessageUnit {
        Message msg;
        Callback cb;

        private MessageUnit(Message msg, Callback cb) {
            this.msg = msg;
            this.cb = cb;
        }
    }

    public PoolWorkersController() {}

    public void startController(DownloadWorkerObserver observer) {
        downloadWorkerObserver = observer;
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.interrupted()) {
                        processMessage();
                    }
                } catch (InterruptedException e) {
                    Logger.getGlobal().log(Level.INFO, "Message queue thread was interrupted", e);
                }
                shutdownController();
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
                    downloadWorkerObserver.onWorkerComplete(uid, worker);
                } catch (ClosedByInterruptException e) {
                    downloadWorkerObserver.onWorkerStopped(uid, worker);
                    Logger.getGlobal().log(Level.INFO, "worker canceled", e);
                } catch (IOException e) {
                    downloadWorkerObserver.onWorkerError(uid, worker, formatMessage(e));
                    Logger.getGlobal().log(Level.INFO, e.getClass().getName(), e);
                } catch (Exception e) {
                    downloadWorkerObserver.onWorkerError(uid, worker, formatMessage(e));
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
