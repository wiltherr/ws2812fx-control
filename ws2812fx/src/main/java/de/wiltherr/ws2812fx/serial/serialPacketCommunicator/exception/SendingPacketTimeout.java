package de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception;

public class SendingPacketTimeout extends SendingPacketFailed {
    public SendingPacketTimeout(int timeoutMs) {
        super(String.format("Writing bytes to serial device timed out after %d ms.", timeoutMs));
    }
}
