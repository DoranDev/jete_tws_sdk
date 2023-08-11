package com.bluetrum.devicemanager.cmd.request;

import android.util.SparseArray;

import com.bluetrum.devicemanager.cmd.Request;
import com.bluetrum.devicemanager.models.RemoteEqSetting;

import java.nio.ByteBuffer;

public final class EqRequest extends Request {

    private final byte segmentNum;
    private final byte eqMode;
    private final SparseArray<Byte> gains;

    public EqRequest(byte segmentNum, byte eqMode, byte... eqGains) {
        super(COMMAND_EQ);

        this.segmentNum = segmentNum;
        this.eqMode = eqMode;

        this.gains = new SparseArray<>(segmentNum);
        if (segmentNum <= eqGains.length) {
            for (int i = 0; i < segmentNum; i++) {
                this.gains.put(i, eqGains[i]);
            }
        } else {
            int i = 0;
            for (; i < eqGains.length; i++) {
                this.gains.put(i, eqGains[i]);
            }
            for (; i < segmentNum; i++) {
                this.gains.put(i, (byte) 0);
            }
        }
    }

    public EqRequest(byte eqMode, byte... eqGains) {
        this(RemoteEqSetting.DEFAULT_SEGMENT_NUMBER, eqMode, eqGains);
    }

    @Override
    public byte[] getPayload() {
        int payloadLength = 2 + segmentNum;
        ByteBuffer bb = ByteBuffer.allocate(payloadLength);
        bb.put(segmentNum);
        bb.put(eqMode);
        for (int i = 0; i < segmentNum; i++) {
            bb.put(gains.get(i, (byte) 0));
        }
        return bb.array();
    }

    public static EqRequest PresetEqRequest(byte eqMode, byte... eqGains) {
        return new EqRequest(RemoteEqSetting.DEFAULT_SEGMENT_NUMBER, eqMode, eqGains);
    }

    public static EqRequest CustomEqRequest(byte customEqMode, byte... eqGains) {
        return new EqRequest(RemoteEqSetting.DEFAULT_SEGMENT_NUMBER, (byte) (RemoteEqSetting.CUSTOM_START_INDEX + customEqMode), eqGains);
    }
}
