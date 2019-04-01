package de.wiltherr.ws2812fx.serial.entity;


import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UIntByteMask;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UIntByteMaskBuilder;

import java.util.Arrays;
import java.util.List;

import static de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UInt.Type.*;
import static de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.UIntByteMaskBuilder.createOf;

public enum EntityByteMask implements UIntByteMask {

    DEFAULT_RESPONSE(U_INT_8),
    PIXEL_INDEX(U_INT_16),
    LENGTH(U_INT_16),
    START(U_INT_16),
    STOP(U_INT_16),
    SPEED(U_INT_16),
    MODE(U_INT_8),
    OPTIONS(U_INT_8),
    COLOR(U_INT_32),
    SEGMENT_CONFIG(SPEED, MODE, OPTIONS, COLOR, COLOR, COLOR),
    SEGMENT(START, STOP, SEGMENT_CONFIG);

    private UIntByteMask byteMask;

    EntityByteMask(UIntByteMaskBuilder byteMaskBuilder) {
        this.byteMask = byteMaskBuilder.build();
    }

    EntityByteMask(UInt.Type uIntType) {
        this(createOf(uIntType));
    }

    EntityByteMask(EntityByteMask... byteMasks) {
        UIntByteMaskBuilder builder = UIntByteMaskBuilder.create();
        Arrays.stream(byteMasks).forEach(builder::append);
        byteMask = builder.build();
    }

    public UIntByteMask getByteMask() {
        return byteMask;
    }

    @Override
    public List<UInt.Type> getUIntTypes() {
        return byteMask.getUIntTypes();
    }

    @Override
    public int getByteSize() {
        return byteMask.getByteSize();
    }
}
