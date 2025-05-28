package dev.jstock.server;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import dev.jstock.commons.Frame;
import dev.jstock.commons.FrameDataFactory;
import dev.jstock.commons.FrameFactory;
import dev.jstock.commons.Game;
import dev.jstock.commons.Player;
import dev.jstock.commons.Frames.JoinFrame;
import dev.jstock.commons.Frames.LeaveFrame;

public class Server extends WebSocketServer {

    public Server(int port) {
        super(new java.net.InetSocketAddress(port));
    }

    HashMap<WebSocket, UUID> connections = new HashMap<>();

    @Override
    public void onStart() {
        System.out.println("Server started: " + super.getAddress());
    }

    @Override
    public void onOpen(WebSocket con, ClientHandshake handshake) {
        System.out.println("New client: " + con.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket con, int code, String reason, boolean remote) {
        System.out.println("Client closed: " + con.getRemoteSocketAddress());

        synchronized (connections) {
            UUID player = connections.get(con);
            if (player != null) {
                Game game = GameSingleton.getInstance();
                game.removePlayer(player);

                connections.remove(con);
                broadcast(FrameFactory.createLeaveFrame(player).encodeFrame());
                return;
            } else {
                System.out.println("Client closed without joining: " + con.getRemoteSocketAddress());
            }
        }
    }

    @Override
    public void onMessage(WebSocket con, ByteBuffer blob) {
        Frame frame = Frame.decodeBytes(blob.array());
        Game game = GameSingleton.getInstance();

        switch (frame.getType()) {
            case FrameDataFactory.JOIN_FRAME:
                JoinFrame joinFrame = (JoinFrame) frame.getFrameData();

                if (game.containsPlayer(joinFrame.getClientUUID()) == true) {
                    System.out.println("Client error: Player already exists" + joinFrame.getClientUUID());
                    con.close();
                    return;
                }

                synchronized (connections) {
                    connections.put(con, joinFrame.getClientUUID());
                }

                Player newPlayer = new Player(joinFrame.getClientUUID());
                game.addPlayer(newPlayer);

                con.send(FrameFactory.createGameFrame(game).encodeFrame());
                selectiveBroadcast(con, FrameFactory.createPlayerFrame(newPlayer));
                
                return;
            case FrameDataFactory.LEAVE_FRAME:
                LeaveFrame leaveFrame = (LeaveFrame) frame.getFrameData();
                game.removePlayer(leaveFrame.getClientUUID());
                con.close();
                // In theory does broadcasting in onClose?
                break;
            case FrameDataFactory.GAME_FRAME:
                // Should never receive game frame from client
                break;
            case FrameDataFactory.PLAYER_FRAME:
                Player player = (Player) frame.getFrameData();
                game.updatePlayer(player);
                System.out.println(player.getIdentifier() + " -> " + player.getX() + ", " + player.getY() + ", " + player.getFacing());

                selectiveBroadcast(con, FrameFactory.createPlayerFrame(player));
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
    public void onError(WebSocket con, Exception ex) {
        System.err.println("Client error: " + con.getRemoteSocketAddress());
        con.close();
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
    }

    private synchronized void selectiveBroadcast(WebSocket doNotInclude, Frame frame) {
        for (WebSocket connection : connections.keySet()) {
            if (connection != doNotInclude) {
                connection.send(frame.encodeFrame());
            }
        }
    }
}
