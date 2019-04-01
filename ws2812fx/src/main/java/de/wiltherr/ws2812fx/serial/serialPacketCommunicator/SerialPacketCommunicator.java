package de.wiltherr.ws2812fx.serial.serialPacketCommunicator;

import com.fazecast.jSerialComm.SerialPort;
import de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.SendingPacketTimeout;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class SerialPacketCommunicator {

    private SerialPort serialPort;
    private int serialDeviceReadBufferSize;
    private int readTimeout;
    private int writeTimeout;

    public SerialPacketCommunicator(int portIndex, int readTimeoutMs, int writeTimeoutMs, int baudRate, int serialDeviceReadBufferSize) {
        try {
            init(SerialPort.getCommPorts()[portIndex], readTimeoutMs, writeTimeoutMs, baudRate, serialDeviceReadBufferSize);
        } catch (ArrayIndexOutOfBoundsException e) {
            final AtomicInteger i = new AtomicInteger(0);
            throw new de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.SerialPacketCommunictorOpeningException(String.format("No serial device was found on index %d. Available port indices are: %s", portIndex,
                    Arrays.stream(getPorts())
                            .map(port -> i.getAndIncrement() + "=" + port.getSystemPortName() + " (" + port.getPortDescription() + ")")
                            .collect(Collectors.joining(", ", "[", "]"))
            ));
        }
    }

    public SerialPacketCommunicator(SerialPort serialPort, int readTimeoutMs, int writeTimeoutMs, int baudRate, int serialDeviceReadBufferSize) {
        Objects.requireNonNull(serialPort, "serialPort is null.");
        init(serialPort, readTimeoutMs, writeTimeoutMs, baudRate, serialDeviceReadBufferSize);
    }

    public static SerialPort[] getPorts() {
        return SerialPort.getCommPorts();
    }

    private void init(SerialPort serialPort, int readTimeoutMs, int writeTimeoutMs, int baudRate, int serialDeviceReadBufferSize) {
        this.serialDeviceReadBufferSize = serialDeviceReadBufferSize;
        this.readTimeout = readTimeoutMs;
        this.writeTimeout = writeTimeoutMs;

        this.serialPort = serialPort;
        this.serialPort.setBaudRate(baudRate);
        this.serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, readTimeoutMs, writeTimeoutMs);
        boolean success = this.serialPort.openPort();
        if (!success) {
            throw new de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.SerialPacketCommunictorOpeningException("Opening port " + serialPort.getSystemPortName() + " (" + serialPort.getPortDescription() + ") failed.");
        }
    }

    public int sendBytePacket(byte[] bytePacket) throws de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.SendingPacketFailed {
        Objects.requireNonNull(bytePacket, "bytePacket was null.");
        if (serialDeviceReadBufferSize < (bytePacket.length + CobsUtils.OVERHEAD_LENGTH)) {
            throw new de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.PacketSizeException("Packet BYTE_SIZE is higher than read buffer of target device.");
        }
        //encode packet
        byte[] cobsPacket = CobsUtils.encode(bytePacket);
        int bytesWritten = serialPort.writeBytes(cobsPacket, cobsPacket.length);
        if (bytesWritten == 0) {
            throw new SendingPacketTimeout(writeTimeout);
        } else if (bytesWritten == -1) {
            throw new de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.SendingPacketFailed();
        }
        return bytesWritten;
    }

    public byte[] receiveBytePacket() throws de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.ReceivingPacketFailed {
        ByteBuffer serialReadBuffer = ByteBuffer.allocate(serialDeviceReadBufferSize);
        while (true) {
            byte[] temp = new byte[1];
            int bytesRead = serialPort.readBytes(temp, temp.length);
            if (bytesRead == 0) {
                throw new de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.ReceivingPacketTimeout(readTimeout);
            } else if (bytesRead == -1) {
                throw new de.wiltherr.ws2812fx.serial.serialPacketCommunicator.exception.ReceivingPacketFailed();
            }
            serialReadBuffer.put(temp);
            //check if the end of COBS packet is reached (last byte of COBS packet has to be 0)
            if (temp[0] == 0x0) {
                //get written bytes from buffer
                byte[] cobsPacket = Arrays.copyOfRange(serialReadBuffer.array(), 0, serialReadBuffer.position());
                //return decoded packet
                return CobsUtils.decode(cobsPacket);
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", serialPort.getSystemPortName(), serialPort.getDescriptivePortName());
    }
}
