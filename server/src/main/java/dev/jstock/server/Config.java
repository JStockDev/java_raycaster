package dev.jstock.server;

public class Config {
    private int port;
    private byte[][] map;

    public Config(int address, byte[][] map) {
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
