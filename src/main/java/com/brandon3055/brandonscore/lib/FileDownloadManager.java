package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.LogHelperBC;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by brandon3055 on 5/08/2017.
 * This is a general purpose download manager that can be used to que up
 * files for download and monitor download progress.
 */
public class FileDownloadManager implements Runnable {

    //Monitoring Fields
    public volatile int totalFiles = 0;
    public volatile int filesDownloaded = 0;
    public volatile boolean running = false;
    public volatile boolean downloadsComplete = false;
    public Map<String, File> failedFiles = Collections.synchronizedMap(new HashMap<>());
    //================


    private String name;
    private boolean resetOnFinish;
    private Runnable queCompeteCallback = null;
    private final ThreadFileDownloader[] workers;
    private volatile boolean stopDownload = false;
    private Queue<Pair<String, File>> downloadQue = new ConcurrentLinkedDeque<>();

    public FileDownloadManager(String name, int maxWorkers, boolean resetOnFinish) {
        this.name = name;
        this.workers = new ThreadFileDownloader[maxWorkers];
        this.resetOnFinish = resetOnFinish;
    }

    public void startDownload() {
        if (running) return;
        running = true;
        downloadsComplete = false;
        new Thread(this, name).start();
    }

    @Override
    public void run() {
        //While downloads are in progress
        while (!downloadsComplete) {
            //Synchronize on workers
            synchronized (workers) {

                //If stop has been requested stop all workers and add their files back to the que
                if (stopDownload) {
                    for (int i = 0; i < workers.length; i++) {
                        if (!workers[i].isRunning()) {
                            workers[i].interrupt();
                            downloadQue.add(new Pair<>(workers[i].sourceURL, workers[i].outputFile));
                            workers[i] = null;
                        }
                    }
                    stopDownload = false;
                    break;
                }

                //If download que is empty then set complete to true (for now)
                downloadsComplete = downloadQue.isEmpty();

                for (int i = 0; i < workers.length; i++) {
                    ThreadFileDownloader worker = workers[i];

                    if (worker != null && worker.downloadFailed()) {
                        failedFiles.put(worker.sourceURL, worker.outputFile);
                    }

                    //If worker is still running then set complete back to false
                    if (worker != null && worker.isRunning()) {
                        downloadsComplete = false;
                    }
                    else if (downloadQue.size() > 0) {
                        filesDownloaded++;
                        Pair<String, File> file = downloadQue.poll();
                        worker = new ThreadFileDownloader(name + ":worker-" + i, file.key(), file.value());
                        LogHelperBC.dev("FileDownloadHandler: Starting Download: " + file.key() + " -> " + file.value());
                        workers[i] = worker;
                        worker.start();
                    }
                }
            }

            try {
                Thread.sleep(1);
            }
            catch (InterruptedException ignored) {}
        }

        running = false;
        if (queCompeteCallback != null) {
            queCompeteCallback.run();
        }
        if (resetOnFinish) {
            reset();
        }
    }

    //region File Que

    /**
     * Adds a file to the download que.
     *
     * @return true is the download thread is active, false if it needs to be restarted via {@link #startDownload()}
     */
    public boolean addFileToQue(String url, File file, boolean startIfStopped) {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        downloadQue.add(new Pair<>(url, file));
        if (startIfStopped) {
            startDownload();
        }
        totalFiles++;
        return running;
    }

    /**
     * Adds a file to the download que and starts the downloader if it is inactive.
     */
    public void addFileToQue(String url, File file) {
        addFileToQue(url, file, true);
    }

    public void stopDownloads() {
        stopDownload = true;
    }

    /**
     * Can only be called when the downloader is not running, Resets the downloader stats.
     */
    public void reset() {
        if (running) {
            return;
        }
        totalFiles = 0;
        filesDownloaded = 0;
        failedFiles.clear();
        downloadQue.clear();
    }

    //endregion

    /**
     * @return a map of all currently downloading files and their progress.
     */
    public Map<File, Double> getActiveProgress() {
        Map<File, Double> map = new HashMap<>();

        synchronized (workers) {
            for (ThreadFileDownloader thread : workers) {
                if (thread != null && thread.isRunning()) {
                    map.put(thread.outputFile, thread.getProgress());
                }
            }
        }

        return map;
    }

    public double getDownloadProgressTotal() {
        double total = filesDownloaded;
        for (Double d : getActiveProgress().values()) {
            total += d;
        }
        return total / totalFiles;
    }

    public void setQueCompeteCallback(Runnable queCompeteCallback) {
        this.queCompeteCallback = queCompeteCallback;
    }
}
