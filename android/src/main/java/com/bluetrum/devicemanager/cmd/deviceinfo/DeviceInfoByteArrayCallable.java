package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public final class DeviceInfoByteArrayCallable extends DeviceInfoCallable<byte[]> {

    public DeviceInfoByteArrayCallable(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public byte[] call() throws Exception {
        return getPayload();
    }

}
