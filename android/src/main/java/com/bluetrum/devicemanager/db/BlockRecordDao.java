package com.bluetrum.devicemanager.db;

import androidx.annotation.RestrictTo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Dao
public interface BlockRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBlockRecord(BlockRecord record);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateBlockRecord(BlockRecord record);

    @Delete
    void deleteBlockRecord(BlockRecord record);

    @Query("DELETE FROM block_record WHERE address = :address")
    void deleteBlockRecord(String address);

    @Query("DELETE FROM block_record")
    void deleteAll();

    @Query("SELECT * FROM block_record WHERE address = :address")
    BlockRecord getBlockRecord(String address);

    @Query("UPDATE block_record SET block_time = :blockTime WHERE address = :address")
    int updateBlockTime(String address, long blockTime);

}
