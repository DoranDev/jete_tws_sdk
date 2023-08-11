package com.bluetrum.fota;

public final class OtaState {

    public static final byte OTA_STATE_OK               = 0;
    public static final byte OTA_STATE_SAME_FIRMWARE    = 1;
    public static final byte OTA_STATE_KEY_MISMATCH     = 2;
    public static final byte OTA_STATE_CRC_ERROR        = 11;
    public static final byte OTA_STATE_SEQ_ERROR        = 64;
    public static final byte OTA_STATE_DATA_LEN_ERROR   = 65;
    public static final byte OTA_STATE_PAUSE            = (byte) 253;
    public static final byte OTA_STATE_CONTINUE         = (byte) 254;
    public static final byte OTA_STATE_FINISH           = (byte) 255;

}
