package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.models.RemoteEqSetting;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RemoteEqSettingsPayloadHandler extends PayloadHandler<List<RemoteEqSetting>> {

    public RemoteEqSettingsPayloadHandler(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public List<RemoteEqSetting> call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length > 1) {
            ArrayList<RemoteEqSetting> remoteEqSettings = new ArrayList<>();

            ByteBuffer bb = ByteBuffer.wrap(payload);
            byte gainNum = bb.get();

            while (bb.remaining() >= 1 + gainNum) {
                byte eqMode = bb.get();
                byte[] gains = new byte[gainNum];
                bb.get(gains);

                remoteEqSettings.add(new RemoteEqSetting(eqMode, gains));
            }

            return remoteEqSettings;
        }
        return null;
    }

}
