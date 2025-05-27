package dev.jstock.commons;

import java.util.ArrayList;
import java.util.UUID;

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

    public byte[][] getMap() {
        return map;
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
