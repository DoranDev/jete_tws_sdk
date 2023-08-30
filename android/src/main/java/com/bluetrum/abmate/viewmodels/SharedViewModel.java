package com.bluetrum.abmate.viewmodels;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SharedViewModel extends BaseViewModel {

    private ScannerRepository mScannerRepository;

    @Inject
    SharedViewModel(@NonNull DeviceRepository deviceRepository, @NonNull ScannerRepository scannerRepository) {
       super(deviceRepository);
        this.mScannerRepository = scannerRepository;
        scannerRepository.registerBroadcastReceivers();
        deviceRepository.registerBroadcastReceivers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mScannerRepository.unregisterBroadcastReceivers();
        deviceRepository.unregisterBroadcastReceivers();
    }

}
