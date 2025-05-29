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
import dev.jstock.commons.Frames.ObjectiveFrame;

public class Server extends WebSocketServer {

    public Server(int port) {
        super(new java.net.InetSocketAddress(port));
    }


    // Store connections with their associated player UUIDs
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

        // Remove the player from the game if they were connected, and notify all other clients about which client left
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


    // Handle incoming byte messages
    @Override
    public void onMessage(WebSocket con, ByteBuffer blob) {

        // Decode the frame and get a *synchronised* game instance to ensure thread safety
        Frame frame = Frame.decodeBytes(blob.array());
        Game game = GameSingleton.getInstance();


        // Process the frame data by its respective frame type
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
                
                // Add the player into the game, set its starting position, broadcast the current game state to the new player, nd notify other clients about the new player
                Player newPlayer = new Player(joinFrame.getClientUUID(), game.getStartingX(), game.getStartingY(), 0.0);
                game.addPlayer(newPlayer);

                con.send(FrameFactory.createGameFrame(game).encodeFrame());
                selectiveBroadcast(con, FrameFactory.createPlayerFrame(newPlayer));

                return;
            case FrameDataFactory.LEAVE_FRAME:
                // Remove the player from the game and notify other clients about the player leaving
                // Note: The connection is only closed here, the rest of the leave logic is handled in onClose
                LeaveFrame leaveFrame = (LeaveFrame) frame.getFrameData();
                game.removePlayer(leaveFrame.getClientUUID());
                con.close();
                break;
            case FrameDataFactory.PLAYER_FRAME:
                // Retrieve the players new position
                Player player = (Player) frame.getFrameData();
                game.updatePlayer(player);

                // Log the players new location
                System.out.println(player.getIdentifier() + " -> " + player.getX() + ", " + player.getY() + ", "
                        + player.getFacing());

                // Broadcast the updated player position to all other clients
                selectiveBroadcast(con, FrameFactory.createPlayerFrame(player));
                return;
            case FrameDataFactory.OBJECTIVE_FRAME:

                // Process the game win, and broadcast the winning player to all clients
                ObjectiveFrame objectiveFrame = (ObjectiveFrame) frame.getFrameData();
                System.out.println("Player " + objectiveFrame.getClientUUID() + " wins!");
                selectiveBroadcast(con, FrameFactory.createObjectiveFrame(objectiveFrame.getClientUUID()));
                
                this.getConnections().forEach(connection -> {
                    connection.close();
                });
                
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
