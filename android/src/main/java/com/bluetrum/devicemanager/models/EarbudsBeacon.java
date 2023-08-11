package com.bluetrum.devicemanager.models;

import androidx.annotation.NonNull;

public abstract class EarbudsBeacon extends DeviceBeacon {

    protected static final int DEVICE_CONNECTION_STATE_DISCONNECTED    = 0;
    protected static final int DEVICE_CONNECTION_STATE_CONNECTED       = 1;

    protected static final int POS_NEED_AUTH          = 0;
    protected static final int POS_CONNECTION_STATE   = 2;
    protected static final int POS_CUSTOM_SPP_UUID    = 4;

    protected String mBtAddress;
    protected boolean needAuth;
    protected int connectionState;
    protected boolean useCustomSppUuid;

    public EarbudsBeacon(@NonNull byte[] data) {
        super(data);
        // 接着处理父类没处理的信息数据
        // Implement in subclass
    }

    /**
     * 从广播包获取耳机的连接状态
     * @return 是否已经连接
     */
    public boolean isConnected() {
        return connectionState == DEVICE_CONNECTION_STATE_CONNECTED;
    }

    public String getBtAddress() {
        return mBtAddress;
    }

    public boolean needAuth() {
        return needAuth;
    }

    public boolean useCustomSppUuid() {
        return useCustomSppUuid;
    }

}
