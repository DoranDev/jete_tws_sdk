package com.bluetrum.abmate.viewmodels;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;

import com.bluetrum.abmate.BuildConfig;
import com.bluetrum.abmate.bluetooth.BleScanManager;
import com.bluetrum.abmate.models.ABEarbuds;
import com.bluetrum.abmate.utils.Utils;
import com.bluetrum.devicemanager.DeviceCommManager;
import com.bluetrum.devicemanager.DeviceManagerApi;
import com.bluetrum.devicemanager.cmd.Command;
import com.bluetrum.devicemanager.cmd.payloadhandler.MtuPayloadHandler;
import com.bluetrum.devicemanager.cmd.request.DeviceInfoRequest;
import com.bluetrum.devicemanager.models.ABDevice;
import com.bluetrum.devicemanager.models.DeviceBeacon;
import com.bluetrum.devicemanager.models.DevicePower;
import com.bluetrum.devicemanager.models.EarbudsBeacon;
import com.bluetrum.devicemanager.models.RemoteEqSetting;
import com.bluetrum.utils.ParserUtils;
import com.bluetrum.utils.ThreadUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import timber.log.Timber;
@SuppressLint("LogNotTimber")
@Singleton
public class DeviceRepository implements ABEarbuds.ConnectionStateCallback, ABEarbuds.DataDelegate, DeviceCommManager.DeviceResponseErrorHandler {

    public static final int DEVICE_CONNECTION_STATE_IDLE            = 0;
    public static final int DEVICE_CONNECTION_STATE_PAIRING         = 1;
    public static final int DEVICE_CONNECTION_STATE_PAIRED          = 2;
    public static final int DEVICE_CONNECTION_STATE_CONNECTING      = 3;
    public static final int DEVICE_CONNECTION_STATE_CONNECTED       = 4;
    public static final int DEVICE_CONNECTION_STATE_AUTHENTICATING  = 5;
    public static final int DEVICE_CONNECTION_STATE_AUTHENTICATED   = 6;

    private static final String THREAD_NAME_RETRY_CONNECT_DEVICE = "THREAD_NAME_RETRY_CONNECT_DEVICE";
    private static final String THREAD_NAME_PREPARING_DEVICE     = "THREAD_NAME_PREPARING_DEVICE";

    private final Context mContext;
    private final DeviceManagerApi mDeviceManagerApi;
    private final DefaultDeviceCommManager mDeviceCommManager;

    private int mBroadcastReceiverRegisterCount = 0;

    private boolean mIsScanning;

    // 当前已配对设备，用来判断扫描到的Beacon是否与其有关联
    private Set<BluetoothDevice> mBondedDevices;

    // 设备连接状态
    private final MutableLiveData<Integer> deviceConnectionState = new MutableLiveData<>(DEVICE_CONNECTION_STATE_IDLE);
    // 目前弹窗的设备
    private final MutableLiveData<ABDevice> mPopupDevice = new MutableLiveData<>(null);
    // 仅用来记录正在准备连接的设备
    private final MutableLiveData<ABDevice> mPreparingDevice = new MutableLiveData<>(null);
    // 用来记录当前活动的设备
    private final MutableLiveData<ABDevice> mActiveDevice = new MutableLiveData<>(null);

    private final Handler retryConnectDeviceHandler;
    private final Runnable retryConnectDeviceRunnable = this::retryConnectDevice;
    private static final int RETRY_CONNECT_DEVICE_INTERVAL = 200; // ms
    private static final int MAX_RETRY_COUNT = 5;
    private int retryCount;

    // 如果设备端清除配对记录，则手机端在连接时会进行重连
    // 此时SPP已经处于连接状态，重新配对完成后不需要再次连接SPP
    private boolean re_pairing;

    private final MutableLiveData<Short> deviceMaxPacketSize = new MutableLiveData<>(null);

    public LiveData<Integer> getDeviceConnectionState() {
        return deviceConnectionState;
    }

    public LiveData<ABDevice> getPopupDevice() {
        return mPopupDevice;
    }

    public LiveData<ABDevice> getPreparingDevice() {
        return mPreparingDevice;
    }

    public LiveData<ABDevice> getActiveDevice() {
        return mActiveDevice;
    }

    private BluetoothAdapter mAdapter;
    private BluetoothHeadset mBluetoothHeadset;

