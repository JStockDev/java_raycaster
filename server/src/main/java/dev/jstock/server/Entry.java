package dev.jstock.server;

import java.nio.file.Files;
import java.nio.file.Path;

import com.moandjiezana.toml.Toml;

public class Entry {
    public static byte[][] MAP;

    public static void main(String[] args) throws Exception {

        // Load config file, parse it
        String rawFile = Files.readString(Path.of("./server_config.toml"));
        Config config = new Toml().read(rawFile).to(Config.class);

        // Store the map in a static variable, to be accessed by the GameSingleton
        MAP = config.getMap();

        // Ensure the map is square
        int mapWidth = MAP.length;
        for (int i = 0; i < mapWidth; i++) {
            if (MAP[i].length != mapWidth) {
                throw new IllegalArgumentException("Map must be square, but row " + i + " has length " + MAP[i].length);
            }
        }

        // Ensure the map has exactly one player spawn point and at least one objective
        // spawn point
        int playerSpawnCount = 0;
        int objectiveSpawnCount = 0;

        for (int i = 0; i < mapWidth; i++) {
            for (int j = 0; j < mapWidth; j++) {
                if (MAP[i][j] == 2) {
                    playerSpawnCount++;
                } else if (MAP[i][j] == 3) {
                    objectiveSpawnCount++;
                }
            }
        }

        if (playerSpawnCount != 1) {
            throw new IllegalArgumentException(
                    "Map must have exactly one player spawn point, but found " + playerSpawnCount);
        }
        if (objectiveSpawnCount != 1) {
            throw new IllegalArgumentException(
                    "Map must have at least one objective spawn point, but found " + objectiveSpawnCount);
        }

        // Start the server
        Server server = new Server(config.getPort());
        server.start();
    }

}
