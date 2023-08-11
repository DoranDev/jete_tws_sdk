package com.bluetrum.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtils {

    private static volatile Thread sMainThread;
    private static volatile Handler sMainThreadHandler;
    private static volatile ExecutorService sThreadExecutor;

    /**
     * Returns true if the current thread is the UI thread.
     */
    public static boolean isMainThread() {
        if (sMainThread == null) {
            sMainThread = Looper.getMainLooper().getThread();
        }
        return Thread.currentThread() == sMainThread;
    }

    /**
     * Returns a shared UI thread handler.
     */
    public static Handler getUiThreadHandler() {
        if (sMainThreadHandler == null) {
            sMainThreadHandler = new Handler(Looper.getMainLooper());
        }

        return sMainThreadHandler;
    }

    /**
     * Checks that the current thread is the UI thread. Otherwise throws an exception.
     */
    public static void ensureMainThread() {
        if (!isMainThread()) {
            throw new RuntimeException("Must be called on the UI thread");
        }
    }

    /**
     * Posts runnable in background using shared background thread pool.
     *
     * @Return A future of the task that can be monitored for updates or cancelled.
     */
    public static Future<?> postOnBackgroundThread(@NonNull Runnable runnable) {
        return getThreadExecutor().submit(runnable);
    }

    /**
     * Posts callable in background using shared background thread pool.
     *
     * @Return A future of the task that can be monitored for updates or cancelled.
     */
    public static Future<?> postOnBackgroundThread(@NonNull Callable<?> callable) {
        return getThreadExecutor().submit(callable);
    }

    /**
     * Posts the runnable on the main thread.
     */
    public static void postOnMainThread(@NonNull Runnable runnable) {
        getUiThreadHandler().post(runnable);
    }

    /**
     * Post the supplied Runnable to run on the main thread after the given amount of time.
     * @param task The Runnable to run
     * @param delayMillis The delay in milliseconds until the Runnable will be run
     */
    public static void postOnMainThreadDelayed(Runnable task, long delayMillis) {
        getUiThreadHandler().postDelayed(task, delayMillis);
    }

    private static synchronized ExecutorService getThreadExecutor() {
        if (sThreadExecutor == null) {
            sThreadExecutor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());
        }
        return sThreadExecutor;
    }
}
