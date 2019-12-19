package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BCConfigOld;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.function.BiConsumer;

/**
 * Created by brandon3055 on 11/06/2017.
 * This is a general purpose file downloader that allows you to provide a callback that will run once the download is complete.
 */
public class ThreadFileDownloader extends Thread implements IProcess {

    public final String sourceURL;
    public final File outputFile;
    private volatile double progress = 0;
    private volatile boolean finished = false;
    private volatile boolean failed = false;
    private volatile boolean running = false;
    private boolean processComplete = false;
    private Exception exception = null;
    private BiConsumer<ThreadFileDownloader, File> downloadCompleteHandler = null;

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

    public ThreadFileDownloader(String threadName, String sourceURL, File outputFile) {
        this(threadName, sourceURL, outputFile, null);
    }

    @Override
    public synchronized void start() {
        if (downloadCompleteHandler != null) {
            BrandonsCore.proxy.addProcess(this);
        }
        running = true;
        super.start();
    }

    @Override
    public void run() {
        try {
            URL url = new URL(sourceURL);
            if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
                throw new IOException("Could not create parent folder, Reason unknown");
            }
            if (!outputFile.exists() && !outputFile.createNewFile()) {
                throw new IOException("Could not create file, Reason unknown");
            }

            InputStream is = FileHandler.openURLStream(url);//url.openStream();
            OutputStream os = new FileOutputStream(outputFile);

            int size = is.available();
            final byte[] buffer = new byte[8192];
            int n;
            long count=0;
            while (-1 != (n = is.read(buffer))) {
                os.write(buffer, 0, n);
                count += n;
                progress = (double) count / (double) size;
            }

            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);

            finished = true;
        }
        catch (Exception e) {
            LogHelperBC.dev("ThreadFileDownloader: DL Failed " + e.getMessage());
            if (BCConfigOld.devLog) {
                e.printStackTrace();
            }
            exception = e;
            failed = true;
        }
        running = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean downloadFailed() {
        return failed;
    }

    public Exception getException() {
        return exception;
    }

    /**
     * @return the download progress as a value between 0 and 1
     */
    public double getProgress() {
        return progress;
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