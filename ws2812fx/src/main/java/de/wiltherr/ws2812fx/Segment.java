package de.wiltherr.ws2812fx;

public class Segment implements Comparable<Segment> {

    private int index, start, stop;
    private SegmentConfig config;

    public Segment(int index, int start, int stop, SegmentConfig config) {
        this.index = index;
        this.start = start;
        this.stop = stop;
        this.config = config;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public SegmentConfig getConfig() {
        return config;
    }

    public void setConfig(SegmentConfig config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "Segment{" +
                "index=" + index +
                ", start=" + start +
                ", stop=" + stop +
                ", config=" + config +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Segment segment = (Segment) o;

        if (index != segment.index) return false;
        if (start != segment.start) return false;
        if (stop != segment.stop) return false;
        return config != null ? config.equals(segment.config) : segment.config == null;
    }

    @Override
    public int hashCode() {
        int result = index;
        result = 31 * result + start;
        result = 31 * result + stop;
        result = 31 * result + (config != null ? config.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Segment o) {
        return Integer.compare(this.getIndex(), o.getIndex());
    }

    public Segment copy() {
        return new Segment(index, start, stop, config.copy());
    }
}
