package dev.jstock.server;

import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import dev.jstock.commons.Frame;
import dev.jstock.commons.FrameDataFactory;
import dev.jstock.commons.FrameFactory;
import dev.jstock.commons.Game;
import dev.jstock.commons.Player;

public class Server extends WebSocketServer {

    public Server(int port) {
        super(new java.net.InetSocketAddress(port));
    }

    @Override
    public void onOpen(WebSocket con, ClientHandshake handshake) {
        System.out.println("New client: " + con.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket con, int code, String reason, boolean remote) {
        System.out.println("Client closed: " + con.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket con, Exception ex) {
        System.err.println("Client error: " + con.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket con, ByteBuffer blob) {
        Frame frame = Frame.decodeBytes(blob.array());
        Game game = GameSingleton.getInstance();

        switch (frame.getType()) {
            case FrameDataFactory.JOIN_FRAME:
                if (game.containsPlayer(frame.getClientUUID()) == true) {
                    System.out.println("Client error: Player already exists" + frame.getClientUUID());
                    con.close();
                    return;
                } else {
                    Player player = new Player(frame.getClientUUID(), 1.5, 1.5, 0.0);
                    game.addPlayer(player);
                    con.send(FrameFactory.createGameFrame(player, game).encodeFrame());
                }

                return;
            case FrameDataFactory.LEAVE_FRAME:
                game.removePlayer(frame.getClientUUID());
                con.close();

                broadcast(FrameFactory.createLeaveFrame(frame.getClientUUID()).encodeFrame());
                break;
            case FrameDataFactory.GAME_FRAME:
                // Should never receive game frame from client
                break;
            case FrameDataFactory.PLAYER_FRAME:
            
                return;
            case FrameDataFactory.OBJECTIVE_FRAME:
                return;
            case FrameDataFactory.ERROR_FRAME:
                return;
            default:
                return;
        }

    }

    @Override
    public void onStart() {
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
    }
}
