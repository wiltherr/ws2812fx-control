package de.wiltherr.ws2812fx.serial.entity.uint;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class UInt32 extends AbstractUInt {

    public static final long MAX_VALUE = 0xffffffffL; //= 4294967295
    public static final int BIT_SIZE = 32;
    public static final int BYTE_SIZE = 4;
    private byte[] bytes;

    UInt32(byte[] byteArray) {
        super(byteArray, Type.U_INT_32);
        bytes = byteArray;
    }

    UInt32(long uInt) {
        super(uInt, Type.U_INT_32);
        //source codeflush.dev https://stackoverflow.com/a/44853833
        this.bytes = new byte[Long.BYTES];
        ByteBuffer.wrap(this.bytes).putLong(uInt);
        this.bytes = Arrays.copyOfRange(this.bytes, 4, Long.BYTES); //the last four bytes
    }

    public static UInt32 valueOf(long uInt) {
        return new UInt32(uInt);
    }

    public boolean isUInt32(long uInt) {
        return isValidUInt(uInt);
    }

    @Override
    public long toLong() {
        //source codeflush.dev https://stackoverflow.com/a/44853833
        ByteBuffer buffer = ByteBuffer.allocate(8).put(new byte[]{0, 0, 0, 0}).put(bytes);
        buffer.position(0);
        return buffer.getLong();
    }

    @Override
    public byte[] getBytes() {
        return bytes;
    }
}
