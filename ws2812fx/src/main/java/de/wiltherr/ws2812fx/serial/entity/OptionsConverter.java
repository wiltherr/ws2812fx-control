package de.wiltherr.ws2812fx.serial.entity;

import de.wiltherr.ws2812fx.FadeOption;
import de.wiltherr.ws2812fx.Options;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt8;

public class OptionsConverter {

    public static Options fromUInt8(UInt8 uInt8) {
        return new Options(
                ((uInt8.getBytes()[0] & Options.RESERVE_INT_VALUE) == Options.RESERVE_INT_VALUE),
                ((uInt8.getBytes()[0] & Options.GAMMA_INT_VALUE) == Options.GAMMA_INT_VALUE),
                FadeOption.NO_OPTION //TODO
        );
    }

    public static UInt8 toUInt8(Options options) {
        return UInt.create8(options.calcIntValue());
    }
}
