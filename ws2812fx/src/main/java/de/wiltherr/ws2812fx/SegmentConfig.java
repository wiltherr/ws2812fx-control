package de.wiltherr.ws2812fx;

import java.util.List;
import java.util.stream.Collectors;

public class SegmentConfig {

    private int speed;
    private Mode mode;
    private Options options;
    private List<Color> colors;


    public SegmentConfig(int speed, Mode mode, Options options, List<Color> colors) {
        if (speed > WS2812FX.SPEED_MAX || speed < WS2812FX.SPEED_MIN) {
            throw new IllegalArgumentException(
                    String.format("Speed value must be in range of %d and %d.", WS2812FX.SPEED_MIN, WS2812FX.SPEED_MAX));
        }
        this.speed = speed;
        this.mode = mode;
        this.options = options;
        this.colors = colors;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
    }

    @Override
    public String toString() {
        return "SegmentConfig{" +
                "speed=" + speed +
                ", mode=" + mode +
                ", options=" + options +
                ", colors=" + colors +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SegmentConfig that = (SegmentConfig) o;

        if (speed != that.speed) return false;
        if (mode != that.mode) return false;
        if (!options.equals(that.options)) return false;
        return colors.equals(that.colors);
    }

    @Override
    public int hashCode() {
        int result = speed;
        result = 31 * result + (mode != null ? mode.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + (colors != null ? colors.hashCode() : 0);
        return result;
    }

    public SegmentConfig copy() {
        return new SegmentConfig(speed, mode, options.copy(), colors.stream().map(Color::copy).collect(Collectors.toList()));
    }
}
