package com.bluetrum.fota.cmd.request;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class OtaInfoRequest extends OtaRequest {

    private final byte[] payload;

    public OtaInfoRequest(final int version,
                          @NonNull final byte[] hashData,
                          final int dataSize) {
        super(COMMAND_OTA_GET_INFO);
        ByteBuffer bb = ByteBuffer.allocate(4 + 4 + 4)
                .order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(version);
        bb.put(hashData);
        bb.putInt(dataSize);
        payload = bb.array();
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

}
