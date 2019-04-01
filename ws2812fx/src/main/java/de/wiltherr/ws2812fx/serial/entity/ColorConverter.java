package de.wiltherr.ws2812fx.serial.entity;

import de.wiltherr.ws2812fx.Color;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.ByteArrayUtils;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt32;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UIntPacket;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UIntPacketBuilder;

public class ColorConverter {

    public static Color fromUInt32(UInt32 uInt32) {
        int[] color = ByteArrayUtils.toUInt8Array(uInt32.getBytes());
        return new Color(color[1], color[2], color[3], color[0]);
    }

    public static UIntPacket toUIntPacket(Color color) {
        return UIntPacketBuilder.createOf(
                toUInt32(color)
        ).build();
    }

    public static UInt32 toUInt32(Color color) {
        return (UInt32) UInt.ofByteArray(new byte[]{
                        (byte) color.getW(),
                        (byte) color.getR(),
                        (byte) color.getG(),
                        (byte) color.getB()
                }
        );
    }
}
