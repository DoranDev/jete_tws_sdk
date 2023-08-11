package com.bluetrum.devicemanager.models;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.DeviceCommManager;

public abstract class ABDevice implements DeviceCommManager.DeviceCommDelegate {

    private int rssi;

    private int featureVersion;

    public ABDevice(DeviceBeacon deviceBeacon) {
        this.featureVersion = deviceBeacon.getBeaconVersion();
    }

    public abstract void updateDeviceStatus(@NonNull DeviceBeacon deviceBeacon);

    public abstract int getProductId();

    public abstract BluetoothDevice getBleDevice();

    public abstract String getBleName();

    public abstract String getBleAddress();

    public BluetoothDevice getDevice() {
        return getBleDevice();
    }

    public String getAddress() {
        return getBleAddress();
    }

    public String getName() {
        return getBleName();
    }

    /* Connection */

    public abstract void createBond();

    public abstract void connect();

    public abstract void startAuth();

    public abstract boolean send(@NonNull byte[] data);

    public abstract void release();

    /* Getter & Setter */

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getFeatureVersion() {
        return featureVersion;
    }

    /* Compare */

    public boolean matches(final String bleAddress) {
        return getBleAddress().equals(bleAddress);
    }

    public boolean matches(final BluetoothDevice device) {
        return getBleAddress().equals(device.getAddress());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof ABDevice) {
            final ABDevice that = (ABDevice) o;
            return getBleAddress().equals(that.getBleAddress());
        }
        return super.equals(o);
    }

    /* 接口 */

    public abstract void setConnectionStateCallback(ConnectionStateCallback callback);

    public abstract void setDataDelegate(DataDelegate delegate);

    public interface ConnectionStateCallback {
        void onConnected(BluetoothDevice device);
        void onReceiveAuthResult(ABDevice device, boolean passed);
        void onDisconnected();
    }

    public interface DataDelegate {
        void onReceiveData(byte[] data);
        void onWriteData(byte[] data);
    }

}
