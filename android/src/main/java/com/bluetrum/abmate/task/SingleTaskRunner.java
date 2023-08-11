package com.bluetrum.abmate.task;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SingleTaskRunner {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * executeAsync执行的回调
     * @param <T> 返回的类型
     */
    public interface Callback<T> {
        void onComplete(@Nullable T result);
    }

    /**
     * 异步执行任务Callable，带回调
     * @param callable 执行的任务
     * @param callback 执行完的回调，主线程
     * @param <T> 返回的类型
     */
    public <T> void executeAsync(@NonNull Callable<T> callable, @Nullable Callback<T> callback) {
        executor.execute(() -> {
            final T result;
            try {
                result = callable.call();
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    if (callback != null) {
                        callback.onComplete(null);
                    }
                });
                return;
            }
            handler.post(() -> {
                if (callback != null) {
                    callback.onComplete(result);
                }
            });
        });
    }

    /**
     * 异步执行Runnable，不带回调
     * @param runnable 执行的任务
     */
    public void executeAsync(@NonNull Runnable runnable) {
        executor.execute(runnable);
    }
}
