package dev.jstock.client;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import dev.jstock.commons.Frame;

// Networking class
// This implements the raw networking layer for the client
// It includes starting a connection in a non blocking manner, and then decoding any received byte messages into frames and pushing them into a concurrent queue
// This concurrent queue is then read on the main game loop, processing frames when they are present

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

        System.exit(0);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("Error occurred: " + ex.getMessage());
        System.exit(0);
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
