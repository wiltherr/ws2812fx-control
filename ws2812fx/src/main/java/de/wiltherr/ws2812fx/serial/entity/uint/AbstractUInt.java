package de.wiltherr.ws2812fx.serial.entity.uint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public abstract class AbstractUInt /*extends Number */ implements UInt {

    private static long MIN_VALUE = 0;

    private final Type uIntType;

    AbstractUInt(long uInt, Type uIntType) {
        this.uIntType = uIntType;
        if (!isValidUInt(uInt))
            throw new IllegalUIntException(String.format("uInt was %d but only values from %d to %d can converted to unsigned %d bit integers.", uInt, MIN_VALUE, uIntType.maxValue(), uIntType.bitSize()));
    }

    AbstractUInt(byte[] byteArray, Type uIntType) {
        if (byteArray.length != uIntType.byteSize()) {
            throw new IllegalArgumentException(String.format("byteArray must be of length %d for unsigned %d bit integers.", uIntType.byteSize(), uIntType.bitSize()));
        }
        this.uIntType = uIntType;
    }

    boolean isValidUInt(long uInt) {
        return uInt <= uIntType.MAX_VALUE && uInt >= MIN_VALUE;
    }

    @Override
    public long longValue() {
        return toLong();
    }

    @Override
    public byte[] getBytesLittleEndian() {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putLong(toLong());
        byte[] buffer = new byte[byteSize()];
        System.arraycopy(bb.array(), 0, buffer, 0, this.byteSize());
        return buffer;
    }

    @Override
    public byte[] getBytesBigEndian() {
        return this.getBytes();
    }

    @Override
    public Type getType() {
        return uIntType;
    }

    @Override
    public int byteSize() {
        return uIntType.byteSize();
    }

    @Override
    public int bitSize() {
        return uIntType.bitSize();
    }

    @Override
    public String toString() {
        return String.valueOf(longValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractUInt that = (AbstractUInt) o;

        return this.uIntType == that.uIntType && Arrays.equals(this.getBytes(), that.getBytes());
    }

    @Override
    public int hashCode() {
        int result = uIntType.hashCode();
        result = 31 * result + Arrays.hashCode(getBytes());
        return result;
    }
}
