package com.bluetrum.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ParserUtils {

    private static final String TAG = ParserUtils.class.getSimpleName();

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
    private static final String PATTERN_KEY = "[0-9a-fA-F]{32}";
    private static final String PATTERN_UUID_HEX = "[0-9a-fA-F]{32}";

    private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final byte[] ALPHANUMERIC = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public static String bytesToHex(final byte[] bytes, final boolean add0x) {
        if (bytes == null)
            return "";
        return bytesToHex(bytes, 0, bytes.length, add0x);
    }

    public static String bytesToHex(final byte[] bytes, final int start, final int length, final boolean add0x) {
        if (bytes == null || bytes.length <= start || length <= 0)
            return "";

        final int maxLength = Math.min(length, bytes.length - start);
        final char[] hexChars = new char[maxLength * 2];
        for (int j = 0; j < maxLength; j++) {
            final int v = bytes[start + j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        if (!add0x)
            return new String(hexChars);
        return "0x" + new String(hexChars);
    }

    public static byte[] toByteArray(String hexString) {
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    public static int getBitValue(final int value, final int position) {
        return (value >> position) & 1;
    }

    /**
     * Validates the key input
     *
     * @param key key
     * @return true if the Key is a valid value
     * @throws IllegalArgumentException in case of an invalid was entered as an input and the message containing the error
     */
    public static boolean validateKeyInput(@NonNull final String key) throws IllegalArgumentException {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("Key cannot be empty!");
        } else if (!key.matches(PATTERN_KEY)) {
            throw new IllegalArgumentException("key must be a 32-character hexadecimal string!");
        }

        return true;
    }

    public static int getValue(final byte[] bytes) {
        if (bytes == null || bytes.length != 2)
            return 0;
        return unsignedToSigned(unsignedBytesToInt(bytes[0], bytes[1]), 16);
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    public static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    public static int bytesToInt(@NonNull byte[] b) {
        return b.length == 4 ? ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).getInt() : ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).getShort();
    }

    public static int hexToInt(String hex) {
        return ParserUtils.bytesToInt(ParserUtils.toByteArray(hex));
    }

    public static byte[] intToBytes(int i) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(i);
        return b.array();
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded signed value.
     */
    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    /**
     * Returns UUID as a hex
     *
     * @param uuid UUID
     */
    public static String uuidToHex(@NonNull final UUID uuid) {
        return uuid.toString().replace("-", "").toUpperCase(Locale.US);
    }

    /**
     * Returns UUID as a hex
     *
     * @param uuid UUID
     */
    public static String uuidToHex(@NonNull final String uuid) {
        return uuid.replace("-", "").toUpperCase(Locale.US);
    }

    /**
     * Returns UUID in bytes
     *
     * @param uuid UUID
     */
    public static byte[] uuidToBytes(@NonNull final UUID uuid) {
        return toByteArray(uuid.toString().replace("-", ""));
    }

    /**
     * Formats a hex string without dashes to a uuid string
     *
     * @param uuidHex Hex string
     */
    public static String formatUuid(@NonNull final String uuidHex) {
        if (isUuidPattern(uuidHex)) {
            return new StringBuffer(uuidHex).
                    insert(8, "-").
                    insert(13, "-").
                    insert(18, "-").
                    insert(23, "-").toString();
        }
        return null;
    }

    public static boolean isUuidPattern(@NonNull final String uuidHex) {
        return uuidHex.matches(PATTERN_UUID_HEX);
    }

    /**
     * Returns a type4 UUID from a uuid string without dashes
     *
     * @param uuidHex Hex string
     */
    public static UUID getUuid(@NonNull final String uuidHex) {
        if (uuidHex.matches(PATTERN_UUID_HEX)) {
            return UUID.fromString(new StringBuffer(uuidHex).
                    insert(8, "-").
                    insert(4, "-").
                    insert(4, "-").
                    insert(4, "-").toString());
        }
        return null;
    }

    /**
     * Converts the timestamp to long
     *
     * @param timestamp timestamp
     */
    public static Long parseTimeStamp(@NonNull final String timestamp) throws ParseException {
        return SDF.parse(timestamp).getTime();
    }

    /**
     * Formats the timestamp
     *
     * @param timestamp timestamp
     */
    public static String formatTimeStamp(final long timestamp) {
        return SDF.format(new Date(timestamp));
    }

}
