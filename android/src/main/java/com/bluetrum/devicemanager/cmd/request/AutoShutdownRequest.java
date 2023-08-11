package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

/**
 * 参数：0是立即关机，0xFF取消自动关机，中间其他数值则是自定义关机时间
 */
public final class AutoShutdownRequest extends Request {

    private static final byte AUTO_SHUTDOWN_IMMEDIATELY     = 0x00;
    private static final byte AUTO_SHUTDOWN_CANCEL          = (byte) 0xFF;

    private final byte setting;

    public AutoShutdownRequest(byte setting) {
        super(COMMAND_AUTO_SHUTDOWN);
        this.setting = setting;
    }

    @Override
    public byte[] getPayload() {
        return new byte[] { setting };
    }
}
