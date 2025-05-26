package dev.jstock.commons.Frames;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import dev.jstock.commons.FrameData;
import dev.jstock.commons.FrameDataFactory;
import dev.jstock.commons.Game;
import dev.jstock.commons.Player;

public class GameFrame extends FrameData {

    private Player[] players;
    private byte[][] map;

    public GameFrame(Player[] players, byte[][] map) {
        this.players = players;
        this.map = map;
    }

    public static GameFrame fromGame(Game game) {
        return new GameFrame(game.getPlayers(), game.getMap());
    }

    public Game toGame() {
        ArrayList<Player> players = new ArrayList<>(Arrays.asList(this.players));
        return new Game(players, map);
    }

    @Override
    public byte[] encode() {
        ArrayList<Byte> data = new ArrayList<>();

        int mapSize = 0;
        int playerBytesLength = players.length * 40;
        for (byte[] row : map) {
            mapSize += row.length;
        }
        for (byte b : ByteBuffer.allocate(8).putInt(mapSize).putInt(playerBytesLength).array()) {
            data.add(b);
        }

        for (byte[] row : map) {
            for (byte square : row) {
                data.add(square);
            }
        }

        for (Player player : players) {
            byte[] playerBytes = player.encode();
            for (byte b : playerBytes) {
                data.add(b);
            }
        }

        return ArrayUtils.toPrimitive(data.toArray(new Byte[0]));
    }

    @Override
    public byte getFrameIdentifier() {
        return FrameDataFactory.GAME_FRAME;
    }

}