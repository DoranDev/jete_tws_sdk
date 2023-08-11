package com.bluetrum.devicemanager.cmd.request;

import com.bluetrum.devicemanager.cmd.Request;

public final class BluetoothNameRequest extends Request {

    private final String bluetoothName;

    public BluetoothNameRequest(String bluetoothName) {
        super(COMMAND_BLUETOOTH_NAME);
        this.bluetoothName = bluetoothName;
    }

    @Override
    public byte[] getPayload() {
        return bluetoothName.getBytes();
    }
}
