package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class ClearPairRecordRequest extends Request {

    public ClearPairRecordRequest() {
        super(COMMAND_CLEAR_PAIR_RECORD);
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }
}
