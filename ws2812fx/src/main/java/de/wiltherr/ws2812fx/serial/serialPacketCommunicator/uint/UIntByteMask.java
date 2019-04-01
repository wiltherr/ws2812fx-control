package de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint;

import java.util.List;

public interface UIntByteMask {
    public List<UInt.Type> getUIntTypes();

    public int getByteSize();
}
