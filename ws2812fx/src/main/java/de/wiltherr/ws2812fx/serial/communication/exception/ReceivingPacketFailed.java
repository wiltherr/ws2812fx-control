package de.wiltherr.ws2812fx.serial.communication.exception;

public class ReceivingPacketFailed extends Exception {
    public ReceivingPacketFailed(String message) {
        super(message);
    }

    public ReceivingPacketFailed() {
        super("Error reading bytes from serial. Maybe serial device has been shutdown or disconnected.");
    }
}
