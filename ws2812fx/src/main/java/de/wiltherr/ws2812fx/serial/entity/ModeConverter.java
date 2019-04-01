package de.wiltherr.ws2812fx.serial.entity;


import de.wiltherr.ws2812fx.Mode;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt8;

public class ModeConverter {
    public static Mode fromUInt8(UInt8 mode) {
        return Mode.valueOf((int) mode.toLong());
    }
}
