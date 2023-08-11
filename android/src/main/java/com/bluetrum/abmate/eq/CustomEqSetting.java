package com.bluetrum.abmate.eq;

import androidx.annotation.NonNull;

public class CustomEqSetting extends EqSetting {

    public static final byte CUSTOM_START_INDEX = 0x20;

    public CustomEqSetting(@NonNull String name, byte[] gains) {
        super(name, gains);
    }

}
