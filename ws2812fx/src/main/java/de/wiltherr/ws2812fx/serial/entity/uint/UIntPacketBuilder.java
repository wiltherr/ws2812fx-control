package de.wiltherr.ws2812fx.serial.entity.uint;

import de.wiltherr.ws2812fx.serial.communication.ByteArrayUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UIntPacketBuilder {

    private final List<UInt> uIntList = new ArrayList<>();

    public UIntPacketBuilder() {
    }

    public static UIntPacketBuilder create() {
        return new UIntPacketBuilder();
    }

    public static UIntPacketBuilder createOf(long... uInts) {
        return new UIntPacketBuilder().append(uInts);
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

    public static UIntPacketBuilder createOf(byte[] byteArray, ByteOrder byteOrder, UIntByteMask byteMask) throws ByteSizesNotMatchException {
        return new UIntPacketBuilder().append(byteArray, byteOrder, byteMask);
    }

    public UIntPacket build() {
        return new UIntPacket(uIntList);
    }

    public UIntPacketBuilder append(long... uInts) {
        for (long uInt : uInts) {
            uIntList.add(UInt.createAuto(uInt));
        }
        return this;
    }

    public UIntPacketBuilder append(UInt... uInts) {
        uIntList.addAll(Arrays.asList(uInts));
        return this;
    }

    public UIntPacketBuilder append(List<UInt> uInts) {
        uIntList.addAll(uInts);
        return this;
    }

    public UIntPacketBuilder append(UIntPacket... uIntPackets) {
        Arrays.stream(uIntPackets)
                .flatMap(x -> x.getList().stream())
                .forEach(uIntList::add);
        return this;
    }

    public UIntPacketBuilder append(byte[] byteArray, ByteOrder byteOrder, UIntByteMask byteMask) throws ByteSizesNotMatchException {
        if (byteArray.length != byteMask.getByteSize())
            throw new ByteSizesNotMatchException(
                    String.format("Length of byteArray (=%d) not match byteMask size (=%d). ByteArray was: %s",
                            byteArray.length, byteMask.getByteSize(), ByteArrayUtils.toStringUnsigned(byteArray)));

        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        for (UInt.Type uIntType : byteMask.getUIntTypes()) {
            byte[] uIntBytes = new byte[uIntType.byteSize()];
            byteBuffer.get(uIntBytes, 0, uIntType.byteSize());
            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                uIntList.add(UInt.ofByteArray(uIntBytes));
            } else {
                uIntList.add(UInt.ofByteArrayLittleEndian(uIntBytes));
            }
        }
        return this;
    }

    public void clear() {
        uIntList.clear();
    }
}
