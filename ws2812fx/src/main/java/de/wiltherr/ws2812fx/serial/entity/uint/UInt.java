package de.wiltherr.ws2812fx.serial.entity.uint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import static de.wiltherr.ws2812fx.serial.entity.uint.UInt.Type.*;

public interface UInt {

    public static UInt ofByteArray(byte[] byteArray) { //, ByteOrder byteOrder
        Objects.requireNonNull(byteArray, "Byte array must be not null.");

        if (byteArray.length == U_INT_8.byteSize()) {
            return new UInt8(byteArray);
        } else if (byteArray.length == U_INT_16.byteSize()) {
            return new UInt16(byteArray);
        } else if (byteArray.length == U_INT_32.byteSize()) {
            return new UInt32(byteArray);
        } else {
            throw new IllegalArgumentException("Byte array length was " + byteArray.length + " but must be of length 1, 2 or 4.");
        }
    }

    public static UInt ofByteArrayLittleEndian(byte[] byteArray) {
        Objects.requireNonNull(byteArray, "Byte array must be not null.");


        ByteBuffer bb = ByteBuffer.wrap(byteArray); //TODO add constructor to uints instead
        bb.order(ByteOrder.LITTLE_ENDIAN);


        if (byteArray.length == U_INT_8.byteSize()) {
            return new UInt8(byteArray);
        } else if (byteArray.length == U_INT_16.byteSize()) {
            char bytes = bb.getChar(); //char = 2 bytes
            long value = Integer.toUnsignedLong(bytes);
            return new UInt16((int) value);
        } else if (byteArray.length == U_INT_32.byteSize()) {
            int bytes = bb.getInt(); //short = 4 bytes
            long value = Integer.toUnsignedLong(bytes);
            return new UInt32(value);
        } else {
            throw new IllegalArgumentException("Byte array length was " + byteArray.length + " but must be of length 1, 2 or 4.");
        }
    }

    public static UInt createAuto(long uInt) {
        if (uInt <= U_INT_8.maxValue()) {
            return create8((int) uInt);
        } else if (uInt <= U_INT_16.MAX_VALUE) {
            return create16((int) uInt);
        } else {
            return create32(uInt);
        }
    }

    public static UInt create(long uInt, int bitSize) {
        if (bitSize == U_INT_8.bitSize()) {
            return create8((int) uInt);
        } else if (bitSize == U_INT_16.bitSize()) {
            return create16((int) uInt);
        } else if (bitSize == U_INT_32.bitSize()) {
            return create32(uInt);
        } else {
            throw new IllegalArgumentException(String.format("Can not convert to %d bit unsigned integer. Only %d, %d or %d bit are allowed.", bitSize, U_INT_8.bitSize(), U_INT_16.bitSize(), U_INT_32.bitSize()));
        }
    }

    public static UInt create(long uInt, Type type) {
        return create(uInt, type.BIT_SIZE);
    }

    public static UInt8 create8(int uInt8) {
        return UInt8.valueOf(uInt8);
    }

    public static UInt16 create16(int uInt16) {
        return UInt16.valueOf(uInt16);
    }

    public static UInt32 create32(long uInt32) {
        return UInt32.valueOf(uInt32);
    }

    public long toLong();

    public long longValue();

    public byte[] getBytes();

    public byte[] getBytesLittleEndian();

    public byte[] getBytesBigEndian();

    public Type getType();

    public int byteSize();

    public int bitSize();

    public String toString();

    enum Type {
        U_INT_8(UInt8.BIT_SIZE, UInt8.BYTE_SIZE, UInt8.MAX_VALUE),
        U_INT_16(UInt16.BIT_SIZE, UInt16.BYTE_SIZE, UInt16.MAX_VALUE),
        U_INT_32(UInt32.BIT_SIZE, UInt32.BYTE_SIZE, UInt32.MAX_VALUE);

        public final int BIT_SIZE, BYTE_SIZE;
        public final long MAX_VALUE;

        Type(int bitSize, int byteSize, long maxValue) {
            this.BIT_SIZE = bitSize;
            this.BYTE_SIZE = byteSize;
            this.MAX_VALUE = maxValue;
        }

        public final int bitSize() {
            return BIT_SIZE;
        }

        public final int byteSize() {
            return BYTE_SIZE;
        }

        public long maxValue() {
            return MAX_VALUE;
        }
    }
}
