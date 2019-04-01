package de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint;

import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.ByteSizesNotMatchException;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UIntPacketBuilder {

    final List<UInt> uIntList = new ArrayList<>();

    private UIntPacketBuilder() {
    }

    public static UIntPacketBuilder create() {
        return new UIntPacketBuilder();
    }

    public static UIntPacketBuilder createOf(UInt... uInts) {
        return new UIntPacketBuilder().append(uInts);
    }

    public static UIntPacketBuilder createOf(UIntPacket... uIntPackets) {
        return new UIntPacketBuilder().append(uIntPackets);
    }

    public static UIntPacketBuilder createOf(List<UInt> uInts) {
        return new UIntPacketBuilder().append(uInts);
    }

    public static UIntPacket buildFromByteMask(UIntByteMask byteMask, byte[] byteArray, ByteOrder byteOrder) throws ByteSizesNotMatchException {
        return new UIntPacket(byteArray, byteOrder, byteMask);
    }

    public UIntPacket build() {
        return new UIntPacket(uIntList);
    }

    public UIntPacketBuilder append(UInt... uInts) {
        uIntList.addAll(Arrays.asList(uInts));
        return this;
    }

    public UIntPacketBuilder append(UIntPacket... uIntPackets) {
        Arrays.stream(uIntPackets)
                .flatMap(x -> x.getList().stream())
                .forEach(uIntList::add);
        return this;
    }

    public UIntPacketBuilder append(List<UInt> uInts) {
        uIntList.addAll(uInts);
        return this;
    }

    ;

}
