package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class KeyRequest extends Request {

    public static final byte KEY_LEFT_SINGLE_TAP        = 0x01;
    public static final byte KEY_RIGHT_SINGLE_TAP       = 0x02;
    public static final byte KEY_LEFT_DOUBLE_TAP        = 0x03;
    public static final byte KEY_RIGHT_DOUBLE_TAP       = 0x04;
    public static final byte KEY_LEFT_TRIPLE_TAP        = 0x05;
    public static final byte KEY_RIGHT_TRIPLE_TAP       = 0x06;
    public static final byte KEY_LEFT_LONG_PRESS        = 0x07;
    public static final byte KEY_RIGHT_LONG_PRESS       = 0x08;

    public static final byte KEY_TOUCH_SENSITIVE_NORMAL = 0x00;
    public static final byte KEY_TOUCH_SENSITIVE_LOW    = 0x01;
    public static final byte KEY_TOUCH_SENSITIVE_HIGH   = 0x02;

    public static final byte KEY_FUNCTION_NONE          = 0x00;
    public static final byte KEY_FUNCTION_RECALL        = 0x01;
    public static final byte KEY_FUNCTION_ASSISTANT     = 0x02;
    public static final byte KEY_FUNCTION_PREVIOUS      = 0x03;
    public static final byte KEY_FUNCTION_NEXT          = 0x04;
    public static final byte KEY_FUNCTION_VOLUME_UP     = 0x05;
    public static final byte KEY_FUNCTION_VOLUME_DOWN   = 0x06;
    public static final byte KEY_FUNCTION_PLAY_PAUSE    = 0x07;
    public static final byte KEY_FUNCTION_GAME_MODE     = 0x08;
    public static final byte KEY_FUNCTION_ANC_MODE      = 0x09;

    private final byte keyType;
    private final byte keyFunction;

    public KeyRequest(byte keyType, byte keyFunction) {
        super(COMMAND_KEY);
        this.keyType = keyType;
        this.keyFunction = keyFunction;
    }

    public byte getKeyType() {
        return keyType;
    }

    public byte getKeyFunction() {
        return keyFunction;
    }

    @Override
    public byte[] getPayload() {
        return new byte[] { keyType, 1, keyFunction }; // TLV
    }

    public static KeyRequest LeftSingleTapKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_LEFT_SINGLE_TAP, keyFunction);
    }

    public static KeyRequest RightSingleTapKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_RIGHT_SINGLE_TAP, keyFunction);
    }

    public static KeyRequest LeftDoubleTapKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_LEFT_DOUBLE_TAP, keyFunction);
    }

    public static KeyRequest RightDoubleTapKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_RIGHT_DOUBLE_TAP, keyFunction);
    }

    public static KeyRequest LeftTripleTapKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_LEFT_TRIPLE_TAP, keyFunction);
    }

    public static KeyRequest RightTripleTapKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_RIGHT_TRIPLE_TAP, keyFunction);
    }

    public static KeyRequest LeftLongPressKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_LEFT_LONG_PRESS, keyFunction);
    }

    public static KeyRequest rightLongPressKeyRequest(byte keyFunction) {
        return new KeyRequest(KEY_RIGHT_LONG_PRESS, keyFunction);
    }

}
