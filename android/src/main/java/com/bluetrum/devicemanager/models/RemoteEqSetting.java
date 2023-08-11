package com.bluetrum.devicemanager.models;

public class RemoteEqSetting {

    public static final byte DEFAULT_SEGMENT_NUMBER = 10;

    public static final byte CUSTOM_START_INDEX = 0x20;

    private byte mode;
    private byte[] gains;

    public RemoteEqSetting(byte mode, byte[] gains) {
        this.mode = mode;
        this.gains = gains;
    }

    public byte getMode() {
        return mode;
    }

    public byte[] getGains() {
        return gains;
    }

    public boolean isPreset() {
        return mode < CUSTOM_START_INDEX;
    }

    public boolean isCustom() {
        return mode >= CUSTOM_START_INDEX;
    }

    public byte getCustomIndex() {
        return (byte) (mode - CUSTOM_START_INDEX);
    }

}
