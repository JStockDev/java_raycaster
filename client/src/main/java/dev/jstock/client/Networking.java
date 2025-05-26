package dev.jstock.client;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import dev.jstock.commons.Frame;

public class Networking extends WebSocketClient {

    private ConcurrentLinkedQueue<Frame> frameQueue = new ConcurrentLinkedQueue<>();

    public Networking(URI serverUri, ConcurrentLinkedQueue<Frame> frameQueue) {
        super(serverUri);
        this.frameQueue = frameQueue;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server: " + getURI());
    }

    

    @Override
    public void onClose(int code, String reason, boolean remote) {
        
    }

    @Override
    public void onError(Exception ex) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onError'");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onMessage'");
    }

    @Override
    public void onMessage(String message) {}
    
}
