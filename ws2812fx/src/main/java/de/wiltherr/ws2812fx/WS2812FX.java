package de.wiltherr.ws2812fx;

import java.util.List;
import java.util.Set;

public interface WS2812FX {
    int SPEED_MIN = 10;
    int SPEED_MAX = 65535;
    int BRIGHTNESS_MIN = 0;
    int BRIGHTNESS_MAX = 255;

    int MAX_NUM_SEGMENTS = 10;
    int COLORS_PER_SEGMENT = 3;

    void setBrightness(int brightness) throws WS2812FXException;

    //    public void setPixelColor(int pixelIdx, Color color) throws WS2812FXException;
//    public void setMultiPixelColor(Map<Integer,Color> pixelColorMap) throws WS2812FXException;
    void updateSegment(int segmentIndex, SegmentConfig segmentConfig) throws WS2812FXException;

    void updateSegmentMulti(Set<Integer> segmentIndices, SegmentConfig segmentConfig) throws WS2812FXException;

    public void updateSegmentAll(SegmentConfig segment) throws WS2812FXException;

    public void resetSegments(List<Segment> segments) throws WS2812FXException;

    public Integer getLength() throws WS2812FXException;

    public void setLength(int pixelCount) throws WS2812FXException;

    public Segment getSegment(int segmentIdx) throws WS2812FXException;

    public List<Segment> getSegments() throws WS2812FXException;

    public void pause() throws WS2812FXException;

    public void resume() throws WS2812FXException;

    public void start() throws WS2812FXException;

    public void stop() throws WS2812FXException;

    /* TODO: Das hier auch??
    public void setSegmentMode(int segmentIndex, Mode mode);
    public void setSegmentColors(int segmentIndex, List<Color> colors);
    public void setSegmentSpeed(int segmentIndex, int speed);
    */


}
