package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public final class DeviceInfoIntegerCallable extends DeviceInfoCallable<Integer> {

    public DeviceInfoIntegerCallable(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public Integer call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 4) {
            // 小端
            return (payload[3] << 24) | (payload[2] << 16) | (payload[1] << 8) | payload[0];
        }
        return null;
    }

}
