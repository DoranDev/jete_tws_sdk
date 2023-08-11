package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class MusicControlRequest extends Request {

    public static final byte CONTROL_TYPE_VOLUME       = 0x01;
    public static final byte CONTROL_TYPE_PLAY         = 0x02;
    public static final byte CONTROL_TYPE_PAUSE        = 0x03;
    public static final byte CONTROL_TYPE_PREVIOUS     = 0x04;
    public static final byte CONTROL_TYPE_NEXT         = 0x05;

    private final byte controlType;
    private byte volume;

    private MusicControlRequest(byte controlType) {
        super(COMMAND_MUSIC_CONTROL);
        this.controlType = controlType;
    }

    public byte getControlType() {
        return controlType;
    }

    public byte getVolume() {
        return volume;
    }

    @Override
    public byte[] getPayload() {
        if (controlType == CONTROL_TYPE_VOLUME) {
            return new byte[] { controlType, volume };
        } else {
            return new byte[] { controlType };
        }
    }

    public static MusicControlRequest MusicControlVolumeRequest(byte volume) {
        MusicControlRequest request = new MusicControlRequest(CONTROL_TYPE_VOLUME);
        request.volume = volume;
        return request;
    }

    public static MusicControlRequest MusicControlPlayRequest() {
        return new MusicControlRequest(CONTROL_TYPE_PLAY);
    }

    public static MusicControlRequest MusicControlPauseRequest() {
        return new MusicControlRequest(CONTROL_TYPE_PAUSE);
    }

    public static MusicControlRequest MusicControlPreviousRequest() {
        return new MusicControlRequest(CONTROL_TYPE_PREVIOUS);
    }

    public static MusicControlRequest MusicControlNextRequest() {
        return new MusicControlRequest(CONTROL_TYPE_NEXT);
    }

}
