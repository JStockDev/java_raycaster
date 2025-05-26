package dev.jstock.commons;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Player extends FrameData {
    private UUID identifier;
    private Double x;
    private Double y;
    private Double facing;

    public Player(UUID identifier, Double playerX, Double playerY, Double playerFacing) {
        this.identifier = identifier;
        x = playerX;
        y = playerY;
        facing = playerFacing;
    }
    public Player(Double playerX, Double playerY, Double playerFacing) {
        identifier = UUID.randomUUID();
        x = playerX;
        y = playerY;
        facing = playerFacing;
    }
    public UUID getIdentifier() {
        return identifier;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getFacing() {
        return facing;
    }

    public void setX(double playerX) {
        this.x = playerX;
    }
    public void setY(double playerY) {
        this.y = playerY;
    }
    public void setFacing(double playerFacing) {
        this.facing = playerFacing;
    }
    public void setLocation(double playerX, double playerY, double playerFacing) {
        this.x = playerX;
        this.y = playerY;
        this.facing = playerFacing;
    }
    @Override
    public byte[] encode() {
        ByteBuffer buffer = ByteBuffer.allocate(40);
        buffer.putLong(this.identifier.getMostSignificantBits());
        buffer.putLong(this.identifier.getLeastSignificantBits());
        buffer.putDouble(this.x);
        buffer.putDouble(this.y);
        buffer.putDouble(this.facing);

        return buffer.array();
    }
    @Override
    public byte getFrameIdentifier() {
        return FrameDataFactory.PLAYER_FRAME;
    }
}
