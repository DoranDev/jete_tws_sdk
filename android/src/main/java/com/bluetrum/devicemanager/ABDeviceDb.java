package com.bluetrum.devicemanager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.bluetrum.devicemanager.db.BaseDevice;
import com.bluetrum.devicemanager.db.BaseDeviceDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Database(entities = {BaseDevice.class}, version = 1,exportSchema = false)
abstract class ABDeviceDb extends RoomDatabase {

    abstract BaseDeviceDao baseDeviceDao();

    private static volatile ABDeviceDb INSTANCE;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static ABDeviceDb getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (ABDeviceDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ABDeviceDb.class, "ab_device_database.db")
                            .allowMainThreadQueries() // 数据量较少，放主线程不影响
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 读取数据库后执行的回调
     * @param <T> 返回的类型
     */
    public interface Callback<T> {
        void onComplete(@Nullable T result);
    }

    private static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Handler callbackHandler = new Handler(Looper.getMainLooper());

    void insertBaseDevice(@NonNull final BaseDevice baseDevice) {
        databaseWriteExecutor.execute(() -> baseDeviceDao().insertBaseDevice(baseDevice));
    }

    void updatePodDevice(@NonNull final BaseDevice baseDevice) {
        databaseWriteExecutor.execute(() -> baseDeviceDao().updateBaseDevice(baseDevice));
    }

    void deletePodDevice(@NonNull final BaseDevice baseDevice) {
        databaseWriteExecutor.execute(() -> baseDeviceDao().deleteBaseDevice(baseDevice));
    }

    void deletePodDevice(@NonNull final String address) {
        databaseWriteExecutor.execute(() -> baseDeviceDao().deleteBaseDevice(address));
    }

    void getPodDevice(@NonNull final String address, @NonNull final Callback<BaseDevice> callback) {
        databaseWriteExecutor.execute(() -> {
            BaseDevice baseDevice = baseDeviceDao().getBaseDevice(address);
            callbackHandler.post(() -> callback.onComplete(baseDevice));
        });
    }

}
