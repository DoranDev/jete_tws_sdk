package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class IntegerPayloadHandler extends PayloadHandler<Integer> {

    public IntegerPayloadHandler(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public Integer call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 4) {
            // 小端
            return (payload[3] << 24) | (payload[2] << 16) | (payload[1] << 8) | payload[0];
        }
        return null;
    }

}
