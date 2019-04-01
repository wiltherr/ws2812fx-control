package de.wiltherr.ws2812fx;

public class WS2812FXException extends Exception {
    public WS2812FXException(Exception cause) {
        super(cause);
    }

    public WS2812FXException(String message, Exception cause) {
        super(message, cause);
    }

    public WS2812FXException(String message) {
        super(message);
    }
}
