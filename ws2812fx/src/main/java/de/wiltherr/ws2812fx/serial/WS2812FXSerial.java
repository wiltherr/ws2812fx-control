package de.wiltherr.ws2812fx.serial;


import de.wiltherr.ws2812fx.*;
import de.wiltherr.ws2812fx.serial.communication.ByteArrayUtils;
import de.wiltherr.ws2812fx.serial.communication.SerialPacketCommunicator;
import de.wiltherr.ws2812fx.serial.entity.ByteMask;
import de.wiltherr.ws2812fx.serial.entity.SegmentConfigConverter;
import de.wiltherr.ws2812fx.serial.entity.SegmentConverter;
import de.wiltherr.ws2812fx.serial.entity.SerialFunction;
import de.wiltherr.ws2812fx.serial.entity.uint.*;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.FailsafeException;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteOrder;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class WS2812FXSerial implements WS2812FX {

    public static final UInt8 ERROR_CODE = UInt.create8(255);
    public static final UInt8 FUNCTION_NOT_FOUND_CODE = UInt.create8(254);
    public static final UIntPacket ERROR_PACKET = UIntPacketBuilder.createOf(ERROR_CODE).build();
    public static final UIntPacket FUNCTION_NOT_FOUND_PACKET = UIntPacketBuilder.createOf(FUNCTION_NOT_FOUND_CODE).build();
    public static final UIntPacket STATE_STOP_PACKET = UIntPacketBuilder.createOf(UInt.create8(0)).build();
    public static final UIntPacket STATE_START_PACKET = UIntPacketBuilder.createOf(UInt.create8(1)).build();
    public static final UIntPacket STATE_PAUSE_PACKET = UIntPacketBuilder.createOf(UInt.create8(2)).build();
    public static final UIntPacket STATE_RESUME_PACKET = UIntPacketBuilder.createOf(UInt.create8(3)).build();

    public static final UInt8 SERIAL_DEVICE_LISTENING_CODE = UInt.create8(155);
    public static final UIntPacket SERIAL_DEVICE_LISTENING_PACKET = UIntPacketBuilder.createOf(SERIAL_DEVICE_LISTENING_CODE).build();

    private static final Logger log = LoggerFactory.getLogger(WS2812FXSerial.class);
    private static int MAX_MULTI_PIXEL_COLOR_PAIRS = 30;

    private final VirtualWS2812FX state = new VirtualWS2812FX();

    private final SerialPacketCommunicator serial;

    final private RetryPolicy<Object> retryPolicy = new RetryPolicy<Object>()
            .handle(WS2812FXException.class, RuntimeException.class)
            .withDelay(Duration.ofMillis(20))
            .withMaxRetries(3)
            .onRetry(executionAttemptedEvent ->
                    log.warn("Error processing request. Retying request now (attempt counter: {}). Exception message: {}",
                            executionAttemptedEvent.getAttemptCount(), executionAttemptedEvent.getLastFailure().getLocalizedMessage())
            ).onRetriesExceeded(executionCompletedEvent ->
                    log.error("Error processing request. All retries exceeded. Exception message: {}",
                            executionCompletedEvent.getFailure().getLocalizedMessage()));

    public WS2812FXSerial(SerialPacketCommunicator serial) {
        this.serial = serial;
    }

//    @Override
//    public void setPixelColor(int pixelIdx, Color color) throws WS2812FXException {
//        UIntPacket params = UIntPacketBuilder
//                .createOf(UInt.valueOf(pixelIdx, UInt.Type.U_INT_16),
//                        ColorConverter.toUInt32(color))
//                .build();
//        requestFunctionSerialDeviceDefault(SerialFunction.SET_PIXEL_COLOR, params);
//        receiveAndValidateDefaultResponse(SerialFunction.SET_PIXEL_COLOR);
//    }
//
//    @Override
//    public void setMultiPixelColor(Map<Integer, Color> pixelColorMap) throws WS2812FXException {
//        if (pixelColorMap.size() > MAX_MULTI_PIXEL_COLOR_PAIRS) {
//            throw new WS2812FXSerialException(String.format("pixelColorMap contains to many values (%d). Maximum is %d values per request.", pixelColorMap.size(), MAX_MULTI_PIXEL_COLOR_PAIRS));
//        }
//        UIntPacketBuilder builder = UIntPacketBuilder.create();
//        builder.append(UInt.create8(pixelColorMap.size())); //pairCount
//        pixelColorMap.forEach((key, value) -> builder.append(
//                UInt.valueOf(key, UInt.Type.U_INT_16), //pixelIndex
//                ColorConverter.toUInt32(value) //color
//        ));
//        pauseBeforeCallingFunction(SerialFunction.SET_MULTI_PIXEL_COLOR);
//
//        requestFunctionSerialDeviceDefault(SerialFunction.SET_MULTI_PIXEL_COLOR, builder.build());
//        receiveAndValidateDefaultResponse(SerialFunction.SET_MULTI_PIXEL_COLOR);
//
//        resumeAfterCallingFunction(SerialFunction.SET_MULTI_PIXEL_COLOR);
//    }

    @Override
    public void setBrightness(int brightness) throws WS2812FXException {
        UIntPacket params = UIntPacketBuilder.createOf(UInt.create8(brightness)).build();
        requestFunctionSerialDeviceDefault(SerialFunction.SET_BRIGHTNESS, params);
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


        requestFunctionSerialDeviceDefault(SerialFunction.UPDATE_SEGMENT_CONFIG_MULTI, params);

        state.updateSegmentMulti(segmentIndices, segmentConfig);
    }

    @Override
    public void updateSegmentAll(SegmentConfig segmentConfig) throws WS2812FXException {
        UIntPacket params = SegmentConfigConverter.toUIntPacket(segmentConfig);
        requestFunctionSerialDeviceDefault(SerialFunction.UPDATE_SEGMENT_CONFIG_ALL, params);

        state.updateSegmentAll(segmentConfig);
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

        requestFunctionSerialDeviceDefault(SerialFunction.RESET_SEGMENTS, params);

        state.resetSegments(segments);
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
        requestFunctionSerialDeviceDefault(SerialFunction.SET_LENGTH, params);

        state.setLength(pixelCount);
    }

    private Integer getLengthSerialDevice() throws WS2812FXException {
        UIntPacket params = UIntPacketBuilder.create().build(); //empty params
        try {
            UIntPacket response = sendRequestAndParseResponse(SerialFunction.GET_LENGTH, params, ByteMask.LENGTH).get();
            UInt length = response.getList().get(0);
            return (int) length.toLong();

        } catch (ExecutionException | InterruptedException e) {
            throw new WS2812FXSerialException(e);
        }

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

    private List<Segment> getSegmentsSerialDevice() throws WS2812FXException {
        UIntPacket emptyParams = UIntPacketBuilder.create().build();

        try {
            byte[] responseBytes = sendRequest(SerialFunction.GET_SEGMENTS, emptyParams).get();
            //calculate response segment count with segment byte mask
            final int segmentByteSize = ByteMask.SEGMENT.getByteSize();
//            final int segmentCount = responseBytes.length / segmentByteSize;
            UIntPacketBuilder builder = UIntPacketBuilder.create();
            List<Segment> result = new ArrayList<>();
            for (int offset = 0, segmentIndex = 0; (offset + segmentByteSize) <= responseBytes.length; offset += segmentByteSize, segmentIndex++) {
                Segment segment = SegmentConverter.fromUIntPacket(
                        builder.append(
                                Arrays.copyOfRange(responseBytes, offset, (offset + segmentByteSize)),
                                ByteOrder.LITTLE_ENDIAN,
                                ByteMask.SEGMENT
                        ).build()
                );
                segment.setIndex(segmentIndex);
                result.add(segment);
                builder.clear(); //reuse builder
            }


            //convert responseByte to uIntPacket
//            UIntPacket response = UIntPacketBuilder.createOf(responseBytes, ByteOrder.LITTLE_ENDIAN, multipleSegmentByteMask(segmentCount)).build(); //TODO remove multieSegment bytmask and use builder append instead
            return result;

        } catch (ExecutionException | InterruptedException e) {
            throw new WS2812FXSerialException(e); //TODO message
        } catch (ByteSizesNotMatchException e) {
            throw new WS2812FXSerialException(serial.getName(), SerialFunction.GET_SEGMENTS,
                    "Response value of serial device not match the given byteMask.",
                    e);
        }
    }

    @Override
    public void pause() throws WS2812FXException {
        requestFunctionSerialDeviceDefault(SerialFunction.SET_STATE, STATE_PAUSE_PACKET);

        state.pause();
    }

    @Override
    public void resume() throws WS2812FXException {
        requestFunctionSerialDeviceDefault(SerialFunction.SET_STATE, STATE_RESUME_PACKET);

        state.resume();
    }

    @Override
    public void start() throws WS2812FXException {
        requestFunctionSerialDeviceDefault(SerialFunction.SET_STATE, STATE_START_PACKET);

        state.start();
    }

    @Override
    public void stop() throws WS2812FXException {
        requestFunctionSerialDeviceDefault(SerialFunction.SET_STATE, STATE_STOP_PACKET);

        state.stop();
    }

    private void pauseBeforeCallingFunction(SerialFunction serialFunction) throws WS2812FXSerialException {
//        try {
//            log.debug("[{}] Pausing serial device before sending request", serialFunction.getName());
//            this.pause();
//            log.debug("[{}] Paused successfully", serialFunction.getName());
//        } catch (WS2812FXException e) {
//            throw new WS2812FXSerialException(
//                    String.format("Failed to send pause request before calling %s function. ",
//                            serialFunction.getName()),
//                    e
//            );
//        }
    }

    private void resumeAfterCallingFunction(SerialFunction serialFunction) throws WS2812FXSerialException {
//        try {
//            log.debug("[{}] Resuming serial device after finishing request", serialFunction.getName());
//            this.resume();
//            log.debug("[{}] Resumed successfully", serialFunction.getName());
//        } catch (WS2812FXException e) {
//            throw new WS2812FXSerialException(String.format("Failed to send resume request after calling %s function. ", serialFunction.getName()), e);
//        }
    }

    private void requestFunctionSerialDeviceDefault(SerialFunction serialFunction, UIntPacket requestParameters) throws WS2812FXException {
        try {
            Failsafe.with(retryPolicy).run(() -> {
                CompletableFuture<UIntPacket> responsePacket = sendRequestAndParseResponse(serialFunction, requestParameters, ByteMask.DEFAULT_RESPONSE);
                validateDefaultResponse(serialFunction, responsePacket.get());
            });
        } catch (FailsafeException e) {
            if (e.getCause() instanceof WS2812FXSerialException) {
                throw (WS2812FXSerialException) e.getCause();
            } else {
                throw new WS2812FXException(e); //TODO message
            }
        }
    }

    private CompletableFuture<UIntPacket> sendRequestAndParseResponse(SerialFunction serialFunction, UIntPacket requestParameters, UIntByteMask responseByteMask) throws WS2812FXException {
        return sendRequest(serialFunction, requestParameters) //
                .thenApplyAsync(responseBytes -> {
                    log.debug("[{}] Received response for request: {}", serialFunction.getName(), ByteArrayUtils.toStringUnsigned(responseBytes));
                    try {
                        return processSerialResponse(serialFunction, responseBytes, responseByteMask);
                    } catch (WS2812FXException e) {
                        WS2812FXSerialException exception = new WS2812FXSerialException(serial.getName(), serialFunction, "Error waiting for request response", e);
                        throw new CompletionException(exception);
                    }
                });
    }

    private CompletableFuture<byte[]> sendRequest(SerialFunction serialFunction, UIntPacket requestParameters) throws WS2812FXSerialException {
        UIntPacket request = UIntPacketBuilder
                .createOf(UInt.create8(serialFunction.getNumber()))
                .append(requestParameters)
                .build();
        log.debug("[{}] Submitting request to serial device: {}", serialFunction.getName(), request.toStringWithUIntTypes());
        return serial.submitRequest(request.getBytesLittleEndian());
    }


//    private void awaitListeningPacket(SerialFunction serialFunction) {
//        log.debug("[{}] Waiting for serial device to listen", serialFunction.getName());
//        int failCounter = 0;
//        while (failCounter < 5) {
//            try {
//                UIntPacket packet = UIntPacketBuilder.createOf(ByteMask.SERIAL_DEVICE_LISTENING_PACKET, serial.receiveBytePacket(), ByteOrder.LITTLE_ENDIAN);
//                if(SERIAL_DEVICE_LISTENING_PACKET.equals(packet)) {
//                    return;
//                } else {
//                    log.debug("[{}] Received an unknown packet from serial device while waiting for listening packet. Packet received was: {}.", serialFunction.getName(), packet.toStringWithUIntTypes());
//                    failCounter++;
//                }
//            } catch (ReceivingPacketFailed receivingPacketFailed) {
//                log.warn("[{}] Receiving packet from serial failed while waiting for listening packet.", serialFunction.getName(), receivingPacketFailed);
//                failCounter++;
//            } catch (ByteSizesNotMatchException e) {
//                log.warn("[{}] Byte size of received packet not matches the listening packet: {}", serialFunction.getName(), e.getMessage());
//                failCounter++;
//            }
//        }
//        throw new IllegalStateException("Waiting for listening packet failed."); //TODO better exception
//    }

    private UIntPacket processSerialResponse(SerialFunction serialFunction, byte[] responseBytes, UIntByteMask responseByteMask) throws WS2812FXException {
        try {
            UIntPacket responsePacket = UIntPacketBuilder.createOf(responseBytes, ByteOrder.LITTLE_ENDIAN, responseByteMask).build();
            log.debug("[{}] Response successfully parsed: {}", serialFunction.getName(), responsePacket.toStringWithUIntTypes());
            return responsePacket;
        } catch (ByteSizesNotMatchException e) {
            throw new WS2812FXSerialException(serial.getName(), serialFunction,
                    "Response of serial device not match the given byteMask.",
                    e
            );
        }
    }


    private void validateDefaultResponse(SerialFunction serialFunction, UIntPacket responsePacket) throws WS2812FXException {
        UIntPacket functionCalledSuccessfulResponse = UIntPacketBuilder.createOf(UInt.create8(serialFunction.getNumber())).build();
        if (functionCalledSuccessfulResponse.equals(responsePacket)) {
            return; //validation successful
        } else if (ERROR_PACKET.equals(responsePacket)) {
            throw new WS2812FXSerialException(serial.getName(), serialFunction, "Serial device responded with error response.");
        } else if (FUNCTION_NOT_FOUND_PACKET.equals(responsePacket)) {
            throw new WS2812FXSerialException(serial.getName(), serialFunction, String.format("No function with number %d could be found.", serialFunction.getNumber()));
        } else {
            throw new WS2812FXSerialException(serial.getName(), serialFunction, String.format("Serial device responded with undefined response. Response was: %s", responsePacket.toString()));
        }
    }
}
