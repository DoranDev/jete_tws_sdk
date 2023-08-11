package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class FactoryResetRequest extends Request {

    public FactoryResetRequest() {
        super(COMMAND_FACTORY_RESET);
    }

    @Override
    public byte[] getPayload() {
        return new byte[0];
    }
}
