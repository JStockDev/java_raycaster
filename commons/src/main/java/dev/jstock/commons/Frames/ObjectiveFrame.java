package dev.jstock.commons.Frames;

import java.nio.ByteBuffer;
import java.util.UUID;

import dev.jstock.commons.FrameData;
import dev.jstock.commons.FrameDataFactory;

public class ObjectiveFrame extends FrameData {
    private UUID clientUUID;

    public ObjectiveFrame(UUID clientUUID) {
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
        return FrameDataFactory.OBJECTIVE_FRAME;
    }

    public UUID getClientUUID() {
        return clientUUID;

    }

}