    private String deviceAddressDisconnectedByUser;

    // 屏蔽不弹窗时间定义
    public static final int BLOCK_TIME_TEMP = 0;
    public static final int BLOCK_TIME_FOREVER = -1;
    // 快速配对时，点击取消后将不再弹窗，直到这个时间扫描不到设备，就从mPopupBlocklist中移除
    private static final int PREPARE_DEVICE_BLOCK_TIMEOUT = 10000;
    // 临时屏蔽列表，暂时不需要弹窗的设备地址放到这里
    private final Set<String> mTempBlocklist = new HashSet<>();

    // 设备活跃最大时间间隔，超过此间隔将被从发现列表中移除
    private static final int PREPARE_DEVICE_BEACON_INTERVAL = 1000;
    // 弹窗延迟，这段时间内连续扫描到设备才会弹窗
    private static final int PREPARE_DEVICE_DELAY_TIME = 3000;
    // 弹窗超时，这段时间都没扫描到设备的话，就会取消弹窗
    private static final int PREPARE_DEVICE_TIMEOUT = 2000;
    private final Map<String, Date> mTimeFirstFound = new HashMap<>();
    private final Map<String, Date> mTimeLastSeen = new HashMap<>();

    private final Handler mPopupDeviceHandler;
    private final Runnable mClearPopupDeviceRunnable = this::clearPopupDevice;

    private final MutableLiveData<Boolean> mScanningState = new MutableLiveData<>(false);

    public LiveData<Boolean> getScanningState() {
        return mScanningState;
    }

    @Inject
    public DeviceRepository(@ApplicationContext @NonNull final Context context,
                            @NonNull final DeviceManagerApi deviceManagerApi,
                            @NonNull final DefaultDeviceCommManager deviceCommManager) {
        this.mContext = context;
        this.mDeviceManagerApi = deviceManagerApi;
        this.mDeviceCommManager = deviceCommManager;

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = bluetoothManager.getAdapter();
        mAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET);

        updateBondedDevices();

        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

        HandlerThread retryConnectDeviceThread = new HandlerThread(THREAD_NAME_RETRY_CONNECT_DEVICE);
        retryConnectDeviceThread.start();
        retryConnectDeviceHandler = new Handler(retryConnectDeviceThread.getLooper());

