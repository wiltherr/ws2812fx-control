package de.wiltherr.ws2812fx.serial.communication.exception;

public class SerialPacketCommunicatorOpeningException extends IllegalStateException {
    public SerialPacketCommunicatorOpeningException(String message) {
        super(message);
    }
}
