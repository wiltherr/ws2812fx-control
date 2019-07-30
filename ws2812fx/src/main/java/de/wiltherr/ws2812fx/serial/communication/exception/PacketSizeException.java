package de.wiltherr.ws2812fx.serial.communication.exception;

public class PacketSizeException extends IllegalArgumentException {
    public PacketSizeException(String message) {
        super(message);
    }
}
