package de.wiltherr.ws2812fx.serial.entity;

import de.wiltherr.ws2812fx.Segment;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UIntPacket;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UIntPacketBuilder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt.Type.U_INT_16;


public class SegmentConverter {

    public static List<Segment> fromUIntPacketToMultiSegment(UIntPacket uIntPacket) {
        List<Segment> segments = uIntPacket.split(EntityByteMask.SEGMENT).stream()
                .map(SegmentConverter::fromUIntPacket)
                .collect(Collectors.toList());
        IntStream.range(0, segments.size())
                .forEach(i -> segments.get(i).setIndex(i));
        return segments;
    }

    public static Segment fromUIntPacket(UIntPacket uIntPacket) {
        if (uIntPacket.getBytes().length != EntityByteMask.SEGMENT.getByteSize())
            throw new IllegalArgumentException(); //TODO messsage
        List<UInt> uInts = uIntPacket.getList();
        return new Segment(
                -1,
                (int) uInts.get(0).toLong(),
                (int) uInts.get(1).toLong(),
                SegmentConfigConverter.fromUIntPacket(uIntPacket.subPacket(2, uInts.size())));
    }

    public static UIntPacket toUIntPacket(Segment segment) {
        return UIntPacketBuilder.createOf(
                UInt.create(segment.getStart(), U_INT_16), //start
                UInt.create(segment.getStop(), U_INT_16) //stop
        ).append(
                SegmentConfigConverter.toUIntPacket(segment.getConfig())
        ).build();
    }
}
