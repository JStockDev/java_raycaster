package dev.jstock.server;

import dev.jstock.commons.Game;


// Singleton pattern for the Game instance
// This class, while not strictly useful in this project instance, was required by the assignment specifications

public class GameSingleton {
    private static Game instance;

    public static synchronized Game getInstance() {
        if (instance == null) {
            instance = new Game(Entry.MAP);
        }
        return instance;
    }
}
