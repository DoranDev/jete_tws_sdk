package com.bluetrum.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CryptoUtils {

    /**
     * 使用随机数填充字节数组
     * @param bytes 需要填充的字节数组
     */
    public static void fillRandomBytes(@NonNull byte[] bytes) {
        new SecureRandom().nextBytes(bytes);
    }

    @Nullable
    public static byte[] getMD5(@NonNull final byte[] data) {
        try {
            // 使用MD5
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
