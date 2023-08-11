package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public final class DeviceInfoBooleanCallable extends DeviceInfoCallable<Boolean> {

    public DeviceInfoBooleanCallable(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public Boolean call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 1) {
            byte value = payload[0];
            if (value == 0x00) {
                return false;
            } else if (value == 0x01) {
                return true;
            }
        }
        return null;
    }

}
