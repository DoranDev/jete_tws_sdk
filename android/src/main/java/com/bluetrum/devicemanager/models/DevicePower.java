package com.bluetrum.devicemanager.models;

import androidx.annotation.NonNull;

public final class DevicePower {

    private DeviceComponentPower leftSidePower;
    private DeviceComponentPower rightSidePower;
    private DeviceComponentPower casePower;

    public DevicePower(@NonNull final byte[] powerData) {
        parsePowerData(powerData);
    }

    private void parsePowerData(byte[] powerData) {
        if (powerData.length > 0) {
            byte data = powerData[0];
            leftSidePower = new DeviceComponentPower(data);
        }
        if (powerData.length > 1) {
            byte data = powerData[1];
            rightSidePower = new DeviceComponentPower(data);
        }
        if (powerData.length > 2) {
            byte data = powerData[2];
            casePower = new DeviceComponentPower(data);
        }
    }

    public DeviceComponentPower getLeftSidePower() {
        return leftSidePower;
    }

    public DeviceComponentPower getRightSidePower() {
        return rightSidePower;
    }

    public DeviceComponentPower getCasePower() {
        return casePower;
    }

    @NonNull
    @Override
    public String toString() {
        return "DevicePower{" +
                "leftSidePower=" + leftSidePower +
                ", rightSidePower=" + rightSidePower +
                ", casePower=" + casePower +
                '}';
    }
}
