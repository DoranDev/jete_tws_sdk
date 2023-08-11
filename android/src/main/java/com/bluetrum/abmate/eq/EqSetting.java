package com.bluetrum.abmate.eq;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@Entity(tableName = "eq_settings",
        indices = @Index("name"),
        ignoredColumns = {"nameResId", "isCustom"})
public class EqSetting {

    public static final byte EQUALIZER_MAX_GAIN = 12; // 均衡器±最大增益

    @PrimaryKey
    @NonNull
    private final String name; // custom use

    @ColumnInfo(name = "gains")
    @NonNull
    private final byte[] gains;

    protected boolean isCustom;

    protected int nameResId;

    public EqSetting(@NonNull String name, @NonNull byte[] gains) {
        this.name = name;
        this.gains = gains;
        this.isCustom = true;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public byte[] getGains() {
        return gains;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public int getNameResId() {
        return nameResId;
    }

}
