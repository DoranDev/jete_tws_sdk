package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public final class DeviceInfoByteCallable extends DeviceInfoCallable<Byte> {

    public DeviceInfoByteCallable(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public Byte call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 1) {
            return payload[0];
        }
        return null;
    }

}
