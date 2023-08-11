package com.bluetrum.abmate.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.bluetrum.abmate.cmd.SimpleRequestCallback2;
import com.bluetrum.devicemanager.DeviceCommManager;
import com.bluetrum.devicemanager.cmd.Request;
import com.bluetrum.devicemanager.cmd.request.AncGainRequest;
import com.bluetrum.devicemanager.cmd.request.AncModeRequest;
import com.bluetrum.devicemanager.cmd.request.AutoAnswerRequest;
import com.bluetrum.devicemanager.cmd.request.AutoShutdownRequest;
import com.bluetrum.devicemanager.cmd.request.BluetoothNameRequest;
import com.bluetrum.devicemanager.cmd.request.ClearPairRecordRequest;
import com.bluetrum.devicemanager.cmd.request.EqRequest;
import com.bluetrum.devicemanager.cmd.request.FactoryResetRequest;
import com.bluetrum.devicemanager.cmd.request.FindDeviceRequest;
import com.bluetrum.devicemanager.cmd.request.InEarDetectRequest;
import com.bluetrum.devicemanager.cmd.request.KeyRequest;
import com.bluetrum.devicemanager.cmd.request.LanguageRequest;
import com.bluetrum.devicemanager.cmd.request.LedSwitchRequest;
import com.bluetrum.devicemanager.cmd.request.MusicControlRequest;
import com.bluetrum.devicemanager.cmd.request.TransparencyGainRequest;
import com.bluetrum.devicemanager.cmd.request.WorkModeRequest;
import com.bluetrum.devicemanager.models.ABDevice;
import com.bluetrum.devicemanager.models.DevicePower;
import com.bluetrum.devicemanager.models.RemoteEqSetting;

import java.util.List;
import java.util.Map;

abstract class BaseViewModel extends ViewModel {

    final DeviceRepository deviceRepository;

