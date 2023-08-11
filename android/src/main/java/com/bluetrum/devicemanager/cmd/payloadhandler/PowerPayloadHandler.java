package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.models.DevicePower;

public final class PowerPayloadHandler extends PayloadHandler<DevicePower> {

    public PowerPayloadHandler(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public DevicePower call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length != 0) {
            return new DevicePower(payload);
        }
        return null;
    }

}
