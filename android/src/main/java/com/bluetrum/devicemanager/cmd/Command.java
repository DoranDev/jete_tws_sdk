package com.bluetrum.devicemanager.cmd;

public interface Command {

    byte COMMAND_TYPE_REQUEST       = 1;
    byte COMMAND_TYPE_RESPONSE      = 2;
    byte COMMAND_TYPE_NOTIFY        = 3;

    byte COMMAND_EQ                 = 0x20;
    byte COMMAND_MUSIC_CONTROL      = 0x21;
    byte COMMAND_KEY                = 0x22;
    byte COMMAND_AUTO_SHUTDOWN      = 0x23;
    byte COMMAND_FACTORY_RESET      = 0x24;
    byte COMMAND_WORK_MODE          = 0x25;
    byte COMMAND_IN_EAR_DETECT      = 0x26;
    byte COMMAND_DEVICE_INFO        = 0x27;
    byte COMMAND_NOTIFY             = 0x28;
    byte COMMAND_LANGUAGE           = 0x29;
    byte COMMAND_FIND_DEVICE        = 0x2A;
    byte COMMAND_AUTO_ANSWER        = 0x2B;
    byte COMMAND_ANC_MODE           = 0x2C;
    byte COMMAND_BLUETOOTH_NAME     = 0x2D;
    byte COMMAND_LED_MODE           = 0x2E;
    byte COMMAND_CLEAR_PAIR_RECORD  = 0x2F;
    byte COMMAND_ANC_GAIN           = 0x30;
    byte COMMAND_TRANSPARENCY_GAIN  = 0x31;
    byte COMMAND_SOUND_EFFECT_3D    = 0x32;

    byte INFO_DEVICE_POWER          = 0x01;
    byte INFO_FIRMWARE_VERSION      = 0x02;
    byte INFO_BLUETOOTH_NAME        = 0x03;
    byte INFO_EQ_SETTING            = 0x04;
    byte INFO_KEY_SETTINGS          = 0x05;
    byte INFO_DEVICE_VOLUME         = 0x06;
    byte INFO_PLAY_STATE            = 0x07;
    byte INFO_WORK_MODE             = 0x08;
    byte INFO_IN_EAR_STATUS         = 0x09;
    byte INFO_LANGUAGE_SETTING      = 0x0A;
    byte INFO_AUTO_ANSWER           = 0x0B;
    byte INFO_ANC_MODE              = 0x0C;
    byte INFO_IS_TWS                = 0x0D;
    byte INFO_TWS_CONNECTED         = 0x0E;
    byte INFO_LED_SWITCH            = 0x0F;
    byte INFO_FW_CHECKSUM           = 0x10;
    byte INFO_ANC_GAIN              = 0x11;
    byte INFO_TRANSPARENCY_GAIN     = 0x12;
    byte INFO_ANC_GAIN_NUM          = 0x13;
    byte INFO_TRANSPARENCY_GAIN_NUM = 0x14;
    byte INFO_ALL_EQ_SETTINGS       = 0x15;
    byte INFO_MAIN_SIDE             = 0x16;
    byte INFO_PRODUCT_COLOR         = 0x17;
    byte INFO_SOUND_EFFECT_3D       = 0x18;
    byte INFO_DEVICE_CAPABILITIES   = (byte) 0xFE;
    byte INFO_MAX_PACKET_SIZE       = (byte) 0xFF;

    byte NOTIFICATION_DEVICE_POWER      = 0x01;
    byte NOTIFICATION_EQ_SETTING        = 0x04;
    byte NOTIFICATION_KEY_SETTINGS      = 0x05;
    byte NOTIFICATION_DEVICE_VOLUME     = 0x06;
    byte NOTIFICATION_PLAY_STATE        = 0x07;
    byte NOTIFICATION_WORK_MODE         = 0x08;
    byte NOTIFICATION_IN_EAR_STATUS     = 0x09;
    byte NOTIFICATION_LANGUAGE_SETTING  = 0x0A;
    byte NOTIFICATION_ANC_MODE          = 0x0C;
    byte NOTIFICATION_TWS_CONNECTED     = 0x0E;
    byte NOTIFICATION_LED_SWITCH        = 0x0F;
    byte NOTIFICATION_ANC_GAIN          = 0x11;
    byte NOTIFICATION_TRANSPARENCY_GAIN = 0x12;
    byte NOTIFICATION_MAIN_SIDE         = 0x16;
    byte NOTIFICATION_SOUND_EFFECT_3D   = 0x18;

    byte getCommand();
    byte getCommandType();
    byte[] getPayload();
}
