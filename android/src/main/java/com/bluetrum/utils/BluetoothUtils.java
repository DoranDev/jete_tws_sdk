package com.bluetrum.utils;

import android.bluetooth.BluetoothDevice;

public class BluetoothUtils {

    private static final String TAG = "BluetoothUtils";

    public static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

    static final int BD_ADDR_LEN = 6; // bytes

    public static String getAddressStringFromByte(byte[] address) {
        if (address == null || address.length !=6) {
            return null;
        }
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
                address[0], address[1], address[2], address[3], address[4],
                address[5]);
    }

    public static byte[] getByteAddress(BluetoothDevice device) {
        return getBytesFromAddress(device.getAddress());
    }

    public static byte[] getBytesFromAddress(String address) {
        int i, j = 0;
        byte[] output = new byte[BD_ADDR_LEN];
        for (i = 0; i < address.length();i++) {
            if (address.charAt(i) != ':') {
                output[j] = (byte) Integer.parseInt(address.substring(i, i+2), 16);
                j++;
                i++;
            }
        }
        return output;
    }

}
