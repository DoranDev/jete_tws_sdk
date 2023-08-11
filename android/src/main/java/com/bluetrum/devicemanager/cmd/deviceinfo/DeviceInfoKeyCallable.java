package com.bluetrum.devicemanager.cmd.deviceinfo;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public final class DeviceInfoKeyCallable extends DeviceInfoCallable<Map<Integer, Integer>> {

    public DeviceInfoKeyCallable(@NonNull final byte[] payload) {
        super(payload);
    }

    @Override
    public Map<Integer, Integer> call() throws Exception {
        final byte[] payload = getPayload();
        Map<Integer, Integer> map = new HashMap<>();
        ByteBuffer bb = ByteBuffer.wrap(payload);
        while (bb.remaining() >= 3) {
            byte t = bb.get();
            byte l = bb.get();
            // FIXME: 长度有问题，继续处理下一个？
            if (l != 1) {
                bb.position(bb.position() + l);
                continue;
            }
            byte v = bb.get();
            map.put((int) t, (int) v);
        }
        return map;
    }

}
