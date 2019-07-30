package de.wiltherr.ws2812fx.app.service;


import de.wiltherr.ws2812fx.*;
import de.wiltherr.ws2812fx.serial.WS2812FXSerial;
import de.wiltherr.ws2812fx.serial.communication.SerialPacketCommunicator;
import de.wiltherr.ws2812fx.serial.communication.SerialPacketCommunicatorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WS2812FXService implements WS2812FX {

    public static final SegmentConfig DEFAULT_CONFIG = new SegmentConfig(1000, Mode.STATIC, new Options(false, false, FadeOption.NO_OPTION),
            Arrays.asList(new Color(255, 0, 0, 0), new Color(255, 0, 0, 0), new Color(255, 0, 0, 0)));
    private static final Logger log = LoggerFactory.getLogger(WS2812FXService.class);
    private WS2812FX delegate;
    private ThreadPoolTaskExecutor executor;
    private SerialPacketCommunicator serialPacketCommunicator;

    @PostConstruct
    public void init() throws InterruptedException, WS2812FXException {
        this.serialPacketCommunicator = new SerialPacketCommunicatorImpl(1, 1000, 1000, 115200, 256);

        delegate = new WS2812FXSerial(serialPacketCommunicator);

        executor = new ThreadPoolTaskExecutor();
        executor.initialize();
        restartSerial();



        //Thread.sleep(2000);
        //this.updateSegmentAll(DEFAULT_CONFIG);

    }

    @PreDestroy
    public void clear() {
        serialPacketCommunicator.stop();
    }

    @Override
    public void setBrightness(int brightness) throws WS2812FXException {
        restartSerial();
        delegate.setBrightness(brightness);
        log.debug("Brightness changed: " + brightness);
    }

    @Override
    public void updateSegment(int segmentIndex, SegmentConfig segmentConfig) throws WS2812FXException {
        restartSerial();
        delegate.updateSegment(segmentIndex, segmentConfig);
        log.debug("Updated segment (" + segmentIndex + ") config: " + segmentConfig);
    }

    @Override
    public void updateSegmentMulti(Set<Integer> segmentIndices, SegmentConfig segmentConfig) throws WS2812FXException {
        restartSerial();
        delegate.updateSegmentMulti(segmentIndices, segmentConfig);
        log.debug(
                "Updated multi segments "
                        + segmentIndices.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"))
                        + " config: " + segmentConfig
        );
    }

    @Override
    public void updateSegmentAll(SegmentConfig segmentConfig) throws WS2812FXException {
        restartSerial();
        delegate.updateSegmentAll(segmentConfig);
        log.debug("Updated all segments config: " + segmentConfig);
    }

    @Override
    public void resetSegments(List<Segment> segments) throws WS2812FXException {
        restartSerial();
        delegate.resetSegments(segments);
        log.debug("Reset segments");
    }

    @Override
    public Integer getLength() throws WS2812FXException {
        restartSerial();
        return delegate.getLength();
    }

    @Override
    public void setLength(int pixelCount) throws WS2812FXException {
        restartSerial();
        delegate.setLength(pixelCount);
    }

    @Override
    public Segment getSegment(int segmentIdx) throws WS2812FXException {
        restartSerial();
        return delegate.getSegment(segmentIdx);
    }

    @Override
    public List<Segment> getSegments() throws WS2812FXException {
        restartSerial();
        return delegate.getSegments();
    }

    @Override
    public void pause() throws WS2812FXException {
        restartSerial();
        delegate.pause();
    }

    @Override
    public void resume() throws WS2812FXException {
        restartSerial();
        delegate.resume();
    }

    @Override
    public void start() throws WS2812FXException {
        restartSerial();
        delegate.start();
    }

    @Override
    public void stop() throws WS2812FXException {
        restartSerial();
        delegate.stop();
    }

    private void restartSerial() {
        if (!serialPacketCommunicator.isRunning()) {
            executor.execute(serialPacketCommunicator);
        }
    }
}
