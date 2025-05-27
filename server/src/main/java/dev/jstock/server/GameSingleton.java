package dev.jstock.server;

import dev.jstock.commons.Game;

public class GameSingleton {
    private static Game instance;

    public static synchronized Game getInstance() {
        if (instance == null) {
            instance = new Game(Entry.MAP);
        }
        return instance;
    }
}
