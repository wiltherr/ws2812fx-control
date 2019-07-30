package de.wiltherr.ws2812fx.serial.communication.exception;

public class ReceivingPacketTimeout extends ReceivingPacketFailed {

    public ReceivingPacketTimeout(int timeoutMs) {
        super(String.format("Reading bytes from serial device timed out after %d ms.", timeoutMs));
    }
}
