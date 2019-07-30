package de.wiltherr.ws2812fx;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualWS2812FX implements WS2812FX {

    private final LinkedHashMap<Integer, Segment> segments = new LinkedHashMap<>();
    private int brightness = WS2812FX.BRIGHTNESS_MIN;
    private int length;

    private boolean paused = false;
    private boolean stopped = false;

    public VirtualWS2812FX() {
    }

    public int getBrightness() {
        return brightness;
    }

    @Override
    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    @Override
    public void updateSegment(int segmentIndex, SegmentConfig segmentConfig) {
        if (this.segments.get(segmentIndex) == null)
            throw new IllegalArgumentException(String.format("No segment with index %d found.", segmentIndex));
        this.segments.get(segmentIndex).setConfig(segmentConfig);
    }

    @Override
    public void updateSegmentMulti(Set<Integer> segmentIndices, SegmentConfig segmentConfig) {
        for (Integer segmentIndex : segmentIndices) {
            if (this.segments.get(segmentIndex) == null)
                throw new IllegalArgumentException(String.format("No segment with index %d found.", segmentIndex));
            this.segments.get(segmentIndex).setConfig(segmentConfig);
        }
    }

    @Override
    public void updateSegmentAll(SegmentConfig segmentConfig) {
        this.segments.values().forEach(segment -> segment.setConfig(segmentConfig));
    }

    @Override
    public void resetSegments(List<Segment> segmentList) {
        this.segments.clear();
        List<Integer> indexList = segmentList.stream().map(Segment::getIndex).sorted().collect(Collectors.toList());
        long distinctSize = indexList.stream().distinct().count();
        if (distinctSize != indexList.size())
            throw new IllegalArgumentException(String.format("One ore more segment indices are not unique. IndexList: %s.", Arrays.toString(indexList.toArray())));
        segmentList.forEach(segment -> this.segments.put(segment.getIndex(), segment));
    }

    @Override
    public Segment getSegment(int segmentIndex) {
        return this.segments.get(segmentIndex);
    }

    @Override
    public List<Segment> getSegments() {
        return segments.values().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
        //stip is not stopped because resuming command in ws2812fx libary will start the strip
        stopped = false;
    }

    @Override
    public void start() {
        stopped = false;
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isStopped() {
        return stopped;
    }

    @Override
    public Integer getLength() {
        return length;
    }

    @Override
    public void setLength(int pixelCount) {
        this.length = pixelCount;
    }
}