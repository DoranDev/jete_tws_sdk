package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class InEarDetectRequest extends Request {

    private final boolean enable;

    public InEarDetectRequest(boolean enable) {
        super(COMMAND_IN_EAR_DETECT);
        this.enable = enable;
    }

    @Override
    public byte[] getPayload() {
        byte payload = (byte) (enable ? 0x01 : 0x00);
        return new byte[] { payload };
    }
}
