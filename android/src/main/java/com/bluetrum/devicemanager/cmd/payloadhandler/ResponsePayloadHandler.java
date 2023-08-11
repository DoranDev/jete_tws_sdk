package com.bluetrum.devicemanager.cmd.payloadhandler;

public class ResponsePayloadHandler extends PayloadHandler<Boolean> {

    public ResponsePayloadHandler(byte[] payload) {
        super(payload);
    }

    @Override
    public Boolean call() throws Exception {
        final byte[] payload = getPayload();
        if (payload.length == 1) {
            return (payload[0] == 0x00);
        }
        return null;
    }

}
