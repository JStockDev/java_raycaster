package dev.jstock.server;

import java.nio.file.Files;
import java.nio.file.Path;

import com.moandjiezana.toml.Toml;

public class Entry {
    public static byte[][] MAP;

    public static void main(String[] args) throws Exception {
        
        String rawFile = Files.readString(Path.of("./config.toml"));
        Config config = new Toml().read(rawFile).to(Config.class);

        MAP = config.getMap();
        Server server = new Server(config.getPort());
        server.start();
    }

}
