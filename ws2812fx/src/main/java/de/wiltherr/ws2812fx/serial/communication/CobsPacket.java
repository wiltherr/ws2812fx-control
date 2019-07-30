package de.wiltherr.ws2812fx.serial.communication;

public class CobsPacket {

    private final byte[] decodedBytes;
    private final byte[] encodedBytes;

    private CobsPacket(byte[] encodedBytes, byte[] decodedBytes) {
        this.encodedBytes = encodedBytes;
        this.decodedBytes = decodedBytes;
    }

    public static CobsPacket encode(byte[] decodedBytes) {
        return new CobsPacket(CobsUtils.encode(decodedBytes), decodedBytes);
    }

    public static CobsPacket decode(byte[] encodedBytes) {
        return new CobsPacket(encodedBytes, CobsUtils.decode(encodedBytes));
    }

    public byte[] getBytesDecoded() {
        return decodedBytes;
    }

    public byte[] getBytesEncoded() {
        return encodedBytes;
    }
}