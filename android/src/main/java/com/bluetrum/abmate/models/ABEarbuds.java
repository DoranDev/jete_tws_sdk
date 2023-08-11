package com.bluetrum.abmate.models;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;

import com.bluetrum.abmate.BuildConfig;
import com.bluetrum.abmate.auth.Authenticator;
import com.bluetrum.abmate.auth.NullAuthenticator;
import com.bluetrum.devicemanager.bluetooth.BluetoothSppService;
import com.bluetrum.devicemanager.models.ABDevice;
import com.bluetrum.devicemanager.models.DeviceBeacon;
import com.bluetrum.devicemanager.models.DeviceBeaconV1;
import com.bluetrum.devicemanager.models.EarbudsBeacon;

import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

import no.nordicsemi.android.support.v18.scanner.ScanResult;
import timber.log.Timber;

@SuppressLint("MissingPermission")
public class ABEarbuds extends ABDevice {

    private static final String TAG = ABEarbuds.class.getSimpleName();

    // Broadcast service UUID
    public final static UUID BROADCAST_SERVICE_UUID = UUID.fromString(BuildConfig.BROADCAST_SERVICE_UUID);

    // Manufacturer Specific Data
    public final static int MANUFACTURER_ID = BuildConfig.MANUFACTURER_ID;

    private static final UUID DEFAULT_SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID CUSTOM_SPP_UUID  = UUID.fromString(BuildConfig.CUSTOM_SPP_UUID);

    private static final String SPP_THREAD_NAME = "SPP_THREAD";
    private static final int DEFAULT_SPP_SEND_INTERVAL = 15; // 15 for safety, smaller for faster
    private int sppSendInterval = DEFAULT_SPP_SEND_INTERVAL;

    private DeviceBeacon deviceBeacon;

    // 这里是扫描到的BLE设备，不是经典设备
    private BluetoothDevice mBleDevice;
    // 根据扫描到的Beacon创建的经典蓝牙设备
    private BluetoothDevice mBtDevice;

    private boolean isConnected;
    private boolean needAuth;
    private boolean useCustomSppUuid;
    private boolean mLeftCharging;
    private int mLeftBatteryLevel;
    private boolean mRightCharging;
    private int mRightBatteryLevel;
    private boolean mCaseCharging;
    private int mCaseBatteryLevel;

    private BluetoothSppService sppService;
    private Handler sppHandler;

    private Authenticator authenticator;

    private final Deque<byte[]> requestDataQueue = new LinkedBlockingDeque<>();
    private Handler delaySendHandler;
    private Runnable delaySendRunnable;

    private ConnectionStateCallback connectionStateCallback;
    private DataDelegate dataDelegate;

    public void setConnectionStateCallback(ConnectionStateCallback callback) {
        this.connectionStateCallback = callback;
    }

    public void setDataDelegate(DataDelegate delegate) {
        this.dataDelegate = delegate;
    }

    private Handler getDelaySendHandler() {
        // 延迟初始化
        if (delaySendHandler == null) {
            synchronized (this) {
                if (delaySendHandler == null) {
                    // 延迟发送handler
                    HandlerThread delaySendThread = new HandlerThread("DelaySendThread");
                    delaySendThread.start();
                    delaySendHandler = new Handler(delaySendThread.getLooper());
                }
            }
        }
        return delaySendHandler;
    }

    public ABEarbuds(ScanResult result, EarbudsBeacon deviceBeacon) {
        super(deviceBeacon);

        this.mBleDevice = result.getDevice();
        this.deviceBeacon = deviceBeacon;
        this.isConnected = deviceBeacon.isConnected();
        this.needAuth = deviceBeacon.needAuth();
        this.useCustomSppUuid = deviceBeacon.useCustomSppUuid();
        // Only version 1 has battery information
        if (deviceBeacon.getBeaconVersion() == 1) {
            DeviceBeaconV1 beacon = (DeviceBeaconV1) deviceBeacon;
            // Update battery information
            this.mLeftBatteryLevel = beacon.getLeftBatteryLevel();
            this.mRightBatteryLevel = beacon.getRightBatteryLevel();
            this.mCaseBatteryLevel = beacon.getCaseBatteryLevel();
            this.mLeftCharging = beacon.isLeftChanging();
            this.mRightCharging = beacon.isRightChanging();
            this.mCaseCharging = beacon.isCaseCharging();
        }
        // 经典蓝牙设备
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBtDevice = bluetoothAdapter.getRemoteDevice(deviceBeacon.getBtAddress()); // 如果系统没有缓存，是获取不到名称的
        // 验证器
//        this.authenticator = new ABAuthenticator(this.mClassicAddress);
        this.authenticator = new NullAuthenticator();
    }

