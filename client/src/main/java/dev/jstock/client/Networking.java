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
    public void onOpen(ServerHandshake handshakedata) {}

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected from server: " + getURI() + " | Code: " + code + " | Reason: " + reason);
        if (remote) {
            System.out.println("Remote disconnection detected.");
        } else {
            System.out.println("Local disconnection.");
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Error occurred: " + ex.getMessage());
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        Frame frame = Frame.decodeBytes(bytes.array());
        frameQueue.add(frame);
    }

    @Override
    public void onMessage(String message) {
    }

}
