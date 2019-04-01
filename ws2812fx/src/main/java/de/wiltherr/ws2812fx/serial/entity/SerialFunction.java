package de.wiltherr.ws2812fx.serial.entity;

public enum SerialFunction {
    SET_STATE(1, "SetState"), //
    UPDATE_SEGMENT_CONFIG(2, "UpdateSegmentConfig"), //
    UPDATE_SEGMENT_CONFIG_ALL(3, "UpdateSegmentConfigAll"), //
    RESET_SEGMENTS(4, "ResetSegments"), //
    GET_SEGMENTS(5, "GetSegments"), //
    GET_SEGMENT(6, "GetSegment"), //
    SET_PIXEL_COLOR(7, "SetPixelColor"), //
    SET_MULTI_PIXEL_COLOR(8, "SetMultiPixelColor"), //
    SET_BRIGHTNESS(9, "SetBrightness"), //
    UPDATE_SEGMENT_CONFIG_MULTI(10, "UpdateSegmentConfigMulti"), GET_LENGTH(11, "GetLength"), SET_LENGTH(12, "SetLength");

    private final int number;
    private final String name;

    SerialFunction(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }
}
