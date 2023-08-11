package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public class TransparencyGainRequest extends Request {

    private byte gain;

    public TransparencyGainRequest(byte gain) {
        super(COMMAND_TRANSPARENCY_GAIN);
        this.gain = gain;
    }

    @Override
    public byte[] getPayload() {
        return new byte[] { gain };
    }

}