    @Override
    public void updateDeviceStatus(@NonNull DeviceBeacon deviceBeacon) {
        this.deviceBeacon = deviceBeacon;
        // Only version 1 has battery information
        if (deviceBeacon.getBeaconVersion() == 1) {
            DeviceBeaconV1 beacon = (DeviceBeaconV1) deviceBeacon;
            this.isConnected = beacon.isConnected();
            this.needAuth = beacon.needAuth();
            this.useCustomSppUuid = beacon.useCustomSppUuid();
            // Update battery information
            this.mLeftBatteryLevel = beacon.getLeftBatteryLevel();
            this.mRightBatteryLevel = beacon.getRightBatteryLevel();
            this.mCaseBatteryLevel = beacon.getCaseBatteryLevel();
            this.mLeftCharging = beacon.isLeftChanging();
            this.mRightCharging = beacon.isRightChanging();
            this.mCaseCharging = beacon.isCaseCharging();
        }
    }

    @Override
    public int getProductId() {
        return deviceBeacon.getProductId();
    }

    @Override
    public BluetoothDevice getBleDevice() {
        return mBleDevice;
    }

    @Override
    public String getBleName() {
        return mBleDevice.getName();
    }

    @Override
    @NonNull
    public String getBleAddress() {
        return mBleDevice.getAddress();
    }

    public BluetoothDevice getBtDevice() {
        return mBtDevice;
    }

    public String getBtName() {
        return mBtDevice.getName();
    }

    @NonNull
    public String getBtAddress() {
        return mBtDevice.getAddress();
    }

    @Override
    public BluetoothDevice getDevice() {
        return getBtDevice();
    }

    @Override
    public String getAddress() {
        return getBtAddress();
    }

    @Override
    public String getName() {
        return getBtName();
    }

    /**
     * 从广播包获取设备是否已经连接的状态
     * @return 是否已经连接
     */
    public boolean isConnected() {
        return isConnected;
    }

    public int getLeftBatteryLevel() {
        return mLeftBatteryLevel;
    }

    public int getRightBatteryLevel() {
        return mRightBatteryLevel;
    }

    public int getCaseBatteryLevel() {
        return mCaseBatteryLevel;
    }

    public boolean isLeftChanging() {
        return mLeftCharging;
    }

    public boolean isRightChanging() {
        return mRightCharging;
    }

    public boolean isCaseCharging() {
        return mCaseCharging;
    }

    public int getSppSendInterval() {
        return sppSendInterval;
    }

    public void setSppSendInterval(int interval) {
        sppSendInterval = interval;
    }

