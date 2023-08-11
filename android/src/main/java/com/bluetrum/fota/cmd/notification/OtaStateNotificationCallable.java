package com.bluetrum.fota.cmd.notification;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler;

public class OtaStateNotificationCallable extends PayloadHandler<Byte> {

    public OtaStateNotificationCallable(@NonNull byte[] payload) {
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
