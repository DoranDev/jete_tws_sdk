package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class LanguageRequest extends Request {

    public static final byte LANGUAGE_ENGLISH       = 0x00;
    public static final byte LANGUAGE_CHINESE       = 0x01;

    private final byte language;

    public LanguageRequest(byte language) {
        super(COMMAND_LANGUAGE);
        this.language = language;
    }

    @Override
    public byte[] getPayload() {
        return new byte[] { language };
    }
}
