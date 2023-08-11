package com.bluetrum.fota;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class OtaInfo {

    private final int startAddress;
    private final int blockSize;
    private final boolean allowUpdate;

    public OtaInfo(@NonNull final byte[] otaInfoData) throws IllegalArgumentException {
        if (otaInfoData.length != 9)
            throw new IllegalArgumentException("Data length must be 9, now is " + otaInfoData.length);

        ByteBuffer bb = ByteBuffer.wrap(otaInfoData)
                .order(ByteOrder.LITTLE_ENDIAN);
        startAddress = bb.getInt();
        blockSize = bb.getInt();
        byte b = bb.get();
        allowUpdate = (b == 0x01);
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public boolean isAllowUpdate() {
        return allowUpdate;
    }

}
