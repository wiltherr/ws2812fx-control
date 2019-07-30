package de.wiltherr.ws2812fx.serial.entity.uint;

public class UInt16 extends AbstractUInt {

    public static final int MAX_VALUE = 65535;
    public static final int BIT_SIZE = 16;
    public static final int BYTE_SIZE = 2;
    private byte[] bytes;

    UInt16(byte[] byteArray) {
        super(byteArray, Type.U_INT_16);
        bytes = byteArray;
    }

    UInt16(int uInt) {
        super(uInt, Type.U_INT_16);
        bytes = new byte[]{
                (byte) (uInt >> 8),
                (byte) uInt};
    }

    public static UInt16 valueOf(int uInt) {
        return new UInt16(uInt);
    }

    public boolean isValidUInt16(long uInt) {
        return isValidUInt(uInt);
    }

    @Override
    public long toLong() {
        return asInt();
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    private int asInt() {
        //interpret bytes as unsigned 16 bit integer (BigEndian)
        return bytes[1] & 0xFF | (bytes[0] & 0xFF) << 8;
    }
}