    @Override
    public boolean matches(final BluetoothDevice device) {
        return getBtAddress().equals(device.getAddress());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ABEarbuds) {
            final ABEarbuds that = (ABEarbuds) o;
            return getBtAddress().equals(that.getBtAddress());
        }
        return super.equals(o);
    }

    /* Public methods */

    @Override
    public void createBond() {
        // 创建经典蓝牙设备，不能使用成员的BLE设备，地址不同
        mBtDevice.createBond();
    }

    public void connect() {
        // SPP线程
        startSppThread();
        // SPP服务
        UUID uuid = useCustomSppUuid ? CUSTOM_SPP_UUID : DEFAULT_SPP_UUID;
        sppService = new BluetoothSppService(sppHandler);
        sppService.connect(mBtDevice, uuid);
    }

    /**
     * 连接SPP，并开始认证。
     * 如果不需要认证，则直接返回认证成功。
     * Connect to SPP, and start authentication.
     * If no need to authenticate, return passed directly.
     */
    @Override
    public void startAuth() {
        if (!needAuth) {
            if (connectionStateCallback != null) {
                connectionStateCallback.onReceiveAuthResult(this, true);
            }
        } else {
            final byte[] authData = authenticator.getAuthData();
            if (authData != null) {
                sppSend(authData);
            } else {
                // Can't reach here
            }
        }
    }

    @Override
    public boolean send(@NonNull byte[] data) {
        if (checkIfAuthenticated()) {
            sppSend(data);
            // Post next delay send task
            postDelaySend();
            return true;
        }
        return false;
    }

    public synchronized void sendRequestData(byte[] requestData) {
        requestDataQueue.add(requestData);
        // Send next fragment immediately if there isn't delay send task
        if (delaySendRunnable == null) {
            nextSend();
        }
    }

    @Override
    public void release() {
        Timber.v("release");
        authenticator.reset();
        if (delaySendRunnable != null) {
            getDelaySendHandler().removeCallbacks(delaySendRunnable);
        }
        requestDataQueue.clear();
        if (sppService != null) {
            sppService.stop();
            sppService = null;
        }
    }

    /* Private */

    private boolean checkIfAuthenticated() {
        return sppService != null
                && sppService.getState() == BluetoothSppService.STATE_CONNECTED
                && authenticator.isAuthPassed();
    }

    private void nextSend() {
        byte[] requestData = requestDataQueue.pollFirst();
        if (requestData != null) {
            send(requestData);
        }
    }

    private void postDelaySend() {
        if (delaySendRunnable == null || !HandlerCompat.hasCallbacks(getDelaySendHandler(), delaySendRunnable)) {
            delaySendRunnable = () -> {
                delaySendRunnable = null;
                nextSend();
            };
            getDelaySendHandler().postDelayed(delaySendRunnable, sppSendInterval);
        }
    }

    private final Handler.Callback sppCallback = msg -> {
        switch (msg.what) {
            // 状态
            case BluetoothSppService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                    case BluetoothSppService.STATE_CONNECTING:
                        break;
                    case BluetoothSppService.STATE_CONNECTED:
                        if (connectionStateCallback != null) {
                            connectionStateCallback.onConnected(mBtDevice);
                        }
                        break;
                    case BluetoothSppService.STATE_NONE:
                        // 这里当做停止
                        if (connectionStateCallback != null) {
                            connectionStateCallback.onDisconnected();
                        }
                        break;
                }
                break;
            // 读取到数据
            case BluetoothSppService.MESSAGE_READ:
                // 处理接收到的数据
                byte[] readData = (byte[]) msg.obj;
                handleReadData(readData);
                break;
            case BluetoothSppService.MESSAGE_WRITE:
                // 发送完成后调用
                if (checkIfAuthenticated()) {
                    if (connectionStateCallback != null) {
                        byte[] writeData = (byte[]) msg.obj;
                        dataDelegate.onWriteData(writeData);
                    }
                }
                break;
        }
        return true;
    };

    // 处理接收到的数据
    private void handleReadData(final byte[] readData) {
        if (checkIfAuthenticated()) {
            if (dataDelegate != null) {
                dataDelegate.onReceiveData(readData);
            }
        } else {
            boolean res = authenticator.handleAuthData(readData);
            if (!res) {
                connectionStateCallback.onReceiveAuthResult(this, false);
            } else if (!checkIfAuthenticated()) {
                // NEXT
                final byte[] authData = authenticator.getAuthData();
                if (authData != null) {
                    sppSend(authData);
                } else {
                    // Can't reach here
                }
            }
        }
    }

    private void sppSend(@NonNull byte[] data) {
        sppService.write(data);
    }

    /**
     * 开启SPP通信线程
     */
    private void startSppThread() {
        HandlerThread thread = new HandlerThread(SPP_THREAD_NAME);
        thread.start();
        sppHandler = new Handler(thread.getLooper(), sppCallback);
    }

}
