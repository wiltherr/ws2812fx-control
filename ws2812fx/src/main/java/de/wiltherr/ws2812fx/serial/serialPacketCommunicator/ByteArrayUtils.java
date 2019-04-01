package de.wiltherr.ws2812fx.serial.serialPacketCommunicator;

import java.util.Arrays;

public class ByteArrayUtils {
    public static int[] toUInt8Array(byte[] byteArray) {
        int[] uInt8Array = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            uInt8Array[i] = Byte.toUnsignedInt(byteArray[i]);
        }
        return uInt8Array;
    }

    public static String toStringHex(byte[] byteArray) {
        String[] strings = new String[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            strings[i] = String.format("0x%02X", byteArray[i]);
        }
        return "[" + String.join(", ", strings) + "]";
    }

    public static String toStringUnsigned(byte[] byteArray) {
        return Arrays.toString(toUInt8Array(byteArray));
    }

    public static String toStringSigned(byte[] bytesArray) {
        return Arrays.toString(bytesArray);
    }
}
