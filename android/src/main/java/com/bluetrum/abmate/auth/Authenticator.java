package com.bluetrum.abmate.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Authenticator between App and device.
 * The App will send the data get from {@link Authenticator#getAuthData()} to device, and receive
 * data from device, then handle the data by {@link Authenticator#handleAuthData(byte[])}. Repeated
 * until the authentication is passed.
 * Note: Need to implement state machine by yourself.
 * Note: If no need to verify, {@link Authenticator#isAuthPassed()} can always return true.
 */
public interface Authenticator {

    /**
     * Get the authentication data which will be sent to device.
     * @return The authentication data will be sent to device
     */
    @Nullable
    byte[] getAuthData();

    /**
     * Handle the authentication data from device.
     * @param data Authentication data
     * @return Weather the data passes the authentication program
     */
    boolean handleAuthData(@NonNull byte[] data);

    /**
     * Check if authentication is passed. If no need to authenticate, can always return true.
     * @return Authentication state
     */
    boolean isAuthPassed();

    /**
     * Reset authenticator
     */
    void reset();

}
