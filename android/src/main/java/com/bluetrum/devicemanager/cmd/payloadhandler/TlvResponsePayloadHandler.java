package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class TlvResponsePayloadHandler extends PayloadHandler<Map<Byte, Boolean>> {

    public TlvResponsePayloadHandler(@NonNull byte[] payload) {
        super(payload);
    }

    @Override
    public Map<Byte, Boolean> call() throws Exception {
        final HashMap<Byte, Boolean> map = new HashMap<>();
        ByteBuffer bb = ByteBuffer.wrap(getPayload());
        while (bb.remaining() >= 3) {
            byte t = bb.get();
            byte l = bb.get();
            if (l != 1) return null; // 格式不对
            byte v = bb.get();
            map.put(t, (v == 0x00));
        }
        if (map.size() > 0)
            return map;
        return null;
    }

}
