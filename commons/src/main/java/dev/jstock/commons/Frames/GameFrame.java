package dev.jstock.commons.Frames;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

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

        int playerBytesLength = players.length * 40;
        data.add(playerBytesLength)

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'encode'");
    }

    @Override
    public byte getFrameIdentifier() {
        return FrameDataFactory.GAME_FRAME;
    }

}
