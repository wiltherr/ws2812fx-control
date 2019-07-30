package de.wiltherr.ws2812fx.serial.entity.uint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UIntByteMaskBuilder {


    private List<UInt.Type> uIntTypes = new ArrayList<>();

    private UIntByteMaskBuilder() {
    }

    public static UIntByteMaskBuilder createOf(UIntByteMask... uIntByteMasks) {
        return new UIntByteMaskBuilder().append(uIntByteMasks);
    }

    public static UIntByteMaskBuilder createOf(UInt.Type... uIntTypes) {
        return new UIntByteMaskBuilder().append(uIntTypes);
    }

    public static UIntByteMaskBuilder createOf(List<UInt.Type> uIntTypes) {
        return new UIntByteMaskBuilder().append(uIntTypes);
    }

    public static UIntByteMaskBuilder create() {
        return new UIntByteMaskBuilder();
    }

    public UIntByteMaskBuilder append(UInt.Type... uIntTypes) {
        this.uIntTypes.addAll(Arrays.asList(uIntTypes));
        return this;
    }

    public UIntByteMaskBuilder append(UIntByteMask... uIntByteMasks) {
        uIntTypes.addAll(
                Arrays.stream(uIntByteMasks)
                        .flatMap(uIntByteMask -> uIntByteMask.getUIntTypes().stream())
                        .collect(Collectors.toList())
        );
        return this;
    }

    public UIntByteMaskBuilder append(List<UInt.Type> uIntTypes) {
        this.uIntTypes.addAll(uIntTypes);
        return this;
    }

    public UIntByteMaskImpl build() {
        return new UIntByteMaskImpl(uIntTypes);
    }

}
