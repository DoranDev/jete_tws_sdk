package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class ByteToIntegerPayloadHandler extends  PayloadHandler<Integer> {

    public ByteToIntegerPayloadHandler(@NonNull byte[] payload) {
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
