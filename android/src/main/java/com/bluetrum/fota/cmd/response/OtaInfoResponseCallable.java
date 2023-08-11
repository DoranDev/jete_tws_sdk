package com.bluetrum.fota.cmd.response;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler;
import com.bluetrum.fota.OtaInfo;

public final class OtaInfoResponseCallable extends PayloadHandler<OtaInfo> {

    public OtaInfoResponseCallable(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public OtaInfo call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 9) {
            return new OtaInfo(payload);
        }
        return null;
    }

}
