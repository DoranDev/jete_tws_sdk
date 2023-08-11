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

import com.bluetrum.devicemanager.db.BlockRecord;
import com.bluetrum.devicemanager.db.BlockRecordDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Database(entities = {BlockRecord.class}, version = 1,exportSchema = false)
abstract class ABBlockDb extends RoomDatabase {

    abstract BlockRecordDao blockRecordDao();

    private static volatile ABBlockDb INSTANCE;

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static ABBlockDb getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (ABBlockDb.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ABBlockDb.class, "ab_block_database.db")
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

    void insertBlockRecord(@NonNull final String address, long blockTime) {
        databaseWriteExecutor.execute(() -> blockRecordDao().insertBlockRecord(new BlockRecord(address, blockTime)));
    }

    void updateBlockRecord(@NonNull final BlockRecord blockRecord) {
        databaseWriteExecutor.execute(() -> blockRecordDao().updateBlockRecord(blockRecord));
    }

    void updateBlockRecord(@NonNull final String address, long blockTime) {
        databaseWriteExecutor.execute(() -> blockRecordDao().updateBlockTime(address, blockTime));
    }

    void deleteBlockRecord(@NonNull final BlockRecord blockRecord) {
        databaseWriteExecutor.execute(() -> blockRecordDao().deleteBlockRecord(blockRecord));
    }

    void deleteBlockRecord(@NonNull final String address) {
        databaseWriteExecutor.execute(() -> blockRecordDao().deleteBlockRecord(address));
    }

    // 同步，数据库不大，影响不大
    BlockRecord getBlockRecord(@NonNull final String address) {
        return blockRecordDao().getBlockRecord(address);
    }

    // 异步
    void getBlockRecord(@NonNull final String address, @NonNull final Callback<BlockRecord> callback) {
        databaseWriteExecutor.execute(() -> {
            BlockRecord blockRecord = blockRecordDao().getBlockRecord(address);
            callbackHandler.post(() -> callback.onComplete(blockRecord));
        });
    }

}
