package com.bluetrum.devicemanager.db;

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
public interface BaseDeviceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBaseDevice(BaseDevice baseDevice);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateBaseDevice(BaseDevice baseDevice);

    @Delete
    void deleteBaseDevice(BaseDevice baseDevice);

    @Query("DELETE FROM base_device WHERE address = :address")
    void deleteBaseDevice(String address);

    @Query("DELETE FROM base_device")
    void deleteAll();

    @Query("SELECT * FROM base_device")
    LiveData<List<BaseDevice>> getBaseDevices();

    @Query("SELECT * FROM base_device WHERE address = :address")
    BaseDevice getBaseDevice(String address);

}
