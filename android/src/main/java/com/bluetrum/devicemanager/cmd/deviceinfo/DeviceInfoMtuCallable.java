package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public class DeviceInfoMtuCallable extends DeviceInfoCallable<Integer> {

    public DeviceInfoMtuCallable(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public Integer call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 1) {
            return payload[0] & 0xFF;
        }
        return null;
    }

}
