package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.IProcess;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.function.BiConsumer;

/**
 * Created by brandon3055 on 11/06/2017.
 * This is a general purpose file downloader that allows you to provide a callback that will run once the download is complete.
 */
public class ThreadFileDownloader extends Thread implements IProcess {

    private final String sourceURL;
    private final File outputFile;
    private volatile boolean finished = false;
    private volatile boolean failed = false;
    private boolean processComplete = false;
    private Exception exception = null;
    private BiConsumer<ThreadFileDownloader, File> downloadCompleteHandler;

    /**
     * The completionCallBack as a consumer that you supply to run your code once the download is complete.
     * Because this uses a separate thread you can not just start the download and then immediately get the result.
     * You have monitor the download thread using a tick handler or some other method to wait for it to finish then run your code.
     * This handler does all that for you.
     * Once the download finishes the handler will be called on the main minecraft thread so you dont need to worry about synchronization.
     * The handler is given the thread and the downloaded file. The file will be null if the download failed.
     * In the event the download failed you can get the error from the thread.
     *
     * @param threadName         A name for this thread.
     * @param sourceURL          The source URL to download from (https not supported).
     * @param outputFile         The local file to save this download as.
     * @param completionCallBack Supply your own callback to run your code once the download is complete (This is thread safe se details above)
     */
    public ThreadFileDownloader(String threadName, String sourceURL, File outputFile, BiConsumer<ThreadFileDownloader, File> completionCallBack) {
        super(threadName);
        this.sourceURL = sourceURL;
        this.outputFile = outputFile;
        this.downloadCompleteHandler = completionCallBack;
    }

    @Override
    public synchronized void start() {
        BrandonsCore.proxy.addProcess(this);
        super.start();
    }

    @Override
    public void run() {
        try {
            URL url = new URL(sourceURL);
            if (!outputFile.exists() && !outputFile.createNewFile()) {
                throw new IOException("Could not create file, Reason unknown");
            }

            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(outputFile);

            IOUtils.copy(is, os);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);

            finished = true;
        }
        catch (Exception e) {
            exception = e;
            failed = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean downloadFailed() {
        return failed;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void updateProcess() {
        if (isFinished()) {
            downloadCompleteHandler.accept(this, outputFile);
            processComplete = true;
        }
        else if (downloadFailed()) {
            downloadCompleteHandler.accept(this, null);
            processComplete = true;
        }
    }

    @Override
    public boolean isDead() {
        return processComplete;
    }
}