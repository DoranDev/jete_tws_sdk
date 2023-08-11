package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public class SoundEffect3dRequest extends Request {

    private final boolean on;

    public SoundEffect3dRequest(boolean on) {
        super(COMMAND_SOUND_EFFECT_3D);
        this.on = on;
    }

    @Override
    public byte[] getPayload() {
        byte[] payload = new byte[1];
        payload[0] = (byte) (on ? 0x01 : 0x00);
        return payload;
    }

}
