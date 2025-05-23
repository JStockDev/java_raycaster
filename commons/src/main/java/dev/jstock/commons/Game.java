package dev.jstock.commons;

import java.lang.reflect.Array;
import java.util.ArrayList;

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

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
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
