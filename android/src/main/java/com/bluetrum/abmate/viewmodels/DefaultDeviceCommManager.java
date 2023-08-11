package com.bluetrum.abmate.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bluetrum.devicemanager.DeviceCommManager;
import com.bluetrum.devicemanager.cmd.Command;
import com.bluetrum.devicemanager.cmd.Request;
import com.bluetrum.devicemanager.cmd.payloadhandler.BooleanPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.BytePayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.ByteToIntegerPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.FirmwareChecksumPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.IntegerPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.KeyPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.PowerPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.RemoteEqSettingPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.RemoteEqSettingsPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.ResponsePayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.StringPayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.TlvResponsePayloadHandler;
import com.bluetrum.devicemanager.cmd.payloadhandler.UInt16ToIntegerPayloadHandler;
import com.bluetrum.devicemanager.cmd.request.KeyRequest;
import com.bluetrum.devicemanager.cmd.request.MusicControlRequest;
import com.bluetrum.devicemanager.models.DevicePower;
import com.bluetrum.devicemanager.models.RemoteEqSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class DefaultDeviceCommManager extends DeviceCommManager {

    /* 设备状态 */

    private final MutableLiveData<DevicePower> devicePower = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> deviceFirmwareVersion = new MutableLiveData<>(null);
    private final MutableLiveData<String> deviceName = new MutableLiveData<>(null);
    private final MutableLiveData<RemoteEqSetting> deviceEqSetting = new MutableLiveData<>(null);
    private final MutableLiveData<Map<Integer, Integer>> deviceKeySettings = new MutableLiveData<>(null);
    private final MutableLiveData<Byte> deviceVolume = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> devicePlayState = new MutableLiveData<>(null);
    private final MutableLiveData<Byte> deviceWorkMode = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deviceInEarStatus = new MutableLiveData<>(null);
    private final MutableLiveData<Byte> deviceLanguageSetting = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deviceAutoAnswer = new MutableLiveData<>(null);
    private final MutableLiveData<Byte> deviceAncMode = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deviceIsTws = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deviceTwsConnected = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deviceLedSwitch = new MutableLiveData<>(null);
    private final MutableLiveData<byte[]> deviceFwChecksum = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> deviceAncGain = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> deviceTransparencyGain = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> deviceAncGainNum = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> deviceTransparencyGainNum = new MutableLiveData<>(null);
    private final MutableLiveData<List<RemoteEqSetting>> deviceRemoteEqSettings = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deviceLeftIsMainSide = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> deviceProductColor = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> deviceSoundEffect3d = new MutableLiveData<>(null);
    private final MutableLiveData<Integer> deviceCapacities = new MutableLiveData<>(null);

    // Request结果（如果成功）反映到DeviceInfo
    private boolean enableRequestToDevInfo = true;
    private final Map<Byte, Byte> requestToDeviceInfoMap = new HashMap<>();

    public DefaultDeviceCommManager() {
        super();
        registerDefaultResponseCallables();
        registerDefaultNotificationCallables();
        registerDefaultDeviceInfoCallables();
        registerRequestToDeviceInfoMaps();
    }

    @Override
    public void sendRequest(@NonNull final Request request,
                            @Nullable final RequestCallback requestCallback) {
        RequestCallback callback = new RequestCallback() {
            @Override
            public void onComplete(@NonNull Request request, @Nullable Object result) {
                if (requestCallback != null) {
                    requestCallback.onComplete(request, result);
                }
                // 处理完Response，再看要不要将结果反应到DevInfo
                if (enableRequestToDevInfo) {
                    processRequestToDevInfo(request, result);
                }
            }
            @Override
            public void onTimeout(@NonNull Request request) {
                if (requestCallback != null) {
                    requestCallback.onTimeout(request);
                }
            }
        };
        super.sendRequest(request, callback);
    }

    private void processRequestToDevInfo(final Request request, final Object result) {
        // 获取Command -> DevInfo映射
        Byte infoType = requestToDeviceInfoMap.get(request.getCommand());
        if (infoType != null) {
            // ResponsePayloadHandler.class
            if (result instanceof Boolean && (boolean) result) {
                processDeviceInfo(infoType, request.getPayload());
            }
            // TlvResponsePayloadHandler.class
            // Update LiveData directly, or sendRequest to get latest status
            // Here choose the first one method
            else if (result instanceof Map) {
                // Key
                if (request instanceof KeyRequest) {
                    byte keyType = ((KeyRequest) request).getKeyType();
                    Boolean keyResult = ((Map<Byte, Boolean>) result).get(keyType);
                    if (keyResult != null && keyResult) {
                        // Update all key settings, not for only one
                        HashMap<Integer, Integer> keySetting = new HashMap<>();
                        keySetting.put((int) keyType, (int) ((KeyRequest) request).getKeyFunction());
                        deviceKeySettingsChanged(keySetting);
                    }
                }
                // Volume
                else if (request instanceof MusicControlRequest) {
                    byte controlType = ((MusicControlRequest) request).getControlType();
                    if (controlType == MusicControlRequest.CONTROL_TYPE_VOLUME) {
                        Boolean controlResult = ((Map<Byte, Boolean>) result).get(controlType);
                        if (controlResult != null && controlResult) {
                            byte[] volumePayload = new byte[] { ((MusicControlRequest) request).getVolume() };
                            processDeviceInfo(infoType, volumePayload);
                        }
                    }
                }
            }
        }
    }

    public void enableRequestToDevInfo(boolean enable) {
        this.enableRequestToDevInfo = enable;
    }

    public void registerRequestToDeviceInfoMap(final byte command, final byte deviceInfo) {
        Timber.d("registerCommandToDeviceInfoMap: %s -> %s", command, deviceInfo);
        requestToDeviceInfoMap.put(command, deviceInfo);
    }

    public void unregisterRequestToDeviceInfoMap(final byte command) {
        requestToDeviceInfoMap.remove(command);
    }

    /* */

    private void deviceKeySettingsChanged(final Map<Integer, Integer> keySetting) {
        Map<Integer, Integer> keySettings = deviceKeySettings.getValue();
        if (keySettings != null) {
            keySettings.putAll(keySetting);
        } else {
            keySettings = keySetting;
        }
        deviceKeySettings.setValue(keySettings);
    }

    /* 回复数据处理 */

    private void registerDefaultResponseCallables() {
        registerResponseCallable(Command.COMMAND_DEVICE_INFO, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_EQ, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_AUTO_SHUTDOWN, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_FACTORY_RESET, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_WORK_MODE, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_IN_EAR_DETECT, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_LANGUAGE, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_FIND_DEVICE, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_AUTO_ANSWER, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_ANC_MODE, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_BLUETOOTH_NAME, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_LED_MODE, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_CLEAR_PAIR_RECORD, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_ANC_GAIN, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_TRANSPARENCY_GAIN, ResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_SOUND_EFFECT_3D, ResponsePayloadHandler.class);

        registerResponseCallable(Command.COMMAND_MUSIC_CONTROL, TlvResponsePayloadHandler.class);
        registerResponseCallable(Command.COMMAND_KEY, TlvResponsePayloadHandler.class);
    }

    private void registerDefaultNotificationCallables() {
        registerNotificationCallback(Command.NOTIFICATION_DEVICE_POWER, PowerPayloadHandler.class, devicePower::setValue);
        registerNotificationCallback(Command.NOTIFICATION_EQ_SETTING, RemoteEqSettingPayloadHandler.class, deviceEqSetting::setValue);
        registerNotificationCallback(Command.NOTIFICATION_KEY_SETTINGS, KeyPayloadHandler.class, this::deviceKeySettingsChanged);
        registerNotificationCallback(Command.NOTIFICATION_DEVICE_VOLUME, BytePayloadHandler.class, deviceVolume::setValue);
        registerNotificationCallback(Command.NOTIFICATION_PLAY_STATE, BooleanPayloadHandler.class, devicePlayState::setValue);
        registerNotificationCallback(Command.NOTIFICATION_WORK_MODE, BytePayloadHandler.class, deviceWorkMode::setValue);
        registerNotificationCallback(Command.NOTIFICATION_IN_EAR_STATUS, BooleanPayloadHandler.class, deviceInEarStatus::setValue);
        registerNotificationCallback(Command.NOTIFICATION_LANGUAGE_SETTING, BytePayloadHandler.class, deviceLanguageSetting::setValue);
        registerNotificationCallback(Command.NOTIFICATION_ANC_MODE, BytePayloadHandler.class, deviceAncMode::setValue);
        registerNotificationCallback(Command.NOTIFICATION_TWS_CONNECTED, BooleanPayloadHandler.class, deviceTwsConnected::setValue);
        registerNotificationCallback(Command.NOTIFICATION_LED_SWITCH, BooleanPayloadHandler.class, deviceLedSwitch::setValue);
        registerNotificationCallback(Command.NOTIFICATION_ANC_GAIN, ByteToIntegerPayloadHandler.class, deviceAncGain::setValue);
        registerNotificationCallback(Command.NOTIFICATION_TRANSPARENCY_GAIN, ByteToIntegerPayloadHandler.class, deviceTransparencyGain::setValue);
        registerNotificationCallback(Command.NOTIFICATION_MAIN_SIDE, BooleanPayloadHandler.class, deviceLeftIsMainSide::setValue);
        registerNotificationCallback(Command.NOTIFICATION_SOUND_EFFECT_3D, BooleanPayloadHandler.class, deviceSoundEffect3d::setValue);
    }

    private void registerDefaultDeviceInfoCallables() {
        registerDeviceInfoCallback(Command.INFO_DEVICE_POWER, PowerPayloadHandler.class, devicePower::setValue);
        registerDeviceInfoCallback(Command.INFO_FIRMWARE_VERSION, IntegerPayloadHandler.class, deviceFirmwareVersion::setValue);
        registerDeviceInfoCallback(Command.INFO_BLUETOOTH_NAME, StringPayloadHandler.class, deviceName::setValue);
        registerDeviceInfoCallback(Command.INFO_EQ_SETTING, RemoteEqSettingPayloadHandler.class, deviceEqSetting::setValue);
        registerDeviceInfoCallback(Command.INFO_KEY_SETTINGS, KeyPayloadHandler.class, this::deviceKeySettingsChanged);
        registerDeviceInfoCallback(Command.INFO_DEVICE_VOLUME, BytePayloadHandler.class, deviceVolume::setValue);
        registerDeviceInfoCallback(Command.INFO_PLAY_STATE, BooleanPayloadHandler.class, devicePlayState::setValue);
        registerDeviceInfoCallback(Command.INFO_WORK_MODE, BytePayloadHandler.class, deviceWorkMode::setValue);
        registerDeviceInfoCallback(Command.INFO_IN_EAR_STATUS, BooleanPayloadHandler.class, deviceInEarStatus::setValue);
        registerDeviceInfoCallback(Command.INFO_LANGUAGE_SETTING, BytePayloadHandler.class, deviceLanguageSetting::setValue);
        registerDeviceInfoCallback(Command.INFO_AUTO_ANSWER, BooleanPayloadHandler.class, deviceAutoAnswer::setValue);
        registerDeviceInfoCallback(Command.INFO_ANC_MODE, BytePayloadHandler.class, deviceAncMode::setValue);
        registerDeviceInfoCallback(Command.INFO_IS_TWS, BooleanPayloadHandler.class, deviceIsTws::setValue);
        registerDeviceInfoCallback(Command.INFO_TWS_CONNECTED, BooleanPayloadHandler.class, deviceTwsConnected::setValue);
        registerDeviceInfoCallback(Command.INFO_LED_SWITCH, BooleanPayloadHandler.class, deviceLedSwitch::setValue);
        registerDeviceInfoCallback(Command.INFO_FW_CHECKSUM, FirmwareChecksumPayloadHandler.class, deviceFwChecksum::setValue);
        registerDeviceInfoCallback(Command.INFO_ANC_GAIN, ByteToIntegerPayloadHandler.class, deviceAncGain::setValue);
        registerDeviceInfoCallback(Command.INFO_TRANSPARENCY_GAIN, ByteToIntegerPayloadHandler.class, deviceTransparencyGain::setValue);
        registerDeviceInfoCallback(Command.INFO_ANC_GAIN_NUM, ByteToIntegerPayloadHandler.class, deviceAncGainNum::setValue);
        registerDeviceInfoCallback(Command.INFO_TRANSPARENCY_GAIN_NUM, ByteToIntegerPayloadHandler.class, deviceTransparencyGainNum::setValue);
        registerDeviceInfoCallback(Command.INFO_ALL_EQ_SETTINGS, RemoteEqSettingsPayloadHandler.class, deviceRemoteEqSettings::setValue);
        registerDeviceInfoCallback(Command.INFO_MAIN_SIDE, BooleanPayloadHandler.class, deviceLeftIsMainSide::setValue);
        registerDeviceInfoCallback(Command.INFO_PRODUCT_COLOR, IntegerPayloadHandler.class, deviceProductColor::setValue);
        registerDeviceInfoCallback(Command.INFO_SOUND_EFFECT_3D, BooleanPayloadHandler.class, deviceSoundEffect3d::setValue);
        registerDeviceInfoCallback(Command.INFO_DEVICE_CAPABILITIES, UInt16ToIntegerPayloadHandler.class, deviceCapacities::setValue);
    }

    private void registerRequestToDeviceInfoMaps() {
        registerRequestToDeviceInfoMap(Command.COMMAND_EQ, Command.INFO_EQ_SETTING);
        registerRequestToDeviceInfoMap(Command.COMMAND_WORK_MODE, Command.INFO_WORK_MODE);
        registerRequestToDeviceInfoMap(Command.COMMAND_IN_EAR_DETECT, Command.INFO_IN_EAR_STATUS);
        registerRequestToDeviceInfoMap(Command.COMMAND_LANGUAGE, Command.INFO_LANGUAGE_SETTING);
        registerRequestToDeviceInfoMap(Command.COMMAND_AUTO_ANSWER, Command.INFO_AUTO_ANSWER);
        registerRequestToDeviceInfoMap(Command.COMMAND_ANC_MODE, Command.INFO_ANC_MODE);
        registerRequestToDeviceInfoMap(Command.COMMAND_BLUETOOTH_NAME, Command.INFO_BLUETOOTH_NAME);
        registerRequestToDeviceInfoMap(Command.COMMAND_LED_MODE, Command.INFO_LED_SWITCH);
        registerRequestToDeviceInfoMap(Command.COMMAND_ANC_GAIN, Command.INFO_ANC_GAIN);
        registerRequestToDeviceInfoMap(Command.COMMAND_TRANSPARENCY_GAIN, Command.INFO_TRANSPARENCY_GAIN);
        registerRequestToDeviceInfoMap(Command.COMMAND_SOUND_EFFECT_3D, Command.INFO_SOUND_EFFECT_3D);

        registerRequestToDeviceInfoMap(Command.COMMAND_MUSIC_CONTROL, Command.INFO_DEVICE_VOLUME);
        registerRequestToDeviceInfoMap(Command.COMMAND_KEY, Command.INFO_KEY_SETTINGS);
    }

    /* 设备状态 */

    public LiveData<DevicePower> getDevicePower() {
        return devicePower;
    }

    public LiveData<Integer> getDeviceFirmwareVersion() {
        return deviceFirmwareVersion;
    }

    public LiveData<String> getDeviceName() {
        return deviceName;
    }

    public LiveData<RemoteEqSetting> getDeviceEqSetting() {
        return deviceEqSetting;
    }

    public LiveData<Map<Integer, Integer>> getDeviceKeySettings() {
        return deviceKeySettings;
    }

    public LiveData<Byte> getDeviceVolume() {
        return deviceVolume;
    }

    public LiveData<Boolean> getDevicePlayState() {
        return devicePlayState;
    }

    public LiveData<Byte> getDeviceWorkMode() {
        return deviceWorkMode;
    }

    public LiveData<Boolean> getDeviceInEarStatus() {
        return deviceInEarStatus;
    }

    public LiveData<Byte> getDeviceLanguageSetting() {
        return deviceLanguageSetting;
    }

    public LiveData<Boolean> getDeviceAutoAnswer() {
        return deviceAutoAnswer;
    }

    public LiveData<Byte> getDeviceAncMode() {
        return deviceAncMode;
    }

    public LiveData<Boolean> getDeviceIsTws() {
        return deviceIsTws;
    }

    public LiveData<Boolean> getDeviceTwsConnected() {
        return deviceTwsConnected;
    }

    public LiveData<Boolean> getDeviceLedSwitch() {
        return deviceLedSwitch;
    }

    public LiveData<byte[]> getDeviceFwChecksum() {
        return deviceFwChecksum;
    }

    public LiveData<Integer> getDeviceAncGain() {
        return deviceAncGain;
    }

    public LiveData<Integer> getDeviceTransparencyGain() {
        return deviceTransparencyGain;
    }

    public LiveData<Integer> getDeviceAncGainNum() {
        return deviceAncGainNum;
    }

    public LiveData<Integer> getDeviceTransparencyGainNum() {
        return deviceTransparencyGainNum;
    }

    public LiveData<List<RemoteEqSetting>> getDeviceRemoteEqSettings() {
        return deviceRemoteEqSettings;
    }

    public LiveData<Boolean> getDeviceLeftIsMainSide() {
        return deviceLeftIsMainSide;
    }

    public LiveData<Integer> getDeviceProductColor() {
        return deviceProductColor;
    }

    public LiveData<Boolean> getDeviceSoundEffect3d() {
        return deviceSoundEffect3d;
    }

    public LiveData<Integer> getDeviceCapacities() {
        return deviceCapacities;
    }

    public void resetDeviceStatus() {
        // 重置LiveData
        devicePower.postValue(null);
        deviceFirmwareVersion.postValue(null);
        deviceName.postValue(null);
        deviceEqSetting.postValue(null);
        deviceKeySettings.postValue(null);
        deviceVolume.postValue(null);
        devicePlayState.postValue(null);
        deviceWorkMode.postValue(null);
        deviceInEarStatus.postValue(null);
        deviceLanguageSetting.postValue(null);
        deviceAutoAnswer.postValue(null);
        deviceAncMode.postValue(null);
        deviceIsTws.postValue(null);
        deviceTwsConnected.postValue(null);
        deviceLedSwitch.postValue(null);
        deviceAncGain.postValue(null);
        deviceTransparencyGain.postValue(null);
        deviceAncGainNum.postValue(null);
        deviceTransparencyGainNum.postValue(null);
        deviceRemoteEqSettings.postValue(null);
        deviceLeftIsMainSide.postValue(null);
        deviceProductColor.postValue(null);
        deviceSoundEffect3d.postValue(null);
        deviceCapacities.postValue(null);
    }

}
