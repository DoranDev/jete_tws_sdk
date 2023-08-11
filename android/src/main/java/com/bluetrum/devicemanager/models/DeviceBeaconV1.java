package com.bluetrum.devicemanager.models;

import androidx.annotation.NonNull;

import com.bluetrum.utils.BluetoothUtils;
import com.bluetrum.utils.ParserUtils;

public class DeviceBeaconV1 extends EarbudsBeacon {

    public static final int BEACON_DATA_LENGTH = 0x0D; // 第一字节是长度

    protected static final int POS_CHARGING_STATUS    = 7;

    protected boolean mLeftCharging;
    protected int mLeftBatteryLevel;
    protected boolean mRightCharging;
    protected int mRightBatteryLevel;
    protected boolean mCaseCharging;
    protected int mCaseBatteryLevel;

    public DeviceBeaconV1(@NonNull byte[] data) {
        super(data);
        // 接着处理父类没处理的信息数据

        // Bluetooth classic address
        byte[] btAddress = new byte[6];
        byteBuffer.get(btAddress);
        mBtAddress = BluetoothUtils.getAddressStringFromByte(btAddress);
        // FMSK
        int featureMask = byteBuffer.get();
        needAuth = ParserUtils.getBitValue(featureMask, POS_NEED_AUTH) == 1;
        connectionState = (featureMask >> POS_CONNECTION_STATE) & 0x03;

        // BAT
        byte leftBatteryLevel = byteBuffer.get();
        mLeftCharging = (ParserUtils.getBitValue(leftBatteryLevel, POS_CHARGING_STATUS) == 1);
        mLeftBatteryLevel = leftBatteryLevel & 0x7F;

        byte rightBatteryLevel = byteBuffer.get();
        mRightCharging = (ParserUtils.getBitValue(rightBatteryLevel, POS_CHARGING_STATUS) == 1);
        mRightBatteryLevel = rightBatteryLevel & 0x7F;

        byte caseBatteryLevel = byteBuffer.get();
        mCaseCharging = (ParserUtils.getBitValue(caseBatteryLevel, POS_CHARGING_STATUS) == 1);
        mCaseBatteryLevel = caseBatteryLevel & 0x7F;
    }

    /* 电量和充电状态 */

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

}
