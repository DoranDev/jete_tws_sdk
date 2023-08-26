package com.bluetrum.abmate.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.bluetrum.abmate.models.ABEarbuds;
import com.bluetrum.devicemanager.models.ABDevice;
import com.bluetrum.devicemanager.models.DeviceBeacon;
import com.bluetrum.devicemanager.models.EarbudsBeacon;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanResult;
import timber.log.Timber;

public class ScannerLiveData extends LiveData<ScannerLiveData> {
    private final List<ABDevice> mDevices = new ArrayList<>();
    private Integer mUpdatedDeviceIndex;

    ScannerLiveData() {
    }

    void deviceDiscovered(final ScanResult result, final DeviceBeacon beacon) {
        ABDevice device = null;
//        Timber.tag("getProductId").d(String.valueOf(beacon.getProductId()));
//        Timber.tag("getAgentId").d(String.valueOf(beacon.getAgentId()));
//        Timber.tag("getBrandId").d(String.valueOf(beacon.getBrandId()));
//        Timber.tag("getBeaconVersion").d(String.valueOf(beacon.getBeaconVersion()));
        final int index = indexOf(result.getDevice().getAddress());
        if (index == -1) {
           // if (beacon.getProductId() == 0) {
                device = new ABEarbuds(result, (EarbudsBeacon) beacon);
                Timber.tag("add device").d(String.valueOf(device.getBleAddress()));
                mDevices.add(device);
           // }
            mUpdatedDeviceIndex = null;
        } else {
            device = mDevices.get(index);
            mUpdatedDeviceIndex = index;
        }
        if (device != null) {
            // Update info from beacon
            device.updateDeviceStatus(beacon);
            // Update RSSI
            device.setRssi(result.getRssi());
        }

        postValue(this);
    }

    /**
     * Clears the list of devices found.
     */
    void clear() {
        mDevices.clear();
        mUpdatedDeviceIndex = null;
        postValue(this);
    }

    @NonNull
    public List<ABDevice> getDevices() {
        return mDevices;
    }

    /**
     * Returns null if a new device was added, or an index of the updated device.
     */
    @Nullable
    public Integer getUpdatedDeviceIndex() {
        final Integer i = mUpdatedDeviceIndex;
        mUpdatedDeviceIndex = null;
        return i;
    }

    public boolean isEmpty() {
        return mDevices.isEmpty();
    }

    /**
     * Finds the index of existing devices on the scan results list.
     *
     * @param bleAddress BLE Address (constant)
     * @return index of -1 if not found
     */
    private int indexOf(final String bleAddress) {
        int i = 0;
        for (final ABDevice device : mDevices) {
            if (device.matches(bleAddress))
                return i;
            i++;
        }
        return -1;
    }
}
