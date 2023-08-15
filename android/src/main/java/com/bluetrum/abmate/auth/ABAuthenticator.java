package com.bluetrum.abmate.auth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.bluetrum.utils.BluetoothUtils;

import java.util.Arrays;

import timber.log.Timber;

/**
 *
 */
public final class ABAuthenticator implements Authenticator {

    // State of authentication
    // 认证器自行控制状态机
    public static final int AUTHENTICATION_STATE_IDLE = 0;
    public static final int AUTHENTICATION_STATE_WAIT_RESPONSE = 1;
    public static final int AUTHENTICATION_STATE_WAIT_RESULT = 2;
    public static final int AUTHENTICATION_STATE_PASS = 3;
    public static final int AUTHENTICATION_STATE_NOT_PASS = 4;

    private int authenticationState = AUTHENTICATION_STATE_IDLE;

    static {
        System.loadLibrary("abauth");
    }

    /* ---------------- 下面变量是旧的，暂时保留 ---------------- */

    // Secret Key
    private final byte[] sk;
    // Random
    private byte[] r;
    // R'
    private byte[] rp;

    /* ---------------- 上面变量是旧的，暂时保留 ---------------- */

    public ABAuthenticator(String address) {
        byte[] btAddress = BluetoothUtils.getBytesFromAddress(address);
        sk = gSK(btAddress);
    }

    /* ---------------- Authenticator ---------------- */

    @Nullable
    @Override
    public byte[] getAuthData() {
        // 不同状态需要不同的认证数据
        switch (authenticationState) {
            case AUTHENTICATION_STATE_IDLE:
                authenticationState = AUTHENTICATION_STATE_WAIT_RESPONSE;
                return getRandom();
            case AUTHENTICATION_STATE_WAIT_RESPONSE:
                authenticationState = AUTHENTICATION_STATE_WAIT_RESULT;
                return getChallenge();
        }

        return null;
    }

    @Override
    public boolean handleAuthData(@NonNull byte[] data) {
        // 不同状态输入不同数据
        switch (authenticationState) {
            case AUTHENTICATION_STATE_WAIT_RESPONSE:
                // 等待固件回复
                if (data.length == 17 && data[0] == 0x11) {
                    byte[] encryptedRandom = Arrays.copyOfRange(data, 1, 17);
                    if (checkResponse(encryptedRandom)) {
                        authenticationState = AUTHENTICATION_STATE_WAIT_RESULT;
                        return true;
                    } else {
                        Timber.v("checkResponse: Failed");
                    }
                }
                authenticationState = AUTHENTICATION_STATE_NOT_PASS;
                return false;
            case AUTHENTICATION_STATE_WAIT_RESULT:
                // 检查结果
                if (data.length == 2
                        && (data[0] == 0x13 && data[1] == 0x00)) {
                    authenticationState = AUTHENTICATION_STATE_PASS;
                    return true;
                } else {
                    Timber.v("checkResult: Failed");
                }
                authenticationState = AUTHENTICATION_STATE_NOT_PASS;
                return false;
            case AUTHENTICATION_STATE_PASS:
                return true;
        }
        return false;
    }

    @Override
    public boolean isAuthPassed() {
        return authenticationState == AUTHENTICATION_STATE_PASS;
    }

    public void reset() {
        authenticationState = AUTHENTICATION_STATE_IDLE;
        r = null;
        rp = null;
    }

    /* ---------------- 下面方法是旧的，暂时保留 ---------------- */

    /**
     * 获取加密的随机数。
     * 生成的随机数会填充到r。
     * @return 使用sk加密随机数r得到的加密的随机数
     */
    public byte[] getRandom() {
        return gR();
    }

    /**
     * 验证设备端返回的值
     * @param response 设备端返回的数
     * @return 验证是否通过
     */
    public boolean checkResponse(byte[] response) {
        return cR(response);
    }

    /**
     * 获取Challenge
     * @return Challenge
     */
    public byte[] getChallenge() {
        return gC();
    }

    /**
     * 获取R'
     * @return R'
     */
    public byte[] getRPrime() {
        return rp;
    }

    /**
     * 获取解密Beacon的密钥
     * @return Beacon Key
     */
    public byte[] getBeaconKey() {
        if (rp == null)
            throw new IllegalStateException("No R'");

        return gBK(rp);
    }

    /**
     * 获取解密Beacon的密钥
     * @param rp R'
     * @return Beacon Key
     */
    public byte[] getBeaconKey(byte[] rp) {
        return gBK(rp);
    }

    public byte[] decryptBeacon(byte[] beacon) {
        return dB(beacon);
    }

    /* Native Methods */

    /**
     * Generate Secret Key
     * @param btAddress 蓝牙经典地址
     * @return Secret Key
     */
    private native byte[] gSK(byte[] btAddress);

    /**
     * Generate Random, and encrypt with Secret Key
     * @return 使用Secret Key加密的随机数R
     */
    private native byte[] gR();

    /**
     * Check Response
     * @param resp 设备端回复
     * @return 验证结果
     */
    private native boolean cR(byte[] resp);

    /**
     * Generate Challenge
     * @return Challenge
     */
    private native byte[] gC();

    /**
     * Generate Beacon Key
     * @param rp R'
     * @return Beacon Key
     */
    private native byte[] gBK(byte[] rp);

    /**
     * Decrypt Beacon
     * @param beacon Beacon Data
     * @return Decrypted Beacon Data
     */
    private native byte[] dB(byte[] beacon);

    /* ---------------- 上面方法是旧的，暂时保留 ---------------- */

}
