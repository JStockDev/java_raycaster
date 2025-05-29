package dev.jstock.client;

public class Config {
    private String serverAddress;

    public Config(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }
}
