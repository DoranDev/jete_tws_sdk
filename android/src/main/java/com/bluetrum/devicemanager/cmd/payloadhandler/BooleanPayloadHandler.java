package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class BooleanPayloadHandler extends PayloadHandler<Boolean> {

    public BooleanPayloadHandler(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public Boolean call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 1) {
            byte value = payload[0];
            if (value == 0x00) {
                return false;
            } else if (value == 0x01) {
                return true;
            }
        }
        return null;
    }

}
