package com.bluetrum.abmate.di;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.bluetrum.abmate.BuildConfig;
import com.bluetrum.abmate.utils.Utils;
import com.bluetrum.abmate.viewmodels.DeviceRepository;
import com.bluetrum.abmate.viewmodels.ScannerRepository;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

@HiltAndroidApp
public class ABApplication extends Application
        implements DefaultLifecycleObserver {

    @Inject
    ScannerRepository mScannerRepository;

    @Inject
    DeviceRepository mDeviceRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        // 配置logger
        setupTimber();

        // 监控App生命周期
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        // 移除常驻通知栏横幅
//        boolean enableLauncherNotification = Utils.getPrefsEnableLaunchNotification(this);
//        if (enableLauncherNotification) {
//            ABNotificationManager.removeLauncherNotification(this);
//        }
//        // 在任务管理器隐藏
//        Utils.setAppExcludeFromRecents(this, enableLauncherNotification);

        if (Utils.getPrefsEnableQuickConnect(this)) {
            Integer connectionState = mDeviceRepository.getDeviceConnectionState().getValue();
            if (connectionState != null && connectionState == DeviceRepository.DEVICE_CONNECTION_STATE_IDLE) {
                mDeviceRepository.startScan();
            }
        }
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        // 创建常驻通知栏横幅，用以快速启动App
        boolean enableLauncherNotification = Utils.getPrefsEnableLaunchNotification(this);
        Timber.d("Show launch notification: %s", enableLauncherNotification);
//        if (enableLauncherNotification) {
//            ABNotificationManager.showLauncherNotification(this, mDeviceRepository.getActiveDevice().getValue());
//        }

        mDeviceRepository.stopScan();
    }

    /**
     * 设置Logger, Timber
     */
    private void setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    /**
     * DebugTree for release version
     */
    private static class CrashReportingTree extends Timber.DebugTree {
        @Override
        protected boolean isLoggable(@Nullable String tag, int priority) {
            return priority >= Log.INFO;
        }

        @Override
        protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            if (!isLoggable(tag, priority)) {
                return;
            }
            super.log(priority, tag, message, t);
        }
    }

}
