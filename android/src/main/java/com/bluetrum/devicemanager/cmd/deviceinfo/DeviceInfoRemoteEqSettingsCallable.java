package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.models.RemoteEqSetting;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public class DeviceInfoRemoteEqSettingsCallable extends DeviceInfoCallable<List<RemoteEqSetting>> {

    public DeviceInfoRemoteEqSettingsCallable(@NonNull final byte[] payload) {
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
