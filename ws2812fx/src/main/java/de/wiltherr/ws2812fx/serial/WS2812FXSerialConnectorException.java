package de.wiltherr.ws2812fx.serial;

import de.wiltherr.ws2812fx.WS2812FXException;
import de.wiltherr.ws2812fx.serial.entity.SerialFunction;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.SerialPacketCommunicator;

public class WS2812FXSerialConnectorException extends WS2812FXException {
    public WS2812FXSerialConnectorException(Exception cause) {
        super(cause);
    }

    public WS2812FXSerialConnectorException(String message, Exception cause) {
        super(message, cause);
    }

    public WS2812FXSerialConnectorException(SerialFunction serialFunction, String message, Exception cause) {
        super(String.format("Error calling serial function '%s' on : %s", serialFunction.getName(), message), cause);
    }

    public WS2812FXSerialConnectorException(SerialPacketCommunicator spc, SerialFunction serialFunction, String message, Exception cause) {
        super(String.format("Error calling serial function '%s' on %s: %s", serialFunction.getName(), spc.toString(), message), cause);
    }

    public WS2812FXSerialConnectorException(String message) {
        super(message);
    }

    public WS2812FXSerialConnectorException(SerialFunction serialFunction, String message) {
        super(String.format("Error calling serial function '%s': %s", serialFunction.getName(), message));
    }

    public WS2812FXSerialConnectorException(SerialPacketCommunicator spc, SerialFunction serialFunction, String message) {
        super(String.format("Error calling serial function '%s' on %s: %s", serialFunction.getName(), spc.toString(), message));
    }
}