        // 处理PreparingDevice的线程和Handler
        HandlerThread preparingDeviceThread = new HandlerThread(THREAD_NAME_PREPARING_DEVICE);
        preparingDeviceThread.start();
        mPopupDeviceHandler = new Handler(preparingDeviceThread.getLooper());
    }

    public DeviceCommManager getDeviceCommManager() {
        return mDeviceCommManager;
    }

    @SuppressLint("MissingPermission")
    public boolean headsetIsConnected(BluetoothDevice device) {
        if (Utils.isBluetoothScanAndConnectPermissionsGranted(mContext)) {
            return mBluetoothHeadset != null && mBluetoothHeadset.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED;
        }
        return false;
    }

    @SuppressLint("MissingPermission")
    private void updateBondedDevices() {
        if (Utils.isBluetoothScanAndConnectPermissionsGranted(mContext)) {
            Timber.d("BLUETOOTH_CONNECT permission granted");
            mBondedDevices = mAdapter.getBondedDevices();
        } else {
            Timber.w("BLUETOOTH_CONNECT permission denied");
        }
    }

    private boolean isPopupDevice(BluetoothDevice device) {
        ABDevice popupDevice = mPopupDevice.getValue();
        return popupDevice != null && popupDevice.getAddress().equals(device.getAddress());
    }

    private boolean isPreparingDevice(BluetoothDevice device) {
        ABDevice preparingDevice = mPreparingDevice.getValue();
        return preparingDevice != null && preparingDevice.getAddress().equals(device.getAddress());
    }

    private boolean isActiveDevice(BluetoothDevice device) {
        ABDevice activeDevice = mActiveDevice.getValue();
        return activeDevice != null && activeDevice.getAddress().equals(device.getAddress());
    }

    /**
     * 设备是否已经配对
     * @param deviceAddress 设备地址
     * @return 是否已经配对
     */
    private boolean isBondedDevice(String deviceAddress) {
//        if (mBondedDevices == null) {
//            updateBondedDevices();
//        }
//        return mBondedDevices != null && mBondedDevices.stream().anyMatch(device -> device.getAddress().equals(deviceAddress));
        if (mBondedDevices == null) {
            return false;
        }

        for (BluetoothDevice device : mBondedDevices) {
            if (device.getAddress().equals(deviceAddress)) {
                return true;
            }
        }

        return false;
    }

    public void bondDevice(ABDevice device) {
        ThreadUtils.postOnMainThread(() -> {
            // 停止扫描
            if (mIsScanning) {
                stopScan();
            }
            // 处理之后，屏蔽此设备，相当于只取第一次检测到已连接的情况
           // mTempBlocklist.add(device.getAddress());

            deviceConnectionState.setValue(DEVICE_CONNECTION_STATE_PAIRING);
            mPreparingDevice.setValue(device);

            re_pairing = false;

            if (isBondedDevice(device.getAddress())) {
                connectDevice(device);
            } else {
                // 连接经典
                ThreadUtils.postOnBackgroundThread(device::createBond);
            }
        });
    }

    private void retryConnectDevice() {
        ABDevice preparingDevice = mPreparingDevice.getValue();
        if (preparingDevice != null) {
            preparingDevice.connect();
        }
    }

    private void onBondedDevice(BluetoothDevice device) {
        ABDevice preparingDevice = mPreparingDevice.getValue();
        if (preparingDevice != null && isPreparingDevice(device)) {
            // 如果之前没有连接，则现在连接
            if (!re_pairing) {
                connectDevice(preparingDevice);
            }
        }
    }

    private void connectDevice(ABDevice device) {
        device.setConnectionStateCallback(this);
        device.setDataDelegate(this);
        retryCount = 0;
        deviceConnectionState.postValue(DEVICE_CONNECTION_STATE_CONNECTING);
        ThreadUtils.postOnBackgroundThread(device::connect);
    }

    public void disconnect() {
        disconnect(true);
    }

    private void disconnect(boolean fromUser) {
        retryConnectDeviceHandler.removeCallbacks(retryConnectDeviceRunnable);

        // 释放掉正在认证的设备
        ABDevice preparingDevice = mPreparingDevice.getValue();
        if (preparingDevice != null) {
            preparingDevice.setConnectionStateCallback(null);
            preparingDevice.setDataDelegate(null);
            preparingDevice.release();
            mTempBlocklist.remove(preparingDevice.getAddress());
            ThreadUtils.postOnMainThread(() -> mPreparingDevice.setValue(null));
        }

        // 释放当前活跃设备
        ABDevice device = mActiveDevice.getValue();
        if (device != null) {
            deviceAddressDisconnectedByUser = null;
            if (fromUser) {
                deviceAddressDisconnectedByUser = device.getAddress();
            }
            device.setConnectionStateCallback(null);
            device.setDataDelegate(null);
            device.release();
            mTempBlocklist.remove(device.getAddress());
            ThreadUtils.postOnMainThread(() -> mActiveDevice.postValue(null));
        }

        mDeviceCommManager.setCommDelegate(null);
        mDeviceCommManager.setResponseErrorHandler(null);
        mDeviceCommManager.reset();
        // 重置设备状态LiveData
        mDeviceCommManager.resetDeviceStatus();
        deviceMaxPacketSize.postValue(null);

        // 更新设备连接状态
        deviceConnectionState.postValue(DEVICE_CONNECTION_STATE_IDLE);

      //  startScanIfMeetConditions();
    }

    private void startScanIfMeetConditions() {
        // Launch scanning if App is in the foreground and Quick Connect is enabled
        if (ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            if (Utils.getPrefsEnableQuickConnect(mContext)) {
                startScan();
            }
        }
    }

    /**
     * 监控蓝牙开关状态
     */
    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);
                int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

                Timber.i("Bluetooth State: %s -> %s", previousState, currentState);

                if (previousState == BluetoothAdapter.STATE_TURNING_ON
                        && currentState == BluetoothAdapter.STATE_ON) { // 开启蓝牙
                    // 蓝牙开启后，重新获取已配对设备
                    updateBondedDevices();
                    // 如果符合条件，则开始扫描
                    startScanIfMeetConditions();
                } else if (previousState == BluetoothAdapter.STATE_TURNING_OFF
                        && currentState == BluetoothAdapter.STATE_OFF) { // 关闭蓝牙
                    if (mIsScanning) {
                        stopScan();
                    }
                    // 清掉已配对设备
                    mBondedDevices = null;
                    // 处理设备断开
                    disconnect(false);
                }
            }
        }
    };

    /**
     * 注册蓝牙广播接收器
     */
    public synchronized void registerBroadcastReceivers() {
        if (mBroadcastReceiverRegisterCount == 0) {
            mContext.registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            mContext.registerReceiver(mBluetoothBondStateBroadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            mContext.registerReceiver(mBluetoothHeadsetStateBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
        }
        mBroadcastReceiverRegisterCount++;
    }

    /**
     * 注销蓝牙广播接收器
     */
    public synchronized void unregisterBroadcastReceivers() {
        mBroadcastReceiverRegisterCount--;
        if (mBroadcastReceiverRegisterCount == 0) {
            mContext.unregisterReceiver(mBluetoothStateBroadcastReceiver);
            mContext.unregisterReceiver(mBluetoothBondStateBroadcastReceiver);
            mContext.unregisterReceiver(mBluetoothHeadsetStateBroadcastReceiver);
        }
    }

    /**
     * 监控蓝牙配对状态
     */
    private final BroadcastReceiver mBluetoothBondStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                //更新已配对设备
                updateBondedDevices();

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && isPreparingDevice(device)) {
                    int previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE);
                    int currentState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);

                    Timber.i("Device(%s) Bond Status: %s -> %s", device.getAddress(), previousState, currentState);

                    if (previousState == BluetoothDevice.BOND_NONE && currentState == BluetoothDevice.BOND_BONDING) {
                        // 开始配对
                    } else if (previousState == BluetoothDevice.BOND_BONDING && currentState == BluetoothDevice.BOND_NONE) {
                        // 未配对
                        deviceConnectionState.setValue(DEVICE_CONNECTION_STATE_IDLE);
                        mPreparingDevice.setValue(null);
                        mPopupDevice.setValue(null);
                        mTempBlocklist.remove(device.getAddress());
                        // 如果符合条件，则开始扫描
//                        startScanIfMeetConditions();
                    } else if (previousState == BluetoothDevice.BOND_BONDED && currentState == BluetoothDevice.BOND_BONDING) {
                        // 重新配对
                        deviceConnectionState.setValue(DEVICE_CONNECTION_STATE_PAIRING);
                        re_pairing = true;
                    } else if (previousState == BluetoothDevice.BOND_BONDING && currentState == BluetoothDevice.BOND_BONDED) {
                        // 完成配对
                        deviceConnectionState.setValue(DEVICE_CONNECTION_STATE_PAIRED);
                        onBondedDevice(device);
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mBluetoothHeadsetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    int previousState = intent.getIntExtra(BluetoothHeadset.EXTRA_PREVIOUS_STATE, BluetoothHeadset.STATE_DISCONNECTED);
                    int currentState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);

                    Timber.i("Headset(%s) connection Status: %s -> %s", device.getAddress(), previousState, currentState);

                    if (previousState == BluetoothHeadset.STATE_CONNECTING && currentState == BluetoothHeadset.STATE_CONNECTED) {
                        // Headset connected
                    } else if (previousState == BluetoothHeadset.STATE_DISCONNECTING && currentState == BluetoothHeadset.STATE_DISCONNECTED) {
                        // Headset disconnected
                        if (isActiveDevice(device)) {
                            // Disconnect activeDevice
                            disconnect(false);
                        }
                    }
                }
            }
        }
    };

    private final BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = (BluetoothHeadset) proxy;
            }
        }
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEADSET) {
                mBluetoothHeadset = null;
            }
        }
    };

    // 不能使用匿名类，不然会被GC回收
    private final SharedPreferences.OnSharedPreferenceChangeListener
            onSharedPreferenceChangeListener = (sharedPreferences, key) -> {
        if (Utils.PREFS_ENABLE_QUICK_CONNECT.equals(key)) {
            if (sharedPreferences.getBoolean(Utils.PREFS_ENABLE_QUICK_CONNECT, false)) {
                deviceAddressDisconnectedByUser = null;
            }
        }
    };

    /* PayloadHandler */

    private void registerMaxPacketSizeCallable() {
        mDeviceCommManager.registerDeviceInfoCallback(Command.INFO_MAX_PACKET_SIZE, MtuPayloadHandler.class, mtuSize -> {
            Timber.d("Max Packet Size: %s", mtuSize);
            if (mtuSize != null) {
                // No longer deal with Command.INFO_MAX_PACKET_SIZE
                mDeviceCommManager.unregisterDeviceInfoCallback(Command.INFO_MAX_PACKET_SIZE);
                // Update LiveData
                deviceMaxPacketSize.setValue(mtuSize.shortValue());
                // Set max communication packet size
                mDeviceCommManager.setMaxPacketSize(mtuSize);
                // 开始认证，如果设备不需要认证，会直接返回认证成功
                ABDevice preparingDevice = mPreparingDevice.getValue();
                if (preparingDevice != null) {
                    deviceConnectionState.postValue(DEVICE_CONNECTION_STATE_AUTHENTICATING);
                    preparingDevice.startAuth();
                }
            }
        });
    }

    /* ConnectionStateCallback */

    @Override
    public void onConnected(@NonNull BluetoothDevice device) {
        ABDevice preparingDevice = mPreparingDevice.getValue();
        if (preparingDevice != null && isPreparingDevice(device)) {
            mDeviceCommManager.setCommDelegate(preparingDevice);
            mDeviceCommManager.setResponseErrorHandler(this);

            deviceConnectionState.postValue(DEVICE_CONNECTION_STATE_CONNECTED);

            // 先获取MTU，以便设置分包大小，然后再获取其他信息
            // First of all, require MTU, in order to set max packet size, and then require other info
            registerMaxPacketSizeCallable();
            mDeviceCommManager.sendRequest(new DeviceInfoRequest(Command.INFO_MAX_PACKET_SIZE));
        }
    }

    @Override
    public void onReceiveAuthResult(ABDevice device, boolean passed) {
        if (passed) {
            // 数据库中添加设备
//            mDeviceManagerApi.insertBtDevice(device);

            // 使用主线程修改LiveData
            ThreadUtils.postOnMainThread(() -> {
                // Notify UI
                mActiveDevice.setValue(device);
                deviceConnectionState.postValue(DEVICE_CONNECTION_STATE_AUTHENTICATED);
                // Require all device info
                mDeviceCommManager.sendRequest(DeviceInfoRequest.defaultInfoRequest());
            });
        } else {
            // 释放btDevice
            device.release();
            deviceConnectionState.postValue(DEVICE_CONNECTION_STATE_IDLE);
        }
        // 已经不处于认证状态
        ThreadUtils.postOnMainThread(() -> mPreparingDevice.setValue(null));
    }

    @Override
    public void onDisconnected() {
        Timber.v("Spp callback Disconnected");

        ABDevice preparingDevice = mPreparingDevice.getValue();
        if (preparingDevice != null) {
            // 重试
            if(retryCount != MAX_RETRY_COUNT) {
                retryCount++;
                Timber.w("Retry count = %s", retryCount);
                retryConnectDeviceHandler.postDelayed(retryConnectDeviceRunnable, RETRY_CONNECT_DEVICE_INTERVAL);
                return;
            }
        }

        disconnect(false);
    }

    /* DataDelegate */

    @Override
    public void onReceiveData(byte[] data) {
        Timber.v("Spp callback Receive: %s", ParserUtils.bytesToHex(data, false));
        mDeviceCommManager.handleData(data);
    }

    @Override
    public void onWriteData(byte[] data) {
        Timber.v("Spp callback Write: %s", ParserUtils.bytesToHex(data, false));
    }

    public static String getConnectionStateName(int connectionState) {
        switch (connectionState) {
            case DEVICE_CONNECTION_STATE_IDLE:
                return "IDLE";
            case DEVICE_CONNECTION_STATE_PAIRING:
                return "PAIRING";
            case DEVICE_CONNECTION_STATE_PAIRED:
                return "PAIRED";
            case DEVICE_CONNECTION_STATE_CONNECTING:
                return "CONNECTING";
            case DEVICE_CONNECTION_STATE_CONNECTED:
                return "CONNECTED";
            case DEVICE_CONNECTION_STATE_AUTHENTICATING:
                return "AUTHENTICATING";
            case DEVICE_CONNECTION_STATE_AUTHENTICATED:
                return "AUTHENTICATED";
            default:
                return "UNKNOWN";
        }
    }

    /* 用于App后台扫描 */

    public void clearPopupDevice() {
        Timber.d("clearPopupDevice");
        mPopupDeviceHandler.removeCallbacks(mClearPopupDeviceRunnable);

        ABDevice popupDevice = mPopupDevice.getValue();
        if (popupDevice != null) {
            mTimeFirstFound.remove(popupDevice.getAddress());
        }

        mPopupDevice.postValue(null);
    }

    public void addDeviceToBlocklist(String deviceAddress, long blockTime) {
        if (blockTime == BLOCK_TIME_TEMP) {
          //  mTempBlocklist.add(deviceAddress);
        } else if (blockTime == BLOCK_TIME_FOREVER) {
            mDeviceManagerApi.insertBlockRecord(deviceAddress, blockTime);
        } else {
            long blockUntilTime = Calendar.getInstance().getTimeInMillis() + blockTime;
            mDeviceManagerApi.insertBlockRecord(deviceAddress, blockUntilTime);
        }
    }

    private void updateTimestamp(final String deviceAddress, final Date time) {
        Date lastSeenTime = mTimeLastSeen.get(deviceAddress);
        if (lastSeenTime != null) {
            // 是否从第一次发现时间列表中移除
            Date lastSeenTimeAfterDelay = new Date(lastSeenTime.getTime() + PREPARE_DEVICE_BEACON_INTERVAL);
            // 超过间隔
            if (time.after(lastSeenTimeAfterDelay)) {
                // 从发现时间中移除
                mTimeFirstFound.remove(deviceAddress);
                Timber.v("mTimeFirstFound.remove(%s)", deviceAddress);
            }

            // 是否从临时弹窗屏蔽列表中移除
            Date lastSeenTimeAfterTimeout = new Date(lastSeenTime.getTime() + PREPARE_DEVICE_BLOCK_TIMEOUT);
            // 超过间隔
            if (time.after(lastSeenTimeAfterTimeout)) {
                // 从Blocklist中移除
                mTempBlocklist.remove(deviceAddress);
                Timber.v("mTempBlocklist.remove(%s)", deviceAddress);
            }
        }
        // 添加到最后发现时间
        mTimeLastSeen.put(deviceAddress, time);
    }

    private boolean isDeviceStillBlocked(String deviceAddress) {
        // 临时屏蔽列表查询
        if (mTempBlocklist.contains(deviceAddress)) {
            return true;
        }

        // 数据库查询
        long blockTime = mDeviceManagerApi.getBlockTime(deviceAddress);
        if (blockTime == -1) {
            return true;
        } else if (blockTime > 0) {
            long now = Calendar.getInstance().getTimeInMillis();
            if (now > blockTime) {
                mDeviceManagerApi.deleteBlockRecord(deviceAddress);
            } else {
//                Timber.v("now: %s < blockTime: %s", now, blockTime);
                return true;
            }
        }

        return false;
    }

    private boolean iSeeYouBefore(String deviceAddress) {
        return mTimeFirstFound.get(deviceAddress) != null;
    }

    private boolean iKnowYou(String deviceAddress) {
        Date now = Calendar.getInstance().getTime();
        Date firstFoundTime = mTimeFirstFound.get(deviceAddress);

        if (firstFoundTime != null) {
            Date firstFoundTimeAfterDelay = new Date(firstFoundTime.getTime() + PREPARE_DEVICE_DELAY_TIME);
            // 如果发现设备超过一段时间，处理之
            return now.after(firstFoundTimeAfterDelay);
        }
        return false;
    }

    private void handleEarbudsFound(@NonNull ABEarbuds device) {
        final String deviceAddress = device.getAddress();
      //  Log.d("handleEarbudsFound",deviceAddress);
        // Check if it's the device disconnected by user last time
//        if (deviceAddress.equals(deviceAddressDisconnectedByUser)) {
//            return;
//        }

//        if (headsetIsConnected(device.getDevice())) {
//            // 停止扫描
//            stopScan();
//            // Connect to device is active
//            bondDevice(device);
//            // 处理之后，屏蔽此设备，相当于只取第一次检测到已连接的情况
//           // mTempBlocklist.add(device.getAddress());
//            return;
//        }

        // If device indicates it's connected
//        if (device.isConnected()) {
//            Timber.w("handleEarbudsFound: Can't be here");
//            return;
//        }

        Date now = Calendar.getInstance().getTime();
        // 更新扫描到的设备的时间戳
        updateTimestamp(deviceAddress, now);

        // Check if device is still blocked
//        if (isDeviceStillBlocked(deviceAddress)) {
//            return;
//        }

        // 是否当前正在弹窗的设备
        if (isPopupDevice(device.getDevice())) {
            // Refresh clear popup timer
            mPopupDeviceHandler.removeCallbacks(mClearPopupDeviceRunnable);
            mPopupDeviceHandler.postDelayed(mClearPopupDeviceRunnable, PREPARE_DEVICE_TIMEOUT); // 记得处理完了清除
        }
        // If no popup device now
        else if (mPopupDevice.getValue() == null) {
            if (iSeeYouBefore(deviceAddress)) {
                // 如果发现设备超过一段时间，处理之
                if (iKnowYou(deviceAddress)) {
                    // 准备处理，通知应用层弹窗什么的
//                    if (isBondedDevice(deviceAddress)) {
//                        // Bonded but disconnected, FIXME: Popup?
//                    }
//                    else
                    {
                        // Un-bonded
                        // 弹窗
                        mPopupDevice.setValue(device);
                        // 通知处理后，清除发现时间
                        mTimeFirstFound.clear();
                        // 如果没有持续广播就清除
                        mPopupDeviceHandler.postDelayed(mClearPopupDeviceRunnable, PREPARE_DEVICE_TIMEOUT); // 记得处理完了清除
                    }
                }
            }
            // 第一次发现
            else {
                mTimeFirstFound.put(deviceAddress, now);
            }
        }
    }

    private void handleProduct(final ScanResult result, final DeviceBeacon deviceBeacon) {
        // TODO: 定义产品ID
       // if (deviceBeacon.getProductId() == 1) {
            ABEarbuds device = new ABEarbuds(result, (EarbudsBeacon) deviceBeacon);
            handleEarbudsFound(device);
      //  }
    }

    private void onResult(final ScanResult result, final DeviceBeacon deviceBeacon) {
        handleProduct(result, deviceBeacon);
    }

    private int getMinRssi() {
        return -61;
    }

    private final ScanCallback mScanCallbacks = new ScanCallback() {


        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            // Ignore device with weak signal

            if (result.getRssi() <= getMinRssi()) {
                return;
            }

//            Timber.v("scan result = %s", result);
            try {
                final ScanRecord scanRecord = result.getScanRecord();
                // 过滤beacon，符合条件的才会添加到列表
                if (scanRecord != null && scanRecord.getBytes() != null) {
                    // broadcast packet filtering device
                 //   Log.d("onScanResult 1",result.toString());
                    byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(ABEarbuds.MANUFACTURER_ID);
                    if (DeviceBeacon.isDeviceBeacon(manufacturerSpecificData)) {
                        DeviceBeacon deviceBeacon = DeviceBeacon.getDeviceBeacon(manufacturerSpecificData);
                       // Log.d("onScanResult 2",result.toString());
                        if (deviceBeacon != null && (deviceBeacon.getAgentId() == BuildConfig.BRAND_ID >> 16)) {
                           // Log.d("onScanResult 3",result.toString());// filter proxy products
                            onResult(result, deviceBeacon);
                        }
                    }
                }
            } catch (Exception ex) {
                Timber.e("Error: %s", ex.getMessage());
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            // Batch scan is disabled (report delay = 0)
        }

        @Override
        public void onScanFailed(final int errorCode) {
            mIsScanning = false;
            mScanningState.postValue(false);
        }
    };

    public void startScan() {
        if (!mAdapter.isEnabled())
            return;
        Log.d("mAdapter","true");
//        if (!Utils.isBluetoothNeededPermissionGranted(mContext))
//            return;
//        Log.d("BluetoothPermission","true");
        if (mIsScanning)
            return;
        Log.d("mIsScanning","true");
        mIsScanning = true;
        mScanningState.postValue(true);
        BleScanManager.startScan(null, mScanCallbacks);
        Log.d("Scan started","true");
    }

    public void stopScan() {
        if (!mIsScanning)
            return;

        BleScanManager.stopScan(mScanCallbacks);
        mIsScanning = false;
        mScanningState.postValue(false);
        Timber.v("Scan stopped");
    }

    /* 设备状态 */

    public LiveData<DevicePower> getDevicePower() {
        return mDeviceCommManager.getDevicePower();
    }

    public LiveData<Integer> getDeviceFirmwareVersion() {
        return mDeviceCommManager.getDeviceFirmwareVersion();
    }

    public LiveData<String> getDeviceName() {
        return mDeviceCommManager.getDeviceName();
    }

    public LiveData<RemoteEqSetting> getDeviceEqSetting() {
        return mDeviceCommManager.getDeviceEqSetting();
    }

    public LiveData<Map<Integer, Integer>> getDeviceKeySettings() {
        return mDeviceCommManager.getDeviceKeySettings();
    }

    public LiveData<Byte> getDeviceVolume() {
        return mDeviceCommManager.getDeviceVolume();
    }

    public LiveData<Boolean> getDevicePlayState() {
        return mDeviceCommManager.getDevicePlayState();
    }

    public LiveData<Byte> getDeviceWorkMode() {
        return mDeviceCommManager.getDeviceWorkMode();
    }

    public LiveData<Boolean> getDeviceInEarStatus() {
        return mDeviceCommManager.getDeviceInEarStatus();
    }

    public LiveData<Byte> getDeviceLanguageSetting() {
        return mDeviceCommManager.getDeviceLanguageSetting();
    }

    public LiveData<Boolean> getDeviceAutoAnswer() {
        return mDeviceCommManager.getDeviceAutoAnswer();
    }

    public LiveData<Byte> getDeviceAncMode() {
        return mDeviceCommManager.getDeviceAncMode();
    }

    public LiveData<Boolean> getDeviceIsTws() {
        return mDeviceCommManager.getDeviceIsTws();
    }

    public LiveData<Boolean> getDeviceTwsConnected() {
        return mDeviceCommManager.getDeviceTwsConnected();
    }

    public LiveData<Boolean> getDeviceLedSwitch() {
        return mDeviceCommManager.getDeviceLedSwitch();
    }

    public LiveData<byte[]> getDeviceFwChecksum() {
        return mDeviceCommManager.getDeviceFwChecksum();
    }

    public LiveData<Integer> getDeviceAncGain() {
        return mDeviceCommManager.getDeviceAncGain();
    }

    public LiveData<Integer> getDeviceTransparencyGain() {
        return mDeviceCommManager.getDeviceTransparencyGain();
    }

    public LiveData<Integer> getDeviceAncGainNum() {
        return mDeviceCommManager.getDeviceAncGainNum();
    }

    public LiveData<Integer> getDeviceTransparencyGainNum() {
        return mDeviceCommManager.getDeviceTransparencyGainNum();
    }

    public LiveData<List<RemoteEqSetting>> getDeviceRemoteEqSettings() {
        return mDeviceCommManager.getDeviceRemoteEqSettings();
    }

    public LiveData<Boolean> getDeviceLeftIsMainSide() {
        return mDeviceCommManager.getDeviceLeftIsMainSide();
    }

    public LiveData<Integer> getDeviceProductColor() {
        return mDeviceCommManager.getDeviceProductColor();
    }

    public LiveData<Boolean> getDeviceSoundEffect3d() {
        return mDeviceCommManager.getDeviceSoundEffect3d();
    }

    public LiveData<Integer> getDeviceCapacities() {
        return mDeviceCommManager.getDeviceCapacities();
    }

    public LiveData<Short> getDeviceMaxPacketSize() {
        return deviceMaxPacketSize;
    }

    @Override
    public void onError(int errorCode) {
        Timber.d("Device response error, errorCode = %s", errorCode);
    }

    /* 数据库操作 */

//    /**
//     * 获取所有设备，LiveData
//     * @return 所有设备的LiveData
//     */
//    public LiveData<List<BaseDevice>> getBaseDevices() {
//        return mDeviceManagerApi.getBaseDevices();
//    }
//
//    /**
//     * 从数据库中删除蓝牙设备
//     * @param device 设备类 {@link Device}
//     */
//    public void deleteBtDevice(@NonNull Device device) {
//        mDeviceManagerApi.deleteBtDevice(device);
//    }
}
