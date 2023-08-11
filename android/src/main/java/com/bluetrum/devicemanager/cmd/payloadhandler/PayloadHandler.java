package com.bluetrum.devicemanager.cmd.payloadhandler;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;

/**
 * 用以替代DeviceInfoCallable和NotificationCallable，统一Payload数据处理
 * @param <T> 数据处理完成后返回的类型
 */
public abstract class PayloadHandler<T> implements Callable<T> {

    private final byte[] payload;

    public PayloadHandler(@NonNull final byte[] payload) {
        this.payload = payload;
    }

    @NonNull
    protected byte[] getPayload() {
        return payload;
    }

}
