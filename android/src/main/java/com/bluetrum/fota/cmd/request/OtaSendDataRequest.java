package com.bluetrum.fota.cmd.request;

import androidx.annotation.NonNull;

public final class OtaSendDataRequest extends OtaRequest {

    private final byte[] payload;

    public OtaSendDataRequest(@NonNull final byte[] data) {
        super(COMMAND_OTA_SEND_DATA);
        payload = data;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

}
