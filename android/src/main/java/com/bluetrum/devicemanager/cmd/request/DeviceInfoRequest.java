package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

import java.io.ByteArrayOutputStream;

public final class DeviceInfoRequest extends Request {

    private byte[] payload = new byte[0];

    private DeviceInfoRequest() {
        super(COMMAND_DEVICE_INFO);
    }

    public DeviceInfoRequest(byte info) {
        super(COMMAND_DEVICE_INFO);
        requireInfo(info);
    }

    public DeviceInfoRequest requireInfo(byte info) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(payload, 0, payload.length);
        os.write(info);
        os.write(0);
        payload = os.toByteArray();
        return this;
    }

    public DeviceInfoRequest requireDevicePower() {
        return requireInfo(INFO_DEVICE_POWER);
    }

    public DeviceInfoRequest requireFirmwareVersion() {
        return requireInfo(INFO_FIRMWARE_VERSION);
    }

    public DeviceInfoRequest requireBluetoothName() {
        return requireInfo(INFO_BLUETOOTH_NAME);
    }

    public DeviceInfoRequest requireEqSettings() {
        return requireInfo(INFO_EQ_SETTING);
    }

    public DeviceInfoRequest requireKeySettings() {
        return requireInfo(INFO_KEY_SETTINGS);
    }

    public DeviceInfoRequest requireDeviceVolume() {
        return requireInfo(INFO_DEVICE_VOLUME);
    }

    public DeviceInfoRequest requirePlayState() {
        return requireInfo(INFO_PLAY_STATE);
    }

    public DeviceInfoRequest requireWorkMode() {
        return requireInfo(INFO_WORK_MODE);
    }

    public DeviceInfoRequest requireInEarStatus() {
        return requireInfo(INFO_IN_EAR_STATUS);
    }

    public DeviceInfoRequest requireLanguageSetting() {
        return requireInfo(INFO_LANGUAGE_SETTING);
    }

    public DeviceInfoRequest requireAutoAnswer() {
        return requireInfo(INFO_AUTO_ANSWER);
    }

    public DeviceInfoRequest requireAncMode() {
        return requireInfo(INFO_ANC_MODE);
    }

    public DeviceInfoRequest requireIsTws() {
        return requireInfo(INFO_IS_TWS);
    }

    public DeviceInfoRequest requireTwsConnected() {
        return requireInfo(INFO_TWS_CONNECTED);
    }

    public DeviceInfoRequest requireLedSwitch() {
        return requireInfo(INFO_LED_SWITCH);
    }

    public DeviceInfoRequest requireFwChecksum() {
        return requireInfo(INFO_FW_CHECKSUM);
    }

    public DeviceInfoRequest requireAncGain() {
        return requireInfo(INFO_ANC_GAIN);
    }

    public DeviceInfoRequest requireTransparencyGain() {
        return requireInfo(INFO_TRANSPARENCY_GAIN);
    }

    public DeviceInfoRequest requireAncGainNum() {
        return requireInfo(INFO_ANC_GAIN_NUM);
    }

    public DeviceInfoRequest requireTransparencyGainNum() {
        return requireInfo(INFO_TRANSPARENCY_GAIN_NUM);
    }

    public DeviceInfoRequest requireAllEqSettings() {
        return requireInfo(INFO_ALL_EQ_SETTINGS);
    }

    public DeviceInfoRequest requireMainSide() {
        return requireInfo(INFO_MAIN_SIDE);
    }

    public DeviceInfoRequest requireProductColor() {
        return requireInfo(INFO_PRODUCT_COLOR);
    }

    public DeviceInfoRequest requireSoundEffect3d() {
        return requireInfo(INFO_SOUND_EFFECT_3D);
    }

    public DeviceInfoRequest requireDeviceCapabilities() {
        return requireInfo(INFO_DEVICE_CAPABILITIES);
    }

    public DeviceInfoRequest requireMaxPacketSize() {
        return requireInfo(INFO_MAX_PACKET_SIZE);
    }

    public static DeviceInfoRequest defaultInfoRequest() {
        return new DeviceInfoRequest()
                .requireDevicePower()
                .requireFirmwareVersion()
                .requireBluetoothName()
                .requireEqSettings()
                .requireKeySettings()
                .requireDeviceVolume()
                .requirePlayState()
                .requireWorkMode()
                .requireInEarStatus()
                .requireLanguageSetting()
                .requireAutoAnswer()
                .requireAncMode()
                .requireIsTws()
                .requireTwsConnected()
                .requireLedSwitch()
                .requireFwChecksum()
                .requireAncGain()
                .requireTransparencyGain()
                .requireAncGainNum()
                .requireTransparencyGainNum()
                .requireAllEqSettings()
                .requireMainSide()
                .requireProductColor()
                .requireSoundEffect3d()
                .requireDeviceCapabilities();
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

}
