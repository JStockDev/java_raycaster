package dev.jstock.commons;

import java.util.UUID;

public class Player {
    private UUID identifier;
    private Double x;
    private Double y;
    private Double facing;

    public Player(Double playerX, Double playerY, Double playerFacing) {
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
}
