package de.wiltherr.ws2812fx.serial.communication;

import com.fazecast.jSerialComm.SerialPort;
import de.wiltherr.ws2812fx.serial.communication.exception.*;
import de.wiltherr.ws2812fx.serial.entity.uint.UInt;
import de.wiltherr.ws2812fx.serial.entity.uint.UIntPacketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SerialPacketCommunicatorImpl implements SerialPacketCommunicator {

    private static final int MAX_SERIAL_DEVICE_ERRORS = 50;
    private static final int MAX_SERIAL_DEVICE_RECONNECTS = 3;
    private static final int MAX_SKIPPED_PACKETS = 100;
    private static final Logger log = LoggerFactory.getLogger(SerialPacketCommunicatorImpl.class);
    private final byte[] readyCobsPacket = CobsUtils.encode(UIntPacketBuilder.createOf(UInt.create(155, UInt.Type.U_INT_8)).build().getBytesLittleEndian());
    private final Queue<SerialPacketRequest> requestQueue = new LinkedList<>();
    private SerialPort serialPort;
    private final byte[] serialReceiveBuffer;
    private int bufferSize;
    private int readTimeout;
    private int writeTimeout;
    private volatile boolean isRunning;

    public SerialPacketCommunicatorImpl(int portIndex, int readTimeoutMs, int writeTimeoutMs, int baudRate, int bufferSize) {
        serialReceiveBuffer = new byte[bufferSize];
        try {
            init(SerialPort.getCommPorts()[portIndex], readTimeoutMs, writeTimeoutMs, baudRate, bufferSize);
        } catch (ArrayIndexOutOfBoundsException e) {
            final AtomicInteger i = new AtomicInteger(0);
            throw new SerialPacketCommunicatorOpeningException(String.format("No serial device was found on index %d. Available port indices are: %s", portIndex,
                    Arrays.stream(getPorts())
                            .map(port -> i.getAndIncrement() + "=" + port.getSystemPortName() + " (" + port.getPortDescription() + ")")
                            .collect(Collectors.joining(", ", "[", "]"))
            ));
        }
    }

    public SerialPacketCommunicatorImpl(SerialPort serialPort, int readTimeoutMs, int writeTimeoutMs, int baudRate, int bufferSize) {
        Objects.requireNonNull(serialPort, "serialPort is null.");
        serialReceiveBuffer = new byte[bufferSize];
        init(serialPort, readTimeoutMs, writeTimeoutMs, baudRate, bufferSize);
    }

    private static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    private void init(SerialPort serialPort, int readTimeoutMs, int writeTimeoutMs, int baudRate, int bufferSize) {
        this.bufferSize = bufferSize;
        this.readTimeout = readTimeoutMs;
        this.writeTimeout = writeTimeoutMs;

        this.serialPort = serialPort;
        this.serialPort.setBaudRate(baudRate);
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, readTimeoutMs, writeTimeoutMs);
    }

    private void openPort() {
        boolean success = serialPort.openPort();
        if (!success) {
            throw new SerialPacketCommunicatorOpeningException("Opening port " + serialPort.getSystemPortName() + " (" + serialPort.getPortDescription() + ") failed.");
        }
        log.info("[{}] Serial port opened successfully.", this.getName());
    }

    private void closePort() {
        boolean success = serialPort.closePort();
        if (!success) {
            throw new SerialPacketCommunicatorClosingException("Closing port " + serialPort.getSystemPortName() + " (" + serialPort.getPortDescription() + ") failed.");
        }
        log.info("[{}] Serial port closed successfully.", this.getName());
    }

    @Override
    public CompletableFuture<byte[]> submitRequest(byte[] requestBytes) {
        if (!this.isRunning) {
            throw new IllegalStateException("Connection to serial device is not running.");
        }
        Objects.requireNonNull(requestBytes, "bytePacket was null.");
        if (bufferSize < (requestBytes.length + CobsUtils.OVERHEAD_LENGTH)) {
            throw new PacketSizeException("Packet BYTE_SIZE is higher than read buffer of target device.");
        }
        SerialPacketRequest request = new SerialPacketRequest(
                CobsPacket.encode(requestBytes)
        );
        requestQueue.offer(request);
        log.info("[{}] Request submitted (queue size after offer: {})", this.getName(), requestQueue.size());
        return request;
    }

    private void send(CobsPacket packet) throws SendingPacketFailed {
        int bytesWritten = serialPort.writeBytes(packet.getBytesEncoded(), packet.getBytesEncoded().length);
        if (bytesWritten == 0) {
            throw new SendingPacketTimeout(writeTimeout);
        } else if (bytesWritten == -1) {
            throw new SendingPacketFailed(); //TODO message
        } else if (bytesWritten != packet.getBytesEncoded().length) {
            throw new SendingPacketFailed();  //TODO message
        }
    }

    private byte[] receive() throws ReceivingPacketFailed {

        int offset = 0;
        int bytesRead = 0;
        while (true) {
            bytesRead = serialPort.readBytes(serialReceiveBuffer, 1, offset);
            if (bytesRead == 1) {
                //check if the end of COBS packet is reached (last byte of COBS packet has to be 0)
                if (serialReceiveBuffer[offset] == 0x0) {
                    //get written bytes from buffer and return decoded packet
                    return Arrays.copyOfRange(serialReceiveBuffer, 0, offset + 1);
                }
                offset += bytesRead;
            } else if (bytesRead == 0) {
                throw new ReceivingPacketTimeout(readTimeout);
            } else if (bytesRead == -1) {
                throw new ReceivingPacketFailed();
            } else {
                return null; //TODO throw exception instead
            }
        }


    }

    @Override
    public String getName() {
        return String.format("%s (%s)", serialPort.getSystemPortName(), serialPort.getDescriptivePortName());
    }

    @Override
    public String toString() {
        return "SerialPacketCommunicator{" +
                "serialPort=" + getName() +
                ", bufferSize=" + bufferSize +
                ", readTimeout=" + readTimeout +
                ", writeTimeout=" + writeTimeout +
                '}';
    }

    @Override
    public void run() {
        isRunning = true;
        int failCounter = 0;
        int reconnectCounter = 0;


        final AtomicReference<SerialPacketRequest> currentRequest = new AtomicReference<>();
        final AtomicBoolean currentRequestSended = new AtomicBoolean(false);
        final AtomicInteger skippedPacketCounter = new AtomicInteger(0);
        final AtomicLong startMillis = new AtomicLong(0);
        byte[] receivedCobsPacket;
        log.info("[{}] Serial communication was stared.", this.getName());
        while (isRunning) {
            try {
                if (!serialPort.isOpen()) {
                    openPort();
                }


                if (currentRequest.get() == null && !requestQueue.isEmpty()) {
                    currentRequest.set(requestQueue.remove());
                    startMillis.set(System.currentTimeMillis());
                    log.trace("[{}] Process request (queue size after remove: {})", this.getName(), requestQueue.size());
                    currentRequest.get().whenComplete((result, throwable) -> {
                        log.debug("[{}] Request complete in {}ms (skipped packet counter: {})",
                                this.getName(), (System.currentTimeMillis() - startMillis.get()), skippedPacketCounter.get());
                        currentRequest.set(null);
                        skippedPacketCounter.set(0);
                        currentRequestSended.set(false);
                    });
                }


                receivedCobsPacket = this.receive();
                if (Arrays.equals(receivedCobsPacket, this.readyCobsPacket)) {
                    failCounter = 0;
                    if (currentRequest.get() != null && !currentRequestSended.get()) {
                        try {
                            this.send(currentRequest.get().getPacket());
                            currentRequestSended.set(true);
                            log.trace("[{}] Sent request to serial device (request: {}). Waiting for response.", this.getName(), ByteArrayUtils.toStringUnsigned(currentRequest.get().getPacket().getBytesDecoded()));
                        } catch (SendingPacketFailed sendingPacketFailed) {
                            log.error("[{}] Failed to send packet. ", this.getName(), sendingPacketFailed);
                            currentRequest.get().completeExceptionally(sendingPacketFailed);
                        }
                    } else if (currentRequest.get() != null && currentRequestSended.get()) {
                        if (skippedPacketCounter.incrementAndGet() >= MAX_SKIPPED_PACKETS) {
                            log.error("[{}] Processing request failed. Skipped too many ready packets without receiving the response.", this.getName());
                            currentRequest.get().completeExceptionally(new ReceivingResponseFailed("Serial device not responding to request."));
                        } else {
                            log.trace("[{}] Skipped readyCobsPacket while waiting for response (skipped packet counter: {})",
                                    this.getName(), skippedPacketCounter.get());
                        }
                    } else {
                        log.trace("[{}] Serial communication is active, no requests to process.", this.getName());
                    }
                } else {

                    if (currentRequest.get() != null && currentRequestSended.get()) {
                        currentRequest.get().complete(CobsUtils.decode(receivedCobsPacket));
                        log.trace("[{}] Processing request completed successfully.", this.getName());
                    } else {
                        failCounter++;
                        log.warn("[{}] Serial communication error: Did not receive the ready packet form serial device (fail counter: {}). Received bytes: {}",
                                this.getName(), failCounter, ByteArrayUtils.toStringUnsigned((receivedCobsPacket)));
                    }
                }
            } catch (Exception e) {
                failCounter++;
                log.error("[{}] Serial communication error: Failed to receive packet form serial device (fail counter: {}). ", this.getName(), failCounter, e);
            }

            if (failCounter > MAX_SERIAL_DEVICE_ERRORS && reconnectCounter <= MAX_SERIAL_DEVICE_RECONNECTS) {
                log.warn("[{}] Too many serial communication errors (fail counter: {}). Trying to reconnect to serial device (reconnect counter: {}).",
                        this.getName(), failCounter, reconnectCounter);
                closePort();
                openPort();
                reconnectCounter++;
            } else if (reconnectCounter > MAX_SERIAL_DEVICE_RECONNECTS) {
                log.error("[{}] Too many serial communication errors (fail counter: {}) and failed reconnects (reconnect counter: {}). Closing serial port.",
                        this.getName(), failCounter, reconnectCounter);
                closePort();
                isRunning = false;
                throw new TooManyErrorsException("Error counter and reconnect counter exceeded. Serial port closed and communication stopped."); //TODO better exception
            }
            //TODO reconnect serial port when failCounter is exceeded
            //TODO throw exception when reconnect failed
        }

        //TODO check failCounter
        isRunning = false;
        log.info("[{}] Serial communication was stopped successfully (left requests in queue: {})", this.getName(), requestQueue.size());
        if (serialPort.isOpen()) {
            closePort();
        }
    }

    public void stop() {
        log.info("[{}] Serial communication was stopped.", this.getName());
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }


}
