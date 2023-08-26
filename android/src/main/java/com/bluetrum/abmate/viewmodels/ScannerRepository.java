package com.bluetrum.abmate.viewmodels;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bluetrum.abmate.BuildConfig;
import com.bluetrum.abmate.bluetooth.BleScanManager;
import com.bluetrum.abmate.models.ABEarbuds;
import com.bluetrum.abmate.utils.Utils;
import com.bluetrum.devicemanager.DeviceManagerApi;
import com.bluetrum.devicemanager.models.DeviceBeacon;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import timber.log.Timber;

/**
 * Repository for scanning for bluetooth devices
 */
public class ScannerRepository {

    private static final String TAG = ScannerRepository.class.getSimpleName();
    private final Context mContext;
    private final DeviceManagerApi deviceManagerApi;

    /**
     * MutableLiveData containing the scanner state to notify MainActivity.
     */
    private final ScannerLiveData mScannerLiveData;
    private final ScannerStateLiveData mScannerStateLiveData;

    private final ScanCallback mScanCallbacks = new ScanCallback() {

        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
//            Timber.tag(TAG).v("scan result = %s", result);
            try {
                final ScanRecord scanRecord = result.getScanRecord();
                // 过滤beacon，符合条件的才会添加到列表
                if (scanRecord != null && scanRecord.getBytes() != null) {
                    // If the packet has been obtained while Location was disabled, mark Location as not required
//                    if (Utils.isLocationRequired(mContext) && !Utils.isLocationEnabled(mContext))
//                        Utils.markLocationNotRequired(mContext);

                    updateScannerLiveData(result);
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
            mScannerStateLiveData.scanningStopped();
        }
    };

    /**
     * Broadcast receiver to monitor the changes in the location provider
     */
    private final BroadcastReceiver mLocationProviderChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final boolean enabled = Utils.isLocationEnabled(context);
            mScannerStateLiveData.setLocationEnabled(enabled);
        }
    };
    /**
     * Broadcast receiver to monitor the changes in the bluetooth adapter
     */
    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    mScannerStateLiveData.bluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan();
                        mScannerStateLiveData.bluetoothDisabled();
                    }
                    break;
            }
        }
    };

    @Inject
    public ScannerRepository(@ApplicationContext @NonNull final Context context,
                             @NonNull final DeviceManagerApi deviceManagerApi) {
        this.mContext = context;
        this.deviceManagerApi = deviceManagerApi;
        mScannerStateLiveData = new ScannerStateLiveData(Utils.isBleEnabled(), Utils.isLocationEnabled(context));
        mScannerLiveData = new ScannerLiveData();
    }

    public ScannerStateLiveData getScannerState() {
        return mScannerStateLiveData;
    }

    public ScannerLiveData getScannerResults() {
        return mScannerLiveData;
    }

    private void updateScannerLiveData(final ScanResult result) {
        // 如果需要设置名称：Type = 0x09
        final ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord != null) {
            // 广播包过滤设备
           // Timber.tag("scanRecord").d(scanRecord.toString());
            byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(ABEarbuds.MANUFACTURER_ID);
//            if(manufacturerSpecificData!=null){
//              Timber.tag("manufacturerSpecific").d(manufacturerSpecificData.toString());
//            }
            if (DeviceBeacon.isDeviceBeacon(manufacturerSpecificData)) {
                DeviceBeacon deviceBeacon = DeviceBeacon.getDeviceBeacon(manufacturerSpecificData);
              //  Timber.tag("deviceBeacon").d(String.valueOf(deviceBeacon.getAgentId()));
                if (deviceBeacon != null && (deviceBeacon.getAgentId() == BuildConfig.BRAND_ID >> 16)) { // 过滤代理产品
                   // Timber.tag("updateScannerLiveData").d(result.toString());
                    mScannerLiveData.deviceDiscovered(result, deviceBeacon);
                    mScannerStateLiveData.deviceFound();
                }
            }
        }
    }

    /**
     * Register for required broadcast receivers.
     */
    void registerBroadcastReceivers() {
        mContext.registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (Utils.isWithinMarshmallowAndR()) {
            mContext.registerReceiver(mLocationProviderChangedReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
        }
    }

    /**
     * Unregister for required broadcast receivers.
     */
    void unregisterBroadcastReceivers() {
        mContext.unregisterReceiver(mBluetoothStateBroadcastReceiver);
        if (Utils.isWithinMarshmallowAndR()) {
            mContext.unregisterReceiver(mLocationProviderChangedReceiver);
        }
    }

    /**
     * Start scanning for Bluetooth devices.
     */
    public void startScan() {
        if (mScannerStateLiveData.isScanning()) {
            return;
        }
        mScannerStateLiveData.scanningStarted();
        Timber.tag("startScan").d("scanningStarted");
        BleScanManager.startScan(null, mScanCallbacks);
    }

    /**
     * stop scanning for bluetooth devices.
     */
    public void stopScan() {
        if (!mScannerStateLiveData.isScanning()) {
            return;
        }

        BleScanManager.stopScan(mScanCallbacks);

        mScannerStateLiveData.scanningStopped();
        mScannerLiveData.clear();
    }

}