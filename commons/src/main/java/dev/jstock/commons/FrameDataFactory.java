package dev.jstock.commons;

import java.nio.ByteBuffer;
import java.util.UUID;

import dev.jstock.commons.Frames.GameFrame;
import dev.jstock.commons.Frames.JoinFrame;
import dev.jstock.commons.Frames.LeaveFrame;
import dev.jstock.commons.Frames.ObjectiveFrame;

// The main factory for holding the frame types, and for decoding the different frame data types.
public class FrameDataFactory {
    // Frame types, identifying the frame type, at position [0]
    public static final byte JOIN_FRAME = 0;
    public static final byte LEAVE_FRAME = 1;
    public static final byte GAME_FRAME = 2;
    public static final byte PLAYER_FRAME = 3;
    public static final byte OBJECTIVE_FRAME = 4;
    public static final byte ERROR_FRAME = 5;

    public static FrameData decodeFrameData(int frameType, byte[] data) {
        // Depending on the frame type, decode the data accordingly
        switch (frameType) {
            case JOIN_FRAME:
                if (data.length != 16) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }
                ByteBuffer rawJoinUUID = ByteBuffer.wrap(data);
                long joinMSB = rawJoinUUID.getLong();
                long joinLSB = rawJoinUUID.getLong();
                UUID joinUUID = new UUID(joinMSB, joinLSB);

                return new JoinFrame(joinUUID);

            case LEAVE_FRAME:
                if (data.length != 16) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }
                ByteBuffer rawUUID = ByteBuffer.wrap(data);
                long leaveMSB = rawUUID.getLong();
                long leaveLSB = rawUUID.getLong();
                UUID leaveUUID = new UUID(leaveMSB, leaveLSB);

                return new LeaveFrame(leaveUUID);

            case PLAYER_FRAME:
                if (data.length != 40) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }

                ByteBuffer buffer = ByteBuffer.wrap(data);
                long uuidMsb = buffer.getLong();
                long uuidLsb = buffer.getLong();
                UUID playerUUID = new UUID(uuidMsb, uuidLsb);

                Double playerX = buffer.getDouble();
                Double playerY = buffer.getDouble();
                Double playerFacing = buffer.getDouble();

                return new Player(playerUUID, playerX, playerY, playerFacing);
            case GAME_FRAME:
                if (data.length < 8) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }

                ByteBuffer gameBuffer = ByteBuffer.wrap(data);

                int mapSize = gameBuffer.getInt();
                int playerBytesLength = gameBuffer.getInt();

                if (data.length != 8 + mapSize + playerBytesLength) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode game frame");
                }

                // Casting is okay since it *should* always be a square map
                // Needs to always be a square due to map size encoding
                int mapLength = (int) Math.sqrt((double) mapSize);

                byte[][] map = new byte[mapLength][mapLength];
                for (int i = 0; i < mapLength; i++) {
                    for (int j = 0; j < mapLength; j++) {
                        map[i][j] = gameBuffer.get();
                    }
                }

                int playerCount = playerBytesLength / 40;
                Player[] players = new Player[playerCount];

                for (int i = 0; i < playerCount; i++) {
                    
                    byte[] rawPlayerData = new byte[40];
                    gameBuffer.get(rawPlayerData);

                    Player player = (Player) decodeFrameData(PLAYER_FRAME, rawPlayerData);
                    players[i] = player;
                }

                return new GameFrame(players, map);

            case OBJECTIVE_FRAME:
                if (data.length != 16) {
                    throw new IndexOutOfBoundsException(
                            "Data array is not the correct size to decode player location");
                }
                
                ByteBuffer rawObjectiveUUID = ByteBuffer.wrap(data);
                long objectiveMSB = rawObjectiveUUID.getLong();
                long objectiveLSB = rawObjectiveUUID.getLong();
                UUID objectiveUUID = new UUID(objectiveMSB, objectiveLSB);

                return new ObjectiveFrame(objectiveUUID);
            default:
                throw new IllegalArgumentException("Invalid frame type");
        }
    }
}
