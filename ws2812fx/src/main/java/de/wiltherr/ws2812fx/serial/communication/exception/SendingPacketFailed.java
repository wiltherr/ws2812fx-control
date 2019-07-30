package de.wiltherr.ws2812fx.serial.communication.exception;

public class SendingPacketFailed extends Exception {
    SendingPacketFailed(String message) {
        super(message);
    }

    public SendingPacketFailed() {
        super("Error writing bytes to serial. Maybe serial device has been shutdown or disconnected.");
    }
}
