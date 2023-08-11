package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class UInt16ToIntegerPayloadHandler extends PayloadHandler<Integer> {

    public UInt16ToIntegerPayloadHandler(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public Integer call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 2) {
            // 小端
            return (payload[1] << 8) | payload[0];
        }
        return null;
    }

}
