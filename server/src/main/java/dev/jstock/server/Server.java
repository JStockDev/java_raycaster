package dev.jstock.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

    public class Server extends WebSocketServer {

        public Server(int port) {
            super(new java.net.InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            conn.send(null);

            // dev.jstock.Frame
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            // Remove player from class
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
                //location other whatever

                synchronized (this) {
                    // Parse the message and update the game state
                    // For example, if the message is a player update, update the player's position
                    // Frame frame = FrameFactory.createPlayerFrame(player);
                    // broadcast(frame.encode());
                }
        }

        @Override
        public void onError(WebSocket connection, Exception ex) {
            System.err.println("Error: " + ex.toString());
        }

        @Override
        public void onStart() {}
    }
