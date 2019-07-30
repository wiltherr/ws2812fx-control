package de.wiltherr.ws2812fx.serial.communication;

import java.util.concurrent.CompletableFuture;

class SerialPacketRequest extends CompletableFuture<byte[]> {
    private final CobsPacket requestPacket;

    SerialPacketRequest(CobsPacket requestPacket) {
        this.requestPacket = requestPacket;
    }

    CobsPacket getPacket() {
        return requestPacket;
    }
}
