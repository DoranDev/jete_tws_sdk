package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

public class FirmwareChecksumPayloadHandler extends PayloadHandler<byte[]> {

    public FirmwareChecksumPayloadHandler(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public byte[] call() throws Exception {
        byte[] payload = getPayload();
        if (payload.length == 4) {
            byte[] checksum = new byte[4];
            checksum[0] = payload[3];
            checksum[1] = payload[2];
            checksum[2] = payload[1];
            checksum[3] = payload[0];
            return checksum;
        }
        return new byte[0];
    }

}
