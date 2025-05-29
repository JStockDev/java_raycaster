package dev.jstock.server;

// Simple config file to hold server settings

public class Config {
    private int port;
    private byte[][] map;

    public Config(int port, byte[][] map) {
        this.port = port;
        this.map = map;
    }

    public int getPort() {
        return port;
    }

    public byte[][] getMap() {
        return map;
    }
}
