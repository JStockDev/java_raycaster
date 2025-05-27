package dev.jstock.commons;

import java.util.ArrayList;
import java.util.Arrays;


public class Frame {
    private byte frameType;
    private FrameData frameData;

    public Frame(byte frameType, FrameData frameData) {
        this.frameType = frameType;
        this.frameData = frameData;
    }

    // ConcurrentLinkedQueue
    public static Frame decodeBytes(byte[] rawData) {
        byte frameType = rawData[0];
        FrameData data = FrameDataFactory.decodeFrameData(frameType, Arrays.copyOfRange(rawData, 1, rawData.length));
        return new Frame(frameType,  data);
    }

    public byte[] encodeFrame() {
        ArrayList<Byte> data = new ArrayList<>();

        data.add(frameType);

        byte[] frameDataBytes = frameData.encode();
        for (byte b : frameDataBytes) {
            data.add(b);
        }

        byte[] frameDataArray = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            frameDataArray[i] = data.get(i);
        }
        
        return frameDataArray;
    }

    public byte getType() {
        return frameType;
    }

    public FrameData getFrameData() {
        return frameData;
    }
}
