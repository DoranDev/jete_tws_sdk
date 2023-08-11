package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class AutoAnswerRequest extends Request {

    private final boolean enable;

    public AutoAnswerRequest(boolean enable) {
        super(COMMAND_AUTO_ANSWER);
        this.enable = enable;
    }

    @Override
    public byte[] getPayload() {
        byte payload = (byte) (enable ? 0x01 : 0x00);
        return new byte[] { payload };
    }
}
