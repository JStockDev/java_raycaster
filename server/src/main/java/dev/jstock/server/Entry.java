package dev.jstock.server;

import java.net.ServerSocket;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import com.moandjiezana.toml.Toml;

import dev.jstock.commons.FrameData;
import dev.jstock.commons.Game;

public class Entry {
    public static byte[][] MAP;

    public static void main(String[] args) {
        // Server server = new Server(45777, );
        
        Toml toml = new Toml().read("./config.toml");
        Config config = toml.to(Config.class);
        
        MAP = config.getMap();
        Server server = new Server(config.getPort());

    }

    
}

