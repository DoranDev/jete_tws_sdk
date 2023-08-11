package com.bluetrum.devicemanager.db;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Entity(tableName = "base_device",
        indices = @Index("address"))
public class BaseDevice {

    @NonNull
    @PrimaryKey
    private String address;

//    private int type;

    @NonNull
    private String name;

    @NonNull
    private byte[] random;

    public BaseDevice(@NonNull String address,
//                     int type,
                      @NonNull String name,
                      @NonNull byte[] random
    ) {
        this.address = address;
//        this.type = type;
        this.name = name;
        this.random = random;
    }

    @NonNull
    public String getAddress() {
        return address;
    }

//    public int getType() {
//        return type;
//    }

    @NonNull
    public String getName() {
        return name;
    }

    public byte[] getRandom() {
        return random;
    }

}
