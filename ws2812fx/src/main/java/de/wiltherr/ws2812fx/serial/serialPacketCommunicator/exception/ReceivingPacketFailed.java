package de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception;

public class ReceivingPacketFailed extends Exception {
    ReceivingPacketFailed(String message) {
        super(message);
    }

    public ReceivingPacketFailed() {
        super("Error reading bytes from serial. Maybe serial device has been shutdown or disconnected.");
    }
}
