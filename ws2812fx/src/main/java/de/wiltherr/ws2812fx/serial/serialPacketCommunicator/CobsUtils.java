package de.wiltherr.ws2812fx.serial.serialPacketCommunicator;

import java.nio.ByteBuffer;
import java.util.Arrays;

/*
* source: http://www.sureshjoshi.com/development/streaming-protocol-buffers-with-cobs/
* */
public class CobsUtils {

    public static final int OVERHEAD_LENGTH = 2;

    // Expected to be the entire packet to encode
    public static byte[] encode(byte[] packet) {
        if (packet == null
                || packet.length == 0) {
            return new byte[]{};
        }

        byte[] output = new byte[packet.length + OVERHEAD_LENGTH];
        byte blockStartValue = 1;
        int lastZeroIndex = 0;
        int srcIndex = 0;
        int destIndex = 1;

        while (srcIndex < packet.length) {
            if (packet[srcIndex] == 0) {
                output[lastZeroIndex] = blockStartValue;
                lastZeroIndex = destIndex++;
                blockStartValue = 1;
            } else {
                output[destIndex++] = packet[srcIndex];
                if (++blockStartValue == 255) {
                    output[lastZeroIndex] = blockStartValue;
                    lastZeroIndex = destIndex++;
                    blockStartValue = 1;
                }
            }

            ++srcIndex;
        }

        output[lastZeroIndex] = blockStartValue;
        return output;
    }

    // Expected to be the entire packet to decode with trailing 0
    public static byte[] decode(byte[] packet) {
        if (packet == null
                || packet.length == 0
                || packet[packet.length - 1] != 0) {
            return new byte[]{};
        }

        byte[] output = new byte[packet.length - OVERHEAD_LENGTH];
        int srcPacketLength = packet.length - 1;
        int srcIndex = 0;
        int destIndex = 0;

        while (srcIndex < srcPacketLength) {
            int code = packet[srcIndex++] & 0xff;
            for (int i = 1; srcIndex < srcPacketLength && i < code; ++i) {
                output[destIndex++] = packet[srcIndex++];
            }
            if (code != 255 && srcIndex != srcPacketLength) {
                output[destIndex++] = 0;
            }
        }

        return output;
    }

    public static byte[] decodeFromBuffer(ByteBuffer byteBuffer) {
        return decode(Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.position()));
    }

    public static byte[] trim(byte[] bytes) {
        int endIdx = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == 0x00) {
                return Arrays.copyOfRange(bytes, 0, i + 1);
            }
        }
        throw new IllegalArgumentException("no 0 bytes to trim");
    }

}
