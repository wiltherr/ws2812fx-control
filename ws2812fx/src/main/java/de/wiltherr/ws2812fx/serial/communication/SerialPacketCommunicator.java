package de.wiltherr.ws2812fx.serial.communication;


import java.util.concurrent.CompletableFuture;

public interface SerialPacketCommunicator extends Runnable {

    String getName();

    CompletableFuture<byte[]> submitRequest(byte[] bytePacket);

    void stop();

    boolean isRunning();
}
