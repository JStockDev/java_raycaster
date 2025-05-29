package dev.jstock.client;

// Basic config class for the TOML config file

public class Config {
    private String serverAddress;

    public Config(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }
}
