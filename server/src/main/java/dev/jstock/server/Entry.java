package dev.jstock.server;

import java.nio.file.Files;
import java.nio.file.Path;

import com.moandjiezana.toml.Toml;

public class Entry {
    public static byte[][] MAP;

    public static void main(String[] args) throws Exception {

        String rawFile = Files.readString(Path.of("./server_config.toml"));
        Config config = new Toml().read(rawFile).to(Config.class);

        MAP = config.getMap();

        int mapWidth = MAP.length;

        for (int i = 0; i < mapWidth; i++) {
            if (MAP[i].length != mapWidth) {
                throw new IllegalArgumentException("Map must be square, but row " + i + " has length " + MAP[i].length);
            }
        }

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

        Server server = new Server(config.getPort());
        server.start();
    }

}
