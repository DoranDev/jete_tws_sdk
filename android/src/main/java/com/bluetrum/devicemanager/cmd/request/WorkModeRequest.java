package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class WorkModeRequest extends Request {

    public static final byte WORK_MODE_NORMAL       = 0x00;
    public static final byte WORK_MODE_GAMING       = 0x01;

    private final byte mode;

    public WorkModeRequest(byte mode) {
        super(COMMAND_WORK_MODE);
        this.mode = mode;
    }

    @Override
    public byte[] getPayload() {
        return new byte[] { mode };
    }
}
