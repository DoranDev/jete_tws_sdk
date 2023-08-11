package com.bluetrum.devicemanager.db;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Entity(tableName = "block_record",
        indices = @Index("address"))
public class BlockRecord {

    @NonNull
    @PrimaryKey
    private String address;

    @ColumnInfo(name = "block_time")
    private long blockTime;

    public BlockRecord(@NonNull String address,
                       long blockTime
    ) {
        this.address = address;
        this.blockTime = blockTime;
    }

    @NonNull
    public String getAddress() {
        return address;
    }

    public long getBlockTime() {
        return blockTime;
    }

}
