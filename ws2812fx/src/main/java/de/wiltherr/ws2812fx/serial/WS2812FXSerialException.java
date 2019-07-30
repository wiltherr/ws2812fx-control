package de.wiltherr.ws2812fx.serial;

import de.wiltherr.ws2812fx.WS2812FXException;
import de.wiltherr.ws2812fx.serial.entity.SerialFunction;

public class WS2812FXSerialException extends WS2812FXException {
    public WS2812FXSerialException(Exception cause) {
        super(cause);
    }

    public WS2812FXSerialException(String message, Exception cause) {
        super(message, cause);
    }

    public WS2812FXSerialException(SerialFunction serialFunction, String message, Exception cause) {
        super(String.format("Error calling serial function '%s' on : %s", serialFunction.getName(), message), cause);
    }

    public WS2812FXSerialException(String serialName, SerialFunction serialFunction, String message, Exception cause) {
        super(String.format("Error calling serial function '%s' on %s: %s", serialFunction.getName(), serialName, message), cause);
    }

    public WS2812FXSerialException(String message) {
        super(message);
    }

    public WS2812FXSerialException(SerialFunction serialFunction, String message) {
        super(String.format("Error calling serial function '%s': %s", serialFunction.getName(), message));
    }

    public WS2812FXSerialException(String serialName, SerialFunction serialFunction, String message) {
        super(String.format("Error calling serial function '%s' on %s: %s", serialFunction.getName(), serialName, message));
    }
}
