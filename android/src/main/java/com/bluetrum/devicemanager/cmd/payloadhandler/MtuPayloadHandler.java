package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class MtuPayloadHandler extends PayloadHandler<Integer> {

    public MtuPayloadHandler(@NonNull byte[] payload) {
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
