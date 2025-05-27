package dev.jstock.commons;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;


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
    public static Frame decodeBytes(byte[] rawData) {
        byte frameType = rawData[0];

        ByteBuffer rawUUID = ByteBuffer.wrap(Arrays.copyOfRange(rawData, 1, 17));
        UUID uuid = new UUID(rawUUID.getLong(), rawUUID.getLong());
        FrameData data = FrameDataFactory.decodeFrameData(frameType, Arrays.copyOfRange(rawData, 17, rawData.length));

        return new Frame(frameType, uuid, data);
    }

    public byte[] encodeFrame() {
        ArrayList<Byte> data = new ArrayList<>();

        data.add(frameType);
        ByteBuffer uuidBuffer = ByteBuffer.allocate(16);
        uuidBuffer.putLong(clientUUID.getMostSignificantBits());
        uuidBuffer.putLong(clientUUID.getLeastSignificantBits());
        for (byte b : uuidBuffer.array()) {
            data.add(b);
        }

        byte[] frameDataBytes = frameData.encode();
        for (byte b : frameDataBytes) {
            data.add(b);
        }
        
        return ArrayUtils.toPrimitive(data.toArray(new Byte[0]));
    }

    public byte getType() {
        return frameType;
    }

    public UUID getClientUUID() {
        return clientUUID;
    }

    public FrameData getFrameData() {
        return frameData;
    }
}
