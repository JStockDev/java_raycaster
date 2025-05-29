package dev.jstock.commons;

import java.util.ArrayList;
import java.util.Arrays;

// This is the main frame structure used for networking
// Contains a byte for the frame type and then the Frame data object
public class Frame {
    private byte frameType;
    private FrameData frameData;

    public Frame(byte frameType, FrameData frameData) {
        this.frameType = frameType;
        this.frameData = frameData;
    }

    // Check the frame type by looking at the first byte, and then decode the rest of the bytes accordingly
    public static Frame decodeBytes(byte[] rawData) {
        byte frameType = rawData[0];
        FrameData data = FrameDataFactory.decodeFrameData(frameType, Arrays.copyOfRange(rawData, 1, rawData.length));
        return new Frame(frameType,  data);
    }

    public byte[] encodeFrame() {
        // Create a list to hold the frame data
        ArrayList<Byte> data = new ArrayList<>();

        // Add the frame type 
        data.add(frameType);

        // Add the encoded frame data
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
