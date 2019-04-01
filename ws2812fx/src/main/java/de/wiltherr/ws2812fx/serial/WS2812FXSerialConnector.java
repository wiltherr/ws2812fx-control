package de.wiltherr.ws2812fx.serial;


import de.wiltherr.ws2812fx.*;
import de.wiltherr.ws2812fx.serial.entity.EntityByteMask;
import de.wiltherr.ws2812fx.serial.entity.SegmentConfigConverter;
import de.wiltherr.ws2812fx.serial.entity.SegmentConverter;
import de.wiltherr.ws2812fx.serial.entity.SerialFunction;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.SerialPacketCommunicator;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.ByteSizesNotMatchException;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.ReceivingPacketFailed;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.SendingPacketFailed;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.uint.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class WS2812FXSerialConnector implements WS2812FX {

    public static final UInt8 ERROR_CODE = UInt.create8(255);
    public static final UInt8 FUNCTION_NOT_FOUND_CODE = UInt.create8(254);
    public static final UIntPacket ERROR_RESPONSE = UIntPacketBuilder.createOf(ERROR_CODE).build();
    public static final UIntPacket FUNCTION_NOT_FOUND_RESPONSE = UIntPacketBuilder.createOf(FUNCTION_NOT_FOUND_CODE).build();
    public static final UIntPacket STATE_STOP = UIntPacketBuilder.createOf(UInt.create8(0)).build();
    public static final UIntPacket STATE_START = UIntPacketBuilder.createOf(UInt.create8(1)).build();
    public static final UIntPacket STATE_PAUSE = UIntPacketBuilder.createOf(UInt.create8(2)).build();
    public static final UIntPacket STATE_RESUME = UIntPacketBuilder.createOf(UInt.create8(3)).build();
    private static final Logger log = LoggerFactory.getLogger(WS2812FXSerialConnector.class);
    private static int MAX_MULTI_PIXEL_COLOR_PAIRS = 30;

    private final VirtualWS2812FX state = new VirtualWS2812FX();

    private final SerialPacketCommunicator serial;

    public WS2812FXSerialConnector(int portIndex) {
        this(portIndex, 1000, 1000, 2400, 256);
    }

    public WS2812FXSerialConnector(int portIndex, int readTimeoutMs, int writeTimeoutMs, int baudRate, int serialDeviceReadBufferSize) {
        serial = new SerialPacketCommunicator(portIndex, readTimeoutMs, writeTimeoutMs, baudRate, serialDeviceReadBufferSize);
    }

    private static UIntByteMask multipleSegmentByteMask(int segmentCount) {
        //build byte mask for multiple segments
        UIntByteMaskBuilder byteMaskBuilder = UIntByteMaskBuilder.create();
        for (int i = 0; i < segmentCount; i++) {
            byteMaskBuilder.append(EntityByteMask.SEGMENT);
        }
        return byteMaskBuilder.build();
    }

//    @Override
//    public void setPixelColor(int pixelIdx, Color color) throws WS2812FXException {
//        UIntPacket params = UIntPacketBuilder
//                .createOf(UInt.valueOf(pixelIdx, UInt.Type.U_INT_16),
//                        ColorConverter.toUInt32(color))
//                .build();
//        sendRequest(SerialFunction.SET_PIXEL_COLOR, params);
//        receiveAndValidateDefaultResponse(SerialFunction.SET_PIXEL_COLOR);
//    }
//
//    @Override
//    public void setMultiPixelColor(Map<Integer, Color> pixelColorMap) throws WS2812FXException {
//        if (pixelColorMap.size() > MAX_MULTI_PIXEL_COLOR_PAIRS) {
//            throw new WS2812FXSerialConnectorException(String.format("pixelColorMap contains to many values (%d). Maximum is %d values per request.", pixelColorMap.size(), MAX_MULTI_PIXEL_COLOR_PAIRS));
//        }
//        UIntPacketBuilder builder = UIntPacketBuilder.create();
//        builder.append(UInt.create8(pixelColorMap.size())); //pairCount
//        pixelColorMap.forEach((key, value) -> builder.append(
//                UInt.valueOf(key, UInt.Type.U_INT_16), //pixelIndex
//                ColorConverter.toUInt32(value) //color
//        ));
//        pauseBeforeCallingFunction(SerialFunction.SET_MULTI_PIXEL_COLOR);
//
//        sendRequest(SerialFunction.SET_MULTI_PIXEL_COLOR, builder.build());
//        receiveAndValidateDefaultResponse(SerialFunction.SET_MULTI_PIXEL_COLOR);
//
//        resumeAfterCallingFunction(SerialFunction.SET_MULTI_PIXEL_COLOR);
//    }

    @Override
    public void setBrightness(int brightness) throws WS2812FXException {
        UIntPacket params = UIntPacketBuilder.createOf(UInt.create8(brightness)).build();
        sendRequest(SerialFunction.SET_BRIGHTNESS, params);
        receiveAndValidateDefaultResponse(SerialFunction.SET_BRIGHTNESS);
        state.setBrightness(brightness);
    }

    @Override
    public void updateSegment(int segmentIndex, SegmentConfig segmentConfig) throws WS2812FXException {
        updateSegmentMulti(new HashSet<Integer>() {{
            add(segmentIndex);
        }}, segmentConfig);
    }

    @Override
    public void updateSegmentMulti(Set<Integer> segmentIndices, SegmentConfig segmentConfig) throws WS2812FXException {
        UIntPacket params = UIntPacketBuilder
                .createOf(SegmentConfigConverter.toUIntPacket(segmentConfig))
                .append(UInt.create8(segmentIndices.size())) //numSegments
                .append(
                        segmentIndices.stream()
                                .map(UInt::create8)
                                .collect(Collectors.toList()))
                .build();
        pauseBeforeCallingFunction(SerialFunction.UPDATE_SEGMENT_CONFIG_MULTI);

        sendRequest(SerialFunction.UPDATE_SEGMENT_CONFIG_MULTI, params);
        receiveAndValidateDefaultResponse(SerialFunction.UPDATE_SEGMENT_CONFIG_MULTI);
        state.updateSegmentMulti(segmentIndices, segmentConfig);

        resumeAfterCallingFunction(SerialFunction.UPDATE_SEGMENT_CONFIG_MULTI);
    }

    @Override
    public void updateSegmentAll(SegmentConfig segmentConfig) throws WS2812FXException {
        UIntPacket params = SegmentConfigConverter.toUIntPacket(segmentConfig);
        pauseBeforeCallingFunction(SerialFunction.UPDATE_SEGMENT_CONFIG_ALL);
        sendRequest(SerialFunction.UPDATE_SEGMENT_CONFIG_ALL, params);
        receiveAndValidateDefaultResponse(SerialFunction.UPDATE_SEGMENT_CONFIG_ALL);
        state.updateSegmentAll(segmentConfig);
        resumeAfterCallingFunction(SerialFunction.UPDATE_SEGMENT_CONFIG_ALL);
    }

    @Override
    public void resetSegments(List<Segment> segments) throws WS2812FXException {
        UIntPacketBuilder builder = UIntPacketBuilder.createOf(
                UInt.create8(segments.size()) //numSegments
        );
        segments.stream()
                .map(SegmentConverter::toUIntPacket)
                .forEach(builder::append);
        UIntPacket params = builder.build();
        pauseBeforeCallingFunction(SerialFunction.RESET_SEGMENTS);
        sendRequest(SerialFunction.RESET_SEGMENTS, params);
        receiveAndValidateDefaultResponse(SerialFunction.RESET_SEGMENTS);
        state.resetSegments(segments);
        resumeAfterCallingFunction(SerialFunction.RESET_SEGMENTS);
    }

    @Override
    public Integer getLength() throws WS2812FXException {
        if (state.getLength() == null) {
            state.setLength(getLengthSerialDevice());
        }
        return state.getLength();
    }

    @Override
    public void setLength(int pixelCount) throws WS2812FXException {
        UIntPacket params = UIntPacketBuilder.createOf(UInt.create16(pixelCount)).build();
        pauseBeforeCallingFunction(SerialFunction.SET_LENGTH);
        sendRequest(SerialFunction.SET_LENGTH, params);
        receiveAndValidateDefaultResponse(SerialFunction.SET_LENGTH);
        state.setLength(pixelCount);
    }

    private Integer getLengthSerialDevice() throws WS2812FXException {
        UIntPacket params = UIntPacketBuilder.create().build(); //empty params
        pauseBeforeCallingFunction(SerialFunction.GET_LENGTH);
        sendRequest(SerialFunction.GET_LENGTH, params);
        UIntPacket response = receiveResponse(SerialFunction.GET_LENGTH, EntityByteMask.LENGTH);
        UInt length = response.getList().get(0);
        resumeAfterCallingFunction(SerialFunction.GET_SEGMENT);
        return (int) length.toLong();
    }

    @Override
    public Segment getSegment(int segmentIndex) throws WS2812FXException {
        if (state.getSegment(segmentIndex) != null) {
            return state.getSegment(segmentIndex);
        }

        for (Segment segment : getSegments()) {
            if (segment.getIndex() == segmentIndex)
                return segment;
        }

        throw new IllegalArgumentException(String.format("No segment with index %d found.", segmentIndex));
    }

    @Override
    public List<Segment> getSegments() throws WS2812FXException {
        if (state.getSegments().isEmpty()) {
            List<Segment> serialDeviceSegments = getSegmentsSerialDevice();
            //update state
            state.resetSegments(serialDeviceSegments);
        }
        return state.getSegments();
    }

    public List<Segment> getSegmentsSerialDevice() throws WS2812FXException {
        UIntPacket emptyParams = UIntPacketBuilder.create().build();
        pauseBeforeCallingFunction(SerialFunction.GET_SEGMENTS);
        sendRequest(SerialFunction.GET_SEGMENTS, emptyParams);

        try {
            byte[] responseBytes = serial.receiveBytePacket();
            //calculate response segment count with segment byte mask
            int segmentCount = responseBytes.length / EntityByteMask.SEGMENT.getByteSize();

            //convert responseByte to uIntPacket
            UIntPacket response = UIntPacketBuilder.buildFromByteMask(multipleSegmentByteMask(segmentCount), responseBytes, ByteOrder.LITTLE_ENDIAN);
            resumeAfterCallingFunction(SerialFunction.GET_SEGMENTS);
            return SegmentConverter.fromUIntPacketToMultiSegment(response);
        } catch (ReceivingPacketFailed receivingPacketFailed) {
            throw new WS2812FXSerialConnectorException(serial, SerialFunction.GET_SEGMENTS,
                    "Failed to receive response from serial device.",
                    receivingPacketFailed
            );
        } catch (ByteSizesNotMatchException byteSizeNotMatchException) {
            throw new WS2812FXSerialConnectorException(serial, SerialFunction.GET_SEGMENTS,
                    "Response value of serial device not match the given byteMask.",
                    byteSizeNotMatchException
            );
        }
    }

    @Override
    public void pause() throws WS2812FXException {
        sendRequest(SerialFunction.SET_STATE, STATE_PAUSE);
        receiveAndValidateDefaultResponse(SerialFunction.SET_STATE);
        state.pause();
    }

    @Override
    public void resume() throws WS2812FXException {
        sendRequest(SerialFunction.SET_STATE, STATE_RESUME);
        receiveAndValidateDefaultResponse(SerialFunction.SET_STATE);
        state.resume();
    }

    @Override
    public void start() throws WS2812FXException {
        sendRequest(SerialFunction.SET_STATE, STATE_START);
        receiveAndValidateDefaultResponse(SerialFunction.SET_STATE);
        state.start();
    }

    @Override
    public void stop() throws WS2812FXException {
        sendRequest(SerialFunction.SET_STATE, STATE_STOP);
        receiveAndValidateDefaultResponse(SerialFunction.SET_STATE);
        state.stop();
    }

    private void pauseBeforeCallingFunction(SerialFunction serialFunction) throws WS2812FXSerialConnectorException {
        try {
            log.debug("[{}] Pausing serial device before sending request", serialFunction.getName());
            this.pause();
            log.debug("[{}] Paused successfully", serialFunction.getName());
        } catch (WS2812FXException e) {
            throw new WS2812FXSerialConnectorException(
                    String.format("Failed to send pause request before calling %s function. ",
                            serialFunction.getName()),
                    e
            );
        }
    }

    private void resumeAfterCallingFunction(SerialFunction serialFunction) throws WS2812FXSerialConnectorException {
        try {
            log.debug("[{}] Resuming serial device after finishing request", serialFunction.getName());
            this.resume();
            log.debug("[{}] Resumed successfully", serialFunction.getName());
        } catch (WS2812FXException e) {
//            throw new WS2812FXSerialConnectorException(String.format("Failed to send resume request after calling %s function. ", serialFunction.getName()), e);
        }
    }

    private void sendRequest(SerialFunction serialFunction, UIntPacket parameters) throws WS2812FXException {
        UIntPacket request = UIntPacketBuilder
                .createOf(UInt.create8(serialFunction.getNumber()))
                .append(parameters)
                .build();
        try {
            log.debug("[{}] Sending packet to serial device: {}", serialFunction.getName(), request.toStringWithUIntTypes());
            serial.sendBytePacket(
                    request.getBytesLittleEndian()
            );
            log.debug("[{}] Packet successfully sent", serialFunction.getName());
        } catch (SendingPacketFailed sendingPacketFailed) {
            throw new WS2812FXSerialConnectorException(serial, serialFunction,
                    "Failed to send request to serial device.",
                    sendingPacketFailed
            );
        }
    }

    private UIntPacket receiveResponse(SerialFunction serialFunction, UIntByteMask responseByteMask) throws WS2812FXException {
        try {
            log.debug("[{}] Waiting for response from serial device", serialFunction.getName());
            byte[] bytes = serial.receiveBytePacket();
            log.debug("[{}] Response received", serialFunction.getName());
            if (bytes.length == EntityByteMask.DEFAULT_RESPONSE.getByteSize()) {
                UIntPacket defaultResponse = UIntPacketBuilder.buildFromByteMask(EntityByteMask.DEFAULT_RESPONSE, bytes, ByteOrder.LITTLE_ENDIAN);
                if (ERROR_RESPONSE.equals(defaultResponse)) {
                    throw new WS2812FXSerialConnectorException(serial, serialFunction, "Serial device responded with error response.");
                } else if (FUNCTION_NOT_FOUND_RESPONSE.equals(defaultResponse)) {
                    throw new WS2812FXSerialConnectorException(serial, serialFunction, String.format("No function with number %d could be found.", serialFunction.getNumber()));
                }
            }
            UIntPacket responsePacket = UIntPacketBuilder.buildFromByteMask(responseByteMask, bytes, ByteOrder.LITTLE_ENDIAN);
            log.debug("[{}] Response successfully parsed: {}", serialFunction.getName(), responsePacket.toStringWithUIntTypes());
            return responsePacket;
        } catch (ReceivingPacketFailed receivingPacketFailed) {
            throw new WS2812FXSerialConnectorException(serial, serialFunction,
                    "Failed to receive response from serial device.",
                    receivingPacketFailed
            );
        } catch (ByteSizesNotMatchException e) {
            throw new WS2812FXSerialConnectorException(serial, serialFunction,
                    "Response of serial device not match the given byteMask.",
                    e
            );
        }
    }

    private void receiveAndValidateDefaultResponse(SerialFunction serialFunction) throws WS2812FXException {
        UIntPacket defaultResponse = receiveResponse(serialFunction, EntityByteMask.DEFAULT_RESPONSE);
        validateDefaultResponse(serialFunction, defaultResponse);
    }

    private void validateDefaultResponse(SerialFunction serialFunction, UIntPacket responsePacket) throws WS2812FXException {
        if (EntityByteMask.DEFAULT_RESPONSE.getUIntTypes().size() == responsePacket.getList().size()) {
            UIntPacket functionCalledResponse = UIntPacketBuilder.createOf(UInt.create8(serialFunction.getNumber())).build();
            if (functionCalledResponse.equals(responsePacket)) {
                return;
            }
        }
        throw new WS2812FXSerialConnectorException(serial, serialFunction, String.format("Serial device responded with undefined response. Response was: %s", responsePacket.toString()));
    }
}
