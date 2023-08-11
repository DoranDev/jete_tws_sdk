package com.bluetrum.abmate.bluetooth;

import android.os.ParcelUuid;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class BleScanManager {

    /**
     * Start scanning for Bluetooth devices.
     */
    public static void startScan(final UUID filterUuid, @NonNull final ScanCallback scanCallback) {
        // Scanning settings
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                // Refresh the devices list every second
                .setReportDelay(0)
                // Hardware filtering has some issues on selected devices
                .setUseHardwareFilteringIfSupported(false)
                // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
                /*.setUseHardwareBatchingIfSupported(false)*/
                .build();

        // Let's use the filter to scan only for Bluetrum bluetooth earbuds.
        final List<ScanFilter> filters = new ArrayList<>();
        if (filterUuid != null) {
            filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid((filterUuid))).build());
        }

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(filters, settings, scanCallback);
    }

    /**
     * stop scanning for bluetooth devices.
     */
    public static void stopScan(@NonNull final ScanCallback scanCallback) {
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
    }

}
