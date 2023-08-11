package com.bluetrum.devicemanager.cmd.notification;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public class NotificationByteToIntegerCallable extends NotificationCallable<Integer> {

    public NotificationByteToIntegerCallable(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public Integer call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 1) {
            return (int) payload[0];
        }
        return null;
    }

}
