package com.bluetrum.abmate.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class NullAuthenticator implements Authenticator {

    @Nullable
    @Override
    public byte[] getAuthData() {
        return null;
    }

    @Override
    public boolean handleAuthData(@NonNull byte[] data) {
        return false;
    }

    @Override
    public boolean isAuthPassed() {
        return true;
    }

    @Override
    public void reset() {
        // empty
    }

}
