package com.bluetrum.abmate.eq;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.lifecycle.LiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Database(entities = {EqSetting.class}, version = 1,exportSchema = false)
abstract class EqDataBase extends RoomDatabase {

    abstract EqSettingDao eqSettingDao();

    private static volatile EqDataBase INSTANCE;

    static EqDataBase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (EqDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            EqDataBase.class, "eq_database.db")
                            .allowMainThreadQueries() // 数据量较少，放主线程不影响
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final ExecutorService databaseWriteExecutor
            = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    void insertEqSetting(@NonNull final EqSetting eqSetting) {
        databaseWriteExecutor.execute(() -> eqSettingDao().insertEqSetting(eqSetting));
    }

    void updateEqSetting(@NonNull final EqSetting eqSetting) {
        databaseWriteExecutor.execute(() -> eqSettingDao().updateEqSetting(eqSetting));
    }

    void updateEqSetting(@NonNull final String name, @NonNull final byte[] gains) {
        databaseWriteExecutor.execute(() -> eqSettingDao().updateEqSetting(name ,gains));
    }

    void deleteEqSetting(@NonNull final EqSetting eqSetting) {
        databaseWriteExecutor.execute(() -> eqSettingDao().deleteEqSetting(eqSetting));
    }

    void deleteEqSetting(@NonNull final String name) {
        databaseWriteExecutor.execute(() -> eqSettingDao().deleteEqSetting(name));
    }

    // 同步，数据库不大，影响不大
    List<EqSetting> getEqSettings() {
        return eqSettingDao().getEqSettings();
    }

    LiveData<List<EqSetting>> getEqSettingsLiveData() {
        return eqSettingDao().getEqSettingsLiveData();
    }

}
