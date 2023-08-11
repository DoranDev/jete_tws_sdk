package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.models.RemoteEqSetting;

import java.nio.ByteBuffer;

public class RemoteEqSettingPayloadHandler extends PayloadHandler<RemoteEqSetting> {

    public RemoteEqSettingPayloadHandler(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public RemoteEqSetting call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length > 2) {
            ByteBuffer bb = ByteBuffer.wrap(payload);
            byte bandNum = bb.get();

            if (bb.remaining() == 1 + bandNum) {
                byte mode = bb.get();
                byte[] gains = new byte[bandNum];
                bb.get(gains);

                return new RemoteEqSetting(mode, gains);
            }
        }
        return null;
    }

}
