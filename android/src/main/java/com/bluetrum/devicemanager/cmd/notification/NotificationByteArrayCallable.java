package com.bluetrum.devicemanager.cmd.notification;

import androidx.annotation.NonNull;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public class NotificationByteArrayCallable extends NotificationCallable<byte[]> {

    public NotificationByteArrayCallable(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public byte[] call() throws Exception {
        return getPayload();
    }

}
