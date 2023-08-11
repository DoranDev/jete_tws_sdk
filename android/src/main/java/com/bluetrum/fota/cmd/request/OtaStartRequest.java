package com.bluetrum.fota.cmd.request;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class OtaStartRequest extends OtaRequest {

    private final byte[] payload;

    public OtaStartRequest(final int startAddress, final int dataLen, @NonNull final byte[] data) {
        super(COMMAND_OTA_START);
        ByteBuffer bb = ByteBuffer.allocate(4 + 4 + data.length)
                .order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(startAddress);
        bb.putInt(dataLen);
        bb.put(data);
        payload = bb.array();
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

}
