package dev.jstock.commons.Frames;

import java.nio.ByteBuffer;

import dev.jstock.commons.FrameData;
import dev.jstock.commons.FrameDataFactory;

public class PlayerLocationFrame extends FrameData {

    private double playerX;
    private double playerY;
    private double playerFacing;

    public PlayerLocationFrame(double playerX, double playerY, double playerFacing) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerFacing = playerFacing;
    }

    @Override
    public byte[] encode() {
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putDouble(playerX);
        buffer.putDouble(playerY);
        buffer.putDouble(playerFacing);

        return buffer.array();
    }

    @Override
    public byte getFrameIdentifier() {
        return FrameDataFactory.LOCATION_FRAME;
    }

}
