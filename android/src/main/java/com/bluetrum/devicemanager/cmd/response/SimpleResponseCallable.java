package com.bluetrum.devicemanager.cmd.response;

/**
 * @deprecated
 * Use class inheriting from {@link com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler}.
 */
@Deprecated
public final class SimpleResponseCallable extends ResponseCallable<Boolean> {

    public SimpleResponseCallable(byte[] payload) {
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
