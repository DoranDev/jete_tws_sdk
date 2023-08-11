package com.bluetrum.abmate.viewmodels;

import androidx.lifecycle.LiveData;

public class ScannerStateLiveData extends LiveData<ScannerStateLiveData> {
    private boolean mScanningStarted;
    private boolean mBluetoothEnabled;
    private boolean mLocationEnabled;
    private boolean mDeviceFound;

    ScannerStateLiveData(final boolean bluetoothEnabled, final boolean locationEnabled) {
        mScanningStarted = false;
        mDeviceFound = false;
        mBluetoothEnabled = bluetoothEnabled;
        mLocationEnabled = locationEnabled;
        postValue(this);
    }

    public void startScanning() {
        postValue(this);
    }

    void scanningStarted() {
        mScanningStarted = true;
    }

    void scanningStopped() {
        mScanningStarted = false;
        mDeviceFound = false;
        postValue(this);
    }

    void bluetoothEnabled() {
        mBluetoothEnabled = true;
        postValue(this);
    }

    void bluetoothDisabled() {
        mBluetoothEnabled = false;
        postValue(this);
    }

    void deviceFound() {
        if (!mDeviceFound) {
            mDeviceFound = true;
            postValue(this);
        }
    }

    public boolean isEmpty() {
        return !mDeviceFound;
    }

    /**
     * Returns whether scanning is in progress.
     */
    public boolean isScanning() {
        return mScanningStarted;
    }

    /**
     * Returns whether Bluetooth adapter is enabled.
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothEnabled;
    }

    /**
     * Returns whether Location is enabled.
     */
    public boolean isLocationEnabled() {
        return mLocationEnabled;
    }

    void setLocationEnabled(final boolean enabled) {
        mLocationEnabled = enabled;
        postValue(this);
    }
}
