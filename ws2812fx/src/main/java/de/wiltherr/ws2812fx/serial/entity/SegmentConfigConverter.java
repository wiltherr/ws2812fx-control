package de.wiltherr.ws2812fx.serial.entity;

import de.wiltherr.ws2812fx.SegmentConfig;
import de.wiltherr.ws2812fx.serial.entity.uint.*;

import java.util.List;
import java.util.stream.Collectors;


public class SegmentConfigConverter {


    public static SegmentConfig fromUIntPacket(UIntPacket uIntPacket) {
        if (uIntPacket.getBytes().length != ByteMask.SEGMENT_CONFIG.getByteSize())
            throw new IllegalArgumentException(); //TODO messsage
        List<UInt> uIntList = uIntPacket.getList();
        return new SegmentConfig(
                (int) uIntList.get(0).toLong(), //speed
                ModeConverter.fromUInt8((UInt8) uIntList.get(1)), //mode
                OptionsConverter.fromUInt8((UInt8) uIntList.get(2)), //options
                uIntList.subList(3, uIntList.size()).stream()
                        .map(uInt32 -> ColorConverter.fromUInt32((UInt32) uInt32))
                        .collect(Collectors.toList())
        );
    }


    public static UIntPacket toUIntPacket(SegmentConfig segmentConfig) {
        UInt32[] colorsValue = segmentConfig.getColors().stream().map( //4 uint8 will be interpreted as one uint32
                ColorConverter::toUInt32
        ).collect(Collectors.toList()).toArray(new UInt32[0]);
        return UIntPacketBuilder.createOf(
                UInt.create16(segmentConfig.getSpeed()), //speed
                UInt.create8(segmentConfig.getMode().getModeNumber()), //mode
                OptionsConverter.toUInt8(segmentConfig.getOptions()) //options
        ).append(colorsValue) //colors
                .build(); //options
    }
}
