package dev.jstock.commons;

import java.util.ArrayList;
import java.util.UUID;

// Main game class, which is responsible for managing players and holding the current map
public class Game {
    private ArrayList<Player> players;
    private byte[][] map;

    public Game(byte[][] map) {
        players = new ArrayList<>();
        this.map = map;
    }

    public Game(ArrayList<Player> players, byte[][] map) {
        this.players = players;
        this.map = map;
    }

    public Player[] getPlayers() {
        return players.toArray(new Player[0]);
    }

    public Player[] getOtherPlayers(Player player) {
        ArrayList<Player> players = new ArrayList<>();

        for (Player p : this.players) {
            if (!p.getIdentifier().equals(player.getIdentifier())) {
                players.add(p);
            }
        }

        return players.toArray(new Player[0]);
    }

    public byte[][] getMap() {
        return map;
    }

    public double getStartingX() {
        double startingX = 0.5;

        // Find the first occurrence of the starting point (2) in the map, as defined in the map legend in the config file
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 2) {
                    startingX += i;
                    break;
                }
            }
        }

        return startingX;
    }

    public double getStartingY() {
        double startingY = 0.5;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 2) {
                    startingY += j;
                    break;
                }
            }
        }

        return startingY;
    }

    public double getObjectiveX() {
        double objectiveX = 0.5;

        // Find the first occurrence of the objective point (3) in the map, as defined in the map legend in the config file
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 3) {
                    objectiveX += i;
                    break;
                }
            }
        }

        return objectiveX;
    }

    public double getObjectiveY() {
        double objectiveY = 0.5;

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 3) {
                    objectiveY += j;
                    break;
                }
            }
        }

        return objectiveY;
    }

    public boolean containsPlayer(UUID playerUUID) {
        for (Player player : players) {
            if (player.getIdentifier().equals(playerUUID)) {
                return true;
            }
        }
        return false;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void updatePlayer(Player player) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getIdentifier().equals(player.getIdentifier())) {
                players.set(i, player);
                return;
            }
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void removePlayer(UUID playerUUID) {
        players.removeIf(player -> player.getIdentifier().equals(playerUUID));
    }

    public void setPlayerX(Player player, double x) {
        player.setX(x);
    }

    public void setPlayerY(Player player, double y) {
        player.setY(y);
    }

    public void setPlayerFacing(Player player, double facing) {
        player.setFacing(facing);
    }

    public void getPlayerLocation(Player player) {
        player.getX();
        player.getY();
        player.getFacing();
    }
}
