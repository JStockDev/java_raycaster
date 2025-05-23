package dev.jstock.commons;

import java.nio.ByteBuffer;

import dev.jstock.commons.Frames.JoinFrame;
import dev.jstock.commons.Frames.LeaveFrame;
import dev.jstock.commons.Frames.PlayerLocationFrame;

public class FrameDataFactory {
    public static final byte JOIN_FRAME = 0;
    public static final byte LEAVE_FRAME = 1;
    public static final byte GAME_FRAME = 2;
    public static final byte LOCATION_FRAME = 3;
    public static final byte OBJECTIVE_FRAME = 4;
    public static final byte ERROR_FRAME = 5;

    public static byte[] encodeFrameData(FrameData frameData) {
        byte[] data = new byte[24];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.put(frameData.encode());
        return data;
    }

    public static FrameData decodeFrameData(int frameType, byte[] data) {
        switch (frameType) {
            case JOIN_FRAME:
                if (data.length != 0) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }
                return new JoinFrame();

            case LEAVE_FRAME:
                if (data.length != 0) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }
                return new LeaveFrame();

            case LOCATION_FRAME:
                if (data.length != 24) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }

                ByteBuffer buffer = ByteBuffer.wrap(data);
                Double playerX = buffer.getDouble();
                Double playerY = buffer.getDouble();
                Double playerFacing = buffer.getDouble();
                return new PlayerLocationFrame(playerX, playerY, playerFacing);
            default:
                throw new IllegalArgumentException("Invalid frame type");
        }
    }
}
