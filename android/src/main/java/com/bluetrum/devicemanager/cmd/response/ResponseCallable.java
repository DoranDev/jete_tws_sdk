package com.bluetrum.devicemanager.cmd.response;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler;

/**
 * @deprecated
 * Use class inheriting from {@link PayloadHandler}.
 */
@Deprecated
public abstract class ResponseCallable<T> extends PayloadHandler<T> {

    public ResponseCallable(@NonNull byte[] payload) {
        super(payload);
    }

}
