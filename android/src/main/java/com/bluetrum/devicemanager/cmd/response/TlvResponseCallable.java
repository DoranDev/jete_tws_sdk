package com.bluetrum.devicemanager.cmd.response;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public final class TlvResponseCallable extends ResponseCallable<Map<Byte, Boolean>> {

    public TlvResponseCallable(byte[] payload) {
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
