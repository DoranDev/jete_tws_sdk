package com.bluetrum.devicemanager.models;

public class DeviceCapacities {

    public static final int DEVICE_CAPACITY_SUPPORT_TWS             = (1 << 0);
    public static final int DEVICE_CAPACITY_SUPPORT_SOUND_EFFECT_3D = (1 << 1);
    public static final int DEVICE_CAPACITY_SUPPORT_MULTIPOINT      = (1 << 2);
    public static final int DEVICE_CAPACITY_SUPPORT_ANC             = (1 << 3);

    public static boolean supportTws(int deviceCapacities) {
        return (deviceCapacities & DEVICE_CAPACITY_SUPPORT_TWS) != 0;
    }

    public static boolean supportSoundEffect3d(int deviceCapacities) {
        return (deviceCapacities & DEVICE_CAPACITY_SUPPORT_SOUND_EFFECT_3D) != 0;
    }

    public static boolean supportMultipoint(int deviceCapacities) {
        return (deviceCapacities & DEVICE_CAPACITY_SUPPORT_MULTIPOINT) != 0;
    }

    public static boolean supportAnc(int deviceCapacities) {
        return (deviceCapacities & DEVICE_CAPACITY_SUPPORT_ANC) != 0;
    }

}
