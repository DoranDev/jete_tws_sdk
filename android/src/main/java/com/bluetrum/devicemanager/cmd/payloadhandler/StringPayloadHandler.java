package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class StringPayloadHandler extends PayloadHandler<String> {

    public StringPayloadHandler(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public String call() throws Exception {
        final byte[] payload = getPayload();
        return new String(payload);
    }

}
