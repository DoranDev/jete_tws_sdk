package com.bluetrum.devicemanager.cmd.notification;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public class NotificationByteCallable extends NotificationCallable<Byte> {

    public NotificationByteCallable(@NonNull byte[] payload) {
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
