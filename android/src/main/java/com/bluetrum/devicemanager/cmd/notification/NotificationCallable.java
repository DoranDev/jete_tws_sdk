package com.bluetrum.devicemanager.cmd.notification;

import androidx.annotation.NonNull;

import com.bluetrum.devicemanager.cmd.payloadhandler.PayloadHandler;

/**
 * @deprecated
 * Use class inheriting from {@link PayloadHandler}.
 */
@Deprecated
public abstract class NotificationCallable<T> extends PayloadHandler<T> {

    public NotificationCallable(@NonNull final byte[] payload) {
        super(payload);
    }

}