package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class AncModeRequest extends Request {

    public static final byte ANC_MODE_OFF               = 0x00;
    public static final byte ANC_MODE_ON                = 0x01;
    public static final byte ANC_MODE_TRANSPARENCY      = 0x02;

    private final byte mode;

    public AncModeRequest(byte mode) {
        super(COMMAND_ANC_MODE);
        this.mode = mode;
    }

    @Override
    public byte[] getPayload() {
        return new byte[] { mode };
    }
}
