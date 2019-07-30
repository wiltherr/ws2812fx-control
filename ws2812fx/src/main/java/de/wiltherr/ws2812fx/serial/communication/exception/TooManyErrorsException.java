package de.wiltherr.ws2812fx.serial.communication.exception;

public class TooManyErrorsException extends RuntimeException {
    public TooManyErrorsException(String message) {
        super(message);
    }
}
