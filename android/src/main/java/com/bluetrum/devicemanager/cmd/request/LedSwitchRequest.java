package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class LedSwitchRequest extends Request {

    private final boolean ledOn;

    public LedSwitchRequest(boolean on) {
        super(COMMAND_LED_MODE);
        this.ledOn = on;
    }

    @Override
    public byte[] getPayload() {
        byte[] payload = new byte[1];
        payload[0] = (byte) (ledOn ? 0x01 : 0x00);
        return payload;
    }

}
