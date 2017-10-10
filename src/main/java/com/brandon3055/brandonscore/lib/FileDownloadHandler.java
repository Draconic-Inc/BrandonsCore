package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.LogHelperBC;

import java.io.File;
import java.util.*;

/**
 * Created by brandon3055 on 5/08/2017.
 */
public class FileDownloadHandler implements Runnable {

    public volatile int totalFiles = 0;
    public volatile int filesDownloaded = 0;
    public volatile boolean running = false;
    public volatile boolean downloadsComplete = false;
    public Map<String, File> failedFiles = Collections.synchronizedMap(new HashMap<>());

    private String name;
    private final int maxWorkers;

    private final ThreadFileDownloader[] workers;

    private List<PairKV<String, File>> downloadQue = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean stopDownload = false;

    public FileDownloadHandler(String name, int maxWorkers) {
        this.name = name;
        this.maxWorkers = maxWorkers;
        this.workers = new ThreadFileDownloader[maxWorkers];
    }

    public void startDownload() {
        if (running) return;
        running = true;
        downloadsComplete = false;
        new Thread(this, name).start();
    }

    @Override
    public void run() {
        while (!downloadsComplete) {
            synchronized (workers) {
                if (stopDownload) {
                    for (int i = 0; i < workers.length; i++) {
                        if (!workers[i].isRunning()) {
                            workers[i].interrupt();
                            downloadQue.add(new PairKV<>(workers[i].sourceURL, workers[i].outputFile));
                            workers[i] = null;
                        }
                    }
                    stopDownload = false;
                    break;
                }

                downloadsComplete = downloadQue.isEmpty();

                for (int i = 0; i < workers.length; i++) {
                    ThreadFileDownloader worker = workers[i];

                    if (worker != null && worker.downloadFailed()) {
                        failedFiles.put(worker.sourceURL, worker.outputFile);
                    }

                    if (worker != null && worker.isRunning()) {
                        downloadsComplete = false;
                    }
                    else if (downloadQue.size() > 0) {
                        PairKV<String, File> file = downloadQue.remove(0);
                        worker = new ThreadFileDownloader(name + ":DLThread-" + i, file.getKey(), file.getValue());
                        LogHelperBC.dev("FileDownloadHandler: Starting Download: " + file.getKey() + " -> " + file.getValue());
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
        downloadQue.add(new PairKV<>(url, file));
        if (startIfStopped) {
            startDownload();
        }
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

    /**
     * @return a map of all currently downloading files and their progress.
     */
    public Map<String, Double> getActiveProgress() {
        Map<String, Double> map = new HashMap<>();

        synchronized (workers) {
            for (ThreadFileDownloader thread : workers) {
                if (thread != null && thread.isRunning()) {
                    map.put(thread.sourceURL, thread.getProgress());
                }
            }
        }

        return map;
    }

    //endregion

}
