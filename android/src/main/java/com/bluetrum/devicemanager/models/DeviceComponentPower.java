package com.bluetrum.devicemanager.models;

public final class DeviceComponentPower {

    private final int powerLevel;
    private final boolean isCharging;

    public DeviceComponentPower(final byte powerData) {
        isCharging = (powerData & 0x80) != 0;
        powerLevel = (powerData & 0x7F);
    }

    public int getPowerLevel() {
        return powerLevel;
    }

    public boolean isCharging() {
        return isCharging;
    }

    @Override
    public String toString() {
        return "DeviceComponentPower{" +
                "powerLevel=" + powerLevel +
                ", isCharging=" + isCharging +
                '}';
    }
}
