package com.bluetrum.abmate.eq;

import androidx.annotation.RestrictTo;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Dao
public interface EqSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEqSetting(EqSetting eqSetting);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateEqSetting(EqSetting eqSetting);

    @Delete
    void deleteEqSetting(EqSetting eqSetting);

    @Query("DELETE FROM eq_settings WHERE name = :name")
    void deleteEqSetting(String name);

    @Query("DELETE FROM eq_settings")
    void deleteAll();

    @Query("SELECT * FROM eq_settings WHERE name = :name")
    EqSetting getEqSetting(String name);

    @Query("UPDATE eq_settings SET gains = :gains WHERE name = :name")
    int updateEqSetting(String name, byte[] gains);

    @Query("SELECT * FROM eq_settings")
    List<EqSetting> getEqSettings();

    @Query("SELECT * FROM eq_settings")
    LiveData<List<EqSetting>> getEqSettingsLiveData();

}
