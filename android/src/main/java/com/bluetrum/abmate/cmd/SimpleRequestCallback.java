package com.bluetrum.abmate.cmd;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.DeviceCommManager;
import com.bluetrum.devicemanager.cmd.Request;

public interface SimpleRequestCallback extends DeviceCommManager.RequestCallback {
    @Override
    default void onTimeout(@NonNull final Request request) {
        // empty
    }
}
