package de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint;

public class UInt8 extends AbstractUInt {

    public static final long MAX_VALUE = 255;
    public static final int BIT_SIZE = 8;
    public static final int BYTE_SIZE = 1;
    private final byte[] bytes;

    UInt8(byte[] byteArray) {
        super(byteArray, Type.U_INT_8);
        bytes = byteArray;
    }

    UInt8(int uInt) {
        super(uInt, Type.U_INT_8);
        bytes = new byte[]{(byte) uInt};
    }

    public static UInt8 valueOf(int uInt) {
        return new UInt8(uInt);
    }

    public boolean isValidUInt8(long uInt) {
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

    @Override
    public byte[] getBytesLittleEndian() {
        return bytes;
        /*ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt((int) asInt());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        byte[] buffer = new byte[byteSize()];
        System.arraycopy(bb.array(), 0, buffer, 0, 1);
        //bb.get(buffer, 0, 0);
        return buffer; //TODO
*/
    }

    private int asInt() {
        //interpret byte as unsigned 8 bit integer (BigEndian)
        return Byte.toUnsignedInt(bytes[0]);
    }
}
