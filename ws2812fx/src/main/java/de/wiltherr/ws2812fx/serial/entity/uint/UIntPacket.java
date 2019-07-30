package de.wiltherr.ws2812fx.serial.entity.uint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UIntPacket {

    private final List<UInt> uIntList;

    UIntPacket() {
        uIntList = new ArrayList<>();
    }

    UIntPacket(List<UInt> uIntList) {
        this.uIntList = new ArrayList<>(uIntList);
    }

    public byte[] getBytes() {
        int byteArraySize = uIntList.stream().mapToInt(UInt::byteSize).sum();
        byte[] result = new byte[byteArraySize];
        int offset = 0;
        for (UInt uInt : uIntList) {
            System.arraycopy(uInt.getBytes(), 0, result, offset, uInt.byteSize());
            offset += uInt.byteSize();
        }
        return result;
    }

    public byte[] getBytesLittleEndian() {
        int byteArraySize = uIntList.stream().mapToInt(UInt::byteSize).sum();
        byte[] result = new byte[byteArraySize];
        int offset = 0;
        for (UInt uInt : uIntList) {
            System.arraycopy(uInt.getBytesLittleEndian(), 0, result, offset, uInt.byteSize());
            offset += uInt.byteSize();
        }
        return result;
    }

    public List<UInt> getList() {
        return Collections.unmodifiableList(uIntList);
    }

    public UIntPacket subPacket(int fromIndex, int toIndex) {
        return UIntPacketBuilder.createOf(this.uIntList.subList(fromIndex, toIndex)).build();
    }

    public List<UIntPacket> splitByMask(UIntByteMask byteMask) {
        int uIntTypeCount = byteMask.getUIntTypes().size();
        List<UIntPacket> uIntPacketList = new ArrayList<>();
        for (int i = 0; i < uIntList.size(); i += uIntTypeCount) {
            uIntPacketList.add(
                    subPacket(i, i + uIntTypeCount)
            );
        }
        return uIntPacketList;
    }

    @Override
    public String toString() {
        return uIntList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String toStringWithUIntTypes() {
        return uIntList.stream()
                .map(uInt -> String.format("%s: %s", uInt.getClass().getSimpleName(), String.valueOf(uInt)))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UIntPacket that = (UIntPacket) o;

        return this.uIntList.equals(that.uIntList);
    }

    @Override
    public int hashCode() {
        return uIntList.hashCode();
    }
}
