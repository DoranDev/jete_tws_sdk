package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class BytePayloadHandler extends PayloadHandler<Byte> {

    public BytePayloadHandler(@NonNull byte[] payload) {
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
