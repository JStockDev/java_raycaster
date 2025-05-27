package dev.jstock.commons.Frames;

import java.nio.ByteBuffer;
import java.util.UUID;

import dev.jstock.commons.FrameData;
import dev.jstock.commons.FrameDataFactory;

public class JoinFrame extends FrameData {
    private UUID clientUUID;

    public JoinFrame(UUID clientUUID) {
        this.clientUUID = clientUUID;
    }

    @Override
    public byte[] encode() {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(clientUUID.getMostSignificantBits());
        buffer.putLong(clientUUID.getLeastSignificantBits());

        return buffer.array();
    }

    @Override
    public byte getFrameIdentifier() {
        return FrameDataFactory.JOIN_FRAME;
    }

    public UUID getClientUUID() {
        return clientUUID;
    }
}
