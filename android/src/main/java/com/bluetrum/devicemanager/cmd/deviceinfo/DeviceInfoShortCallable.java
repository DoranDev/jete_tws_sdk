package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public class DeviceInfoShortCallable extends DeviceInfoCallable<Short> {

    public DeviceInfoShortCallable(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public Short call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 2) {
            return (short) ((payload[1] << 8) | payload[0]);
        }
        return null;
    }

}
