package dev.jstock.commons;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;


public class Frame {
    private byte frameType;
    private UUID clientUUID;
    private FrameData frameData;

    public Frame(byte frameType, UUID clientUUID, FrameData frameData) {
        this.frameType = frameType;
        this.clientUUID = clientUUID;
        this.frameData = frameData;
    }

    // ConcurrentLinkedQueue
    public static Frame decodeBytes(byte[] data) {
        byte frameType = data[0];

        ByteBuffer rawUUID = ByteBuffer.wrap(Arrays.copyOfRange(data, 1, 17));
        UUID playerUUID = new UUID(rawUUID.getLong(), rawUUID.getLong());
        FrameData fData;

        switch (frameType) {
            case 0:

                // PlayerLocation playerLocation = PlayerLocation.decode
                break;

            default:
                break;
        }

        throw new IllegalArgumentException("Frame type not supported");
    }

    public byte[] encodeFrame() {
        ArrayList<Byte> data = new ArrayList();

        throw new IllegalArgumentException("Frame type not supported");
    }
}
