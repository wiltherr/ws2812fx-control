package de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UIntByteMaskImpl implements UIntByteMask {

    private List<UInt.Type> uIntTypes;

    private UIntByteMaskImpl() {
        uIntTypes = new ArrayList<>();
    }

    UIntByteMaskImpl(List<UInt.Type> uIntTypes) {
        this.uIntTypes = uIntTypes;
    }

    @Override
    public List<UInt.Type> getUIntTypes() {
        return Collections.unmodifiableList(uIntTypes);
    }

    @Override
    public int getByteSize() {
        return uIntTypes.stream()
                .map(UInt.Type::byteSize)
                .mapToInt(Integer::valueOf)
                .sum();
    }

}
