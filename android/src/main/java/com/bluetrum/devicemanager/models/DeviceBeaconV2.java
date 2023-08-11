package com.bluetrum.devicemanager.models;

import androidx.annotation.NonNull;

import com.bluetrum.utils.BluetoothUtils;
import com.bluetrum.utils.ParserUtils;

public class DeviceBeaconV2 extends EarbudsBeacon {

    public static final int BEACON_DATA_LENGTH = 0x0D; // 第一字节是长度

    public DeviceBeaconV2(@NonNull byte[] data) {
        super(data);
        // 接着处理父类没处理的信息数据

        // Bluetooth classic address
        byte[] btAddress = new byte[6];
        byteBuffer.get(btAddress);
        deobfuscateBtAddress(btAddress);
        mBtAddress = BluetoothUtils.getAddressStringFromByte(btAddress);
        // FMSK
        int featureMask = byteBuffer.get();
        needAuth = ParserUtils.getBitValue(featureMask, POS_NEED_AUTH) == 1;
        connectionState = (featureMask >> POS_CONNECTION_STATE) & 0x03;
        useCustomSppUuid = ParserUtils.getBitValue(featureMask, POS_CUSTOM_SPP_UUID) == 1;

        // BID
        brandId = byteBuffer.get() | byteBuffer.get() << 8 | byteBuffer.get() << 16;
    }

    private void deobfuscateBtAddress(byte[] btAddress) {
        for (int i = 0; i < btAddress.length; i++) {
            btAddress[i] ^= (byte) 0xAD;
        }
    }

}
