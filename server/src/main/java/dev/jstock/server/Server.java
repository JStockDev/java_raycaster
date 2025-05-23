package dev.jstock.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Server extends WebSocketServer {
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
    }

    @Override
    public void onError(WebSocket connection, Exception ex) {
        System.err.println("Error: " + ex.toString());
    }

    @Override
    public void onStart() {}
}