    BaseViewModel(@NonNull final DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public final DeviceRepository getDeviceRepository() {
        return deviceRepository;
    }


    public LiveData<Integer> getDeviceConnectionState() {
        return deviceRepository.getDeviceConnectionState();
    }

    public LiveData<ABDevice> getPopupDevice() {
        return deviceRepository.getPopupDevice();
    }

    public LiveData<ABDevice> getPreparingDevice() {
        return deviceRepository.getPreparingDevice();
    }

    public LiveData<ABDevice> getActiveDevice() {
        return deviceRepository.getActiveDevice();
    }

//    /**
//     * 获取所有 {@link BaseDevice}，LiveData
//     * @return 所有 {@link BaseDevice} 的LiveData
//     */
//    public LiveData<List<BaseDevice>> getAllDevices() {
//        return deviceRepository.getBaseDevices();
//    }
//
//    public void deleteDevice(@NonNull Device device) {
//        deviceRepository.deleteBtDevice(device);
//    }

    /* 命令相关 */

    /**
     * Send request, and don't care about response.
     * @param request Request
     */
    public void sendRequest(@NonNull final Request request) {
        deviceRepository.getDeviceCommManager().sendRequest(request);
    }

    /**
     * Send request, and handle response.
     * @param request Request
     * @param requestCallback Result callback
     */
    public void sendRequest(@NonNull final Request request,
                            @Nullable final DeviceCommManager.RequestCallback requestCallback) {
        // todo: check device
        deviceRepository.getDeviceCommManager().sendRequest(request, requestCallback);
    }

    /**
     * Send request only for with a response of boolean, others, eg. TLV, will get nothing.
     * @param request Request with a response of boolean
     * @param callback2 Result callback
     */
    public void sendRequest(@NonNull final Request request,
                            @NonNull final SimpleRequestCallback2 callback2) {
        sendRequest(request, new DeviceCommManager.RequestCallback() {
            @Override
            public void onComplete(@NonNull Request request, @Nullable Object result) {
                if (result instanceof Boolean) {
                    callback2.onComplete((boolean) result);
                }
            }

            @Override
            public void onTimeout(@NonNull Request request) {
                callback2.onTimeout();
            }
        });
    }

    /* 设备状态 */

    public LiveData<DevicePower> getDevicePower() {
        return deviceRepository.getDevicePower();
    }

    public LiveData<Integer> getDeviceFirmwareVersion() {
        return deviceRepository.getDeviceFirmwareVersion();
    }

    public LiveData<String> getDeviceName() {
        return deviceRepository.getDeviceName();
    }

    public LiveData<RemoteEqSetting> getDeviceEqSetting() {
        return deviceRepository.getDeviceEqSetting();
    }

    public LiveData<Map<Integer, Integer>> getDeviceKeySettings() {
        return deviceRepository.getDeviceKeySettings();
    }

    public LiveData<Byte> getDeviceVolume() {
        return deviceRepository.getDeviceVolume();
    }

    public LiveData<Boolean> getDevicePlayState() {
        return deviceRepository.getDevicePlayState();
    }

    public LiveData<Byte> getDeviceWorkMode() {
        return deviceRepository.getDeviceWorkMode();
    }

    public LiveData<Boolean> getDeviceInEarStatus() {
        return deviceRepository.getDeviceInEarStatus();
    }

    public LiveData<Byte> getDeviceLanguageSetting() {
        return deviceRepository.getDeviceLanguageSetting();
    }

    public LiveData<Boolean> getDeviceAutoAnswer() {
        return deviceRepository.getDeviceAutoAnswer();
    }

    public LiveData<Byte> getDeviceAncMode() {
        return deviceRepository.getDeviceAncMode();
    }

    public LiveData<Boolean> getDeviceIsTws() {
        return deviceRepository.getDeviceIsTws();
    }

    public LiveData<Boolean> getDeviceTwsConnected() {
        return deviceRepository.getDeviceTwsConnected();
    }

    public LiveData<Boolean> getDeviceLedSwitch() {
        return deviceRepository.getDeviceLedSwitch();
    }

    public LiveData<Integer> getDeviceAncGain() {
        return deviceRepository.getDeviceAncGain();
    }

    public LiveData<Integer> getDeviceTransparencyGain() {
        return deviceRepository.getDeviceTransparencyGain();
    }

    public LiveData<Integer> getDeviceAncGainNum() {
        return deviceRepository.getDeviceAncGainNum();
    }

    public LiveData<Integer> getDeviceTransparencyGainNum() {
        return deviceRepository.getDeviceTransparencyGainNum();
    }

    public LiveData<List<RemoteEqSetting>> getDeviceRemoteEqSettings() {
        return deviceRepository.getDeviceRemoteEqSettings();
    }

    public LiveData<byte[]> getDeviceFWChecksum() {
        return deviceRepository.getDeviceFwChecksum();
    }

    public LiveData<Boolean> getDeviceLeftIsMainSide() {
        return deviceRepository.getDeviceLeftIsMainSide();
    }

    public LiveData<Integer> getDeviceProductColor() {
        return deviceRepository.getDeviceProductColor();
    }

    public LiveData<Boolean> getDeviceSoundEffect3d() {
        return deviceRepository.getDeviceSoundEffect3d();
    }

    public LiveData<Integer> getDeviceCapacities() {
        return deviceRepository.getDeviceCapacities();
    }

    public LiveData<Short> getDeviceMaxPacketSize() {
        return deviceRepository.getDeviceMaxPacketSize();
    }

    // Requests

    // EQ Setting

    public void sendEqRequest(@NonNull EqRequest request, @NonNull SimpleRequestCallback2 callback) {
        sendRequest(request, callback);
    }

    // Auto Shutdown

    public void setAutoShutdownSetting(byte setting, @NonNull SimpleRequestCallback2 callback) {
        AutoShutdownRequest request = new AutoShutdownRequest(setting);
        sendRequest(request, callback);
    }

    // Factory Reset

    public void doFactoryReset(@NonNull SimpleRequestCallback2 callback) {
        FactoryResetRequest request = new FactoryResetRequest();
        sendRequest(request, callback);
    }

    // Work Mode

    public void setWorkMode(byte mode, @NonNull SimpleRequestCallback2 callback) {
        WorkModeRequest request = new WorkModeRequest(mode);
        sendRequest(request, callback);
    }

    // In Ear Detect

    public void enableInEarDetect(boolean enable, @NonNull SimpleRequestCallback2 callback) {
        InEarDetectRequest request = new InEarDetectRequest(enable);
        sendRequest(request, callback);
    }

    // Language Setting

    public void setLanguageSetting(byte languageSetting, @NonNull SimpleRequestCallback2 callback) {
        LanguageRequest request = new LanguageRequest(languageSetting);
        sendRequest(request, callback);
    }

    // Find Device

    public void doFindDevice(boolean enable, @NonNull SimpleRequestCallback2 callback) {
        FindDeviceRequest request = new FindDeviceRequest(enable);
        sendRequest(request, callback);
    }

    // Auto Answer

    public void enableAutoAnswer(boolean enable, @NonNull SimpleRequestCallback2 callback) {
        AutoAnswerRequest request = new AutoAnswerRequest(enable);
        sendRequest(request, callback);
    }

    // ANC Mode

    public void setAncMode(byte mode, @NonNull SimpleRequestCallback2 callback) {
        AncModeRequest request = new AncModeRequest(mode);
        sendRequest(request, callback);
    }

    // Bluetooth Name

    public void setBluetoothName(@NonNull String bluetoothName, @NonNull SimpleRequestCallback2 callback) {
        BluetoothNameRequest request = new BluetoothNameRequest(bluetoothName);
        sendRequest(request, callback);
    }

    // LED Mode

    public void setLedOn(boolean ledOn, @NonNull SimpleRequestCallback2 callback) {
        LedSwitchRequest request = new LedSwitchRequest(ledOn);
        sendRequest(request, callback);
    }

    // Clear Pair Record

    public void doClearPairRecord(@NonNull SimpleRequestCallback2 callback) {
        ClearPairRecordRequest request = new ClearPairRecordRequest();
        sendRequest(request, callback);
    }

    // ANC Gain

    public void setAncGain(byte gain, @NonNull SimpleRequestCallback2 callback) {
        AncGainRequest request = new AncGainRequest(gain);
        sendRequest(request, callback);
    }

    // Transparency Gain

    public void setTransparencyGain(byte gain, @NonNull SimpleRequestCallback2 callback) {
        TransparencyGainRequest request = new TransparencyGainRequest(gain);
        sendRequest(request, callback);
    }

    // Music Control

    public void sendMusicControlRequest(@NonNull MusicControlRequest request, @NonNull SimpleRequestCallback2 callback) {
        sendRequest(request, new DeviceCommManager.RequestCallback() {
            @Override
            public void onComplete(@NonNull Request _request, @Nullable Object result) {
                if (result instanceof Map) {
                    Map<Byte, Boolean> resultMap = (Map<Byte, Boolean>) result;
                    Boolean controlResult = resultMap.get(request.getControlType());
                    if (controlResult != null) {
                        callback.onComplete(controlResult);
                    }
                }
            }

            @Override
            public void onTimeout(@NonNull Request request) {
                callback.onTimeout();
            }
        });
    }

    // Key Setting

    public void setKeySetting(byte keyType, byte keyFunction, @NonNull SimpleRequestCallback2 callback) {
        KeyRequest request = new KeyRequest(keyType, keyFunction);
        sendRequest(request, new DeviceCommManager.RequestCallback() {
            @Override
            public void onComplete(@NonNull Request request, @Nullable Object result) {
                if (result instanceof Map) {
                    Map<Byte, Boolean> resultMap = (Map<Byte, Boolean>) result;
                    Boolean keyResult = resultMap.get(keyType);
                    if (keyResult != null) {
                        callback.onComplete(keyResult);
                    }
                }
            }

            @Override
            public void onTimeout(@NonNull Request request) {
                callback.onTimeout();
            }
        });
    }

}
