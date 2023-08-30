package com.bluetrum.abmate.viewmodels;

import androidx.annotation.NonNull;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScannerViewModel extends BaseViewModel {

    private final ScannerRepository mScannerRepository;


    @Inject
    ScannerViewModel(@NonNull final DeviceRepository deviceRepository, @NonNull final ScannerRepository scannerRepository) {
        super(deviceRepository);
        this.mScannerRepository = scannerRepository;
        scannerRepository.registerBroadcastReceivers();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mScannerRepository.unregisterBroadcastReceivers();
    }

    /**
     * Returns an instance of the scanner repository
     */
    public ScannerRepository getScannerRepository() {
        return mScannerRepository;
    }
}
