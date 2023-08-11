package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler;

/**
 * @deprecated
 * Use class inheriting from {@link PayloadHandler}.
 */
@Deprecated
public abstract class DeviceInfoCallable<T> extends PayloadHandler<T> {

    public DeviceInfoCallable(@NonNull final byte[] payload) {
        super(payload);
    }

}
