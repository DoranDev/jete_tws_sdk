package com.bluetrum.fota.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public abstract class OtaRequest extends Request {

    public static final byte COMMAND_OTA_GET_INFO       = (byte) 0xA0;
    public static final byte COMMAND_OTA_START          = (byte) 0xA1;
    public static final byte COMMAND_OTA_SEND_DATA      = (byte) 0xA2;
    public static final byte COMMAND_OTA_STATE          = (byte) 0xA3;

    public OtaRequest(byte command) {
        super(command);
    }

    @Override
    public boolean withResponse() {
        return false;
    }

}
