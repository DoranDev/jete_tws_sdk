package com.bluetrum.devicemanager.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class DeviceBeacon {

    protected ByteBuffer byteBuffer;

    private final byte[] data;

    private final int beaconVersion;
    private final int productId;

    protected int brandId = 0;

    public static boolean isDeviceBeacon(final byte[] manufacturerSpecificData) {
        return manufacturerSpecificData != null
                // 广播包版本1和2没有其他的鉴别方法，只能通过长度识别
                && ((manufacturerSpecificData.length == DeviceBeaconV1.BEACON_DATA_LENGTH && getDeviceBeaconVersion(manufacturerSpecificData) == 1)
                || (manufacturerSpecificData.length == DeviceBeaconV2.BEACON_DATA_LENGTH && getDeviceBeaconVersion(manufacturerSpecificData) == 2));
    }

    public static int getDeviceBeaconVersion(@NonNull final byte[] manufacturerSpecificData) {
        return manufacturerSpecificData[0] & 0x0F;
    }

    @Nullable
    public static DeviceBeacon getDeviceBeacon(@NonNull final byte[] manufacturerSpecificData) {
        if (isDeviceBeacon(manufacturerSpecificData)) {
            int beaconVersion = getDeviceBeaconVersion(manufacturerSpecificData);
            if (beaconVersion == 1) {
                return new DeviceBeaconV1(manufacturerSpecificData);
            } else if (beaconVersion == 2) {
                return new DeviceBeaconV2(manufacturerSpecificData);
            }
        }
        return null;
    }

    public DeviceBeacon(@NonNull byte[] data) {
        this.data = data;

        byteBuffer = ByteBuffer.wrap(data)
                .order(ByteOrder.LITTLE_ENDIAN);

        // Feature
        int vid = byteBuffer.get() & 0xFF;
        // Beacon version
        beaconVersion = vid & 0x0F;
        // Product ID
        productId = byteBuffer.getShort() & 0xFFFF;

        // 剩下的不同子类不同，需要在子类中处理
    }

    public byte[] getBeaconData() {
        return data;
    }

    /**
     * Beacon的格式版本
     * @return 格式版本
     */
    public int getBeaconVersion() {
        return beaconVersion;
    }

    /**
     * 不同产品类型对应不同的动画和处理方式
     * @return 产品类型
     */
    public int getProductId() {
        return productId;
    }

    /**
     * 品牌ID，用来分辨不同客户的产品
     * @return 品牌ID
     */
    public int getBrandId() {
        return brandId;
    }

    /**
     * 代理ID，用来过滤不同代理的产品
     * @return 代理ID
     */
    public int getAgentId() {
        return getBrandId() >> 16;
    }

}
