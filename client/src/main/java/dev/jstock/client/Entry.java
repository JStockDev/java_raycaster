package dev.jstock.client;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import dev.jstock.commons.Frame;
import dev.jstock.commons.FrameDataFactory;
import dev.jstock.commons.FrameFactory;
import dev.jstock.commons.Game;
import dev.jstock.commons.Player;
import dev.jstock.commons.Frames.GameFrame;
import dev.jstock.commons.Frames.LeaveFrame;

public class Entry {

    public static final double MOVE_AMOUNT = 0.02;
    public static final double ROTATE_AMOUNT = 0.05;
    public static final double FOV = Math.PI / 3.0;

    public static void main(String[] args) {
        try {
            terminalRayCaster();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void terminalRayCaster() throws Exception {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        Terminal terminal = factory.createTerminal();

        ConcurrentLinkedQueue<Frame> frameQueue = new ConcurrentLinkedQueue<>();
        Networking networking = new Networking(new URI("ws://localhost:45777"), frameQueue);

        if (networking.connectBlocking()) {
            System.out.println("Connected to server: " + networking.getURI());
        } else {
            throw new IOException("Failed to connect to server: " + networking.getURI());
        }

        UUID clientUUID = UUID.randomUUID();
        networking.send(FrameFactory.createJoinFrame(clientUUID).encodeFrame());
        while (frameQueue.isEmpty()) {
            Thread.sleep(100);
        }

        Frame joinFrame = frameQueue.poll();

        if (joinFrame.getType() != FrameDataFactory.GAME_FRAME) {
            throw new IOException("Failed to join game: TYPE " + joinFrame.getType());
        }

        Game game = ((GameFrame) joinFrame.getFrameData()).toGame();
        byte[][] map = game.getMap();

        System.out.println("Game joined. Map size: " + map.length);

        Player player = null;

        for (Player netPlayer : game.getPlayers()) {
            if (netPlayer.getIdentifier().equals(clientUUID)) {
                player = netPlayer;
            }
        }

        if (player == null) {
            throw new Exception("Failed to find player with UUID: " + clientUUID);
        }

        int textureSize = 64;
        TextColor.RGB[] nsTexture = new TextColor.RGB[textureSize * textureSize];
        TextColor.RGB[] ewTexture = new TextColor.RGB[textureSize * textureSize];

        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                int colour = y * (256 / textureSize);
                nsTexture[textureSize * y + x] = new TextColor.RGB(colour, 0, 0);
            }
        }

        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                int colour = y * (128 / textureSize);
                ewTexture[textureSize * y + x] = new TextColor.RGB(colour, 0, 0);
            }
        }

        main_loop: while (true) {
            TerminalSize size = terminal.getTerminalSize();
            int screenWidth = size.getColumns();
            int screenHeight = size.getRows();

            double playerMovementXOffset = MOVE_AMOUNT * Math.cos(player.getFacing());
            double playerMovementYOffset = MOVE_AMOUNT * Math.sin(player.getFacing());

            synchronized (frameQueue) {
                while (!frameQueue.isEmpty()) {
                    Frame frame = frameQueue.poll();

                    switch (frame.getType()) {
                        case FrameDataFactory.PLAYER_FRAME:
                            Player recvPlayer = (Player) frame.getFrameData();
                            if (game.containsPlayer(recvPlayer.getIdentifier())) {
                                game.updatePlayer(recvPlayer);
                                System.out.println(recvPlayer.getIdentifier() + "-> " + recvPlayer.getX() + ", "
                                        + recvPlayer.getY() + ", " + recvPlayer.getFacing());
                            } else {
                                System.out.println("New player: " + recvPlayer.getIdentifier());
                                game.addPlayer(recvPlayer);
                            }

                            break;
                        case FrameDataFactory.LEAVE_FRAME:
                            UUID leavePlayerUUID = ((LeaveFrame) frame.getFrameData()).getClientUUID();
                            game.removePlayer(leavePlayerUUID);
                            System.out.println("Player left: " + leavePlayerUUID);
                            break;

                        default:
                            break;
                    }
                }
            }

            KeyStroke keyStroke = terminal.pollInput();

            if (keyStroke != null) {
                switch (keyStroke.getKeyType()) {
                    case Character:
                        char key = keyStroke.getCharacter();

                        switch (key) {
                            case 'w':
                                double posXOffset = player.getX() + playerMovementXOffset;
                                double posYOffset = player.getY() + playerMovementYOffset;

                                if (posXOffset >= 0 && posXOffset < map.length &&
                                        posYOffset >= 0 && posYOffset < map.length &&
                                        map[(int) posXOffset][(int) posYOffset] == 0) {

                                    player.setX(posXOffset);
                                    player.setY(posYOffset);
                                    networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());
                                }

                                break;

                            case 's':

                                double negativeXOffset = player.getX() - playerMovementXOffset;
                                double negativeYOffset = player.getY() - playerMovementYOffset;

                                if (negativeXOffset >= 0 && negativeXOffset < map.length &&
                                        negativeYOffset >= 0 && negativeYOffset < map.length &&
                                        map[(int) negativeXOffset][(int) negativeYOffset] == 0) {

                                    player.setX(negativeXOffset);
                                    player.setY(negativeYOffset);
                                    networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());
                                }

                                break;

                            case 'a':
                                player.setFacing(player.getFacing() - ROTATE_AMOUNT);
                                networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());
                                break;

                            case 'd':
                                player.setFacing(player.getFacing() + ROTATE_AMOUNT);
                                networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());
                                break;

                            default:
                                break;
                        }

                        break;
                    case Escape:
                        break main_loop;
                    default:
                        break;

                }
            }

            int spriteAmount = game.getOtherPlayers(player).length;

            double[] spriteBuffer = new double[screenWidth];

            int[] spriteOrder = new int[spriteAmount];
            double[] spriteDistance = new double[spriteAmount];

            double playerPosX = player.getX();
            double playerPosY = player.getY();
            double playerAngle = player.getFacing();

            double rayAngle = playerAngle - FOV / 2.0;
            double angleIncrement = FOV / screenWidth;

            double xDrawPos = 0;

            for (int x = 0; x < screenWidth; x++) {
                for (int y = 0; y < screenHeight / 2; y++) {
                    terminal.setCursorPosition(x, y);
                    terminal.setForegroundColor(new TextColor.RGB(50, 50, 50));
                    terminal.putCharacter('█');
                }
            }

            while (rayAngle < playerAngle + FOV / 2.0) {
                double horizontalRayAngle = rayAngle;
                double xStep = 0;

                if (Math.signum(Math.cos(horizontalRayAngle)) >= 0) {
                    double offset = Math.ceil(playerPosX) - playerPosX;

                    if (offset == 0.0) {
                        xStep = 1.0;
                    } else {
                        xStep = offset;
                    }
                } else {
                    double offset = Math.floor(playerPosX) - playerPosX;

                    if (offset == 0.0) {
                        xStep = -1.0;
                    } else {
                        xStep = offset;
                    }
                }

                double yStep = xStep * Math.tan(horizontalRayAngle);

                double horizontalMapX = playerPosX + xStep;
                double horizontalMapY = playerPosY + yStep;
                double horizontalRayLength = -1.0;

                yStep = yStep * (1.0 / xStep);

                while (horizontalMapX < map.length &&
                        horizontalMapX > 0.0 &&
                        horizontalMapY < map.length &&
                        horizontalMapY > 0.0) {
                    if (Math.signum(Math.cos(horizontalRayAngle)) >= 0.0) {
                        if (map[(int) horizontalMapX][(int) horizontalMapY] > 0) {
                            horizontalRayLength = Math.sqrt(Math.pow(horizontalMapX - playerPosX, 2.0)
                                    + Math.pow(horizontalMapY - playerPosY, 2.0));
                            break;
                        }

                        horizontalMapX += 1.0;
                        horizontalMapY += yStep;
                    } else {
                        if (map[(int) (horizontalMapX - 1.0)][(int) horizontalMapY] > 0) {
                            horizontalRayLength = Math.sqrt(Math.pow(horizontalMapX - playerPosX, 2.0)
                                    + Math.pow(horizontalMapY - playerPosY, 2.0));
                            break;
                        }

                        horizontalMapX -= 1.0;
                        horizontalMapY -= yStep;
                    }
                }

                if (horizontalRayLength == -1.0) {
                    horizontalRayLength = Double.MAX_VALUE;
                }

                double verticalRayAngle = Math.PI / 2.0 - rayAngle;

                yStep = 0;

                if (Math.signum(Math.cos(verticalRayAngle)) >= 0.0) {
                    double offset = Math.ceil(playerPosY) - playerPosY;

                    if (offset == 0.0) {
                        yStep = 1.0;
                    } else {
                        yStep = offset;
                    }
                } else {
                    double offset = Math.floor(playerPosY) - playerPosY;

                    if (offset == 0.0) {
                        yStep = -1.0;
                    } else {
                        yStep = offset;
                    }
                }

                xStep = yStep * Math.tan(verticalRayAngle);

                double verticalMapX = playerPosX + xStep;
                double verticalMapY = playerPosY + yStep;
                double verticalRayLength = -1.0;

                xStep = xStep * (1.0 / yStep);

                while (verticalMapX < map.length &&
                        verticalMapX > 0.0 &&
                        verticalMapY < map.length &&
                        verticalMapY > 0.0) {
                    if (Math.signum(Math.cos(verticalRayAngle)) == 1.0) {
                        if (map[(int) verticalMapX][(int) verticalMapY] > 0) {
                            verticalRayLength = Math.sqrt(Math.pow(verticalMapX - playerPosX, 2.0)
                                    + Math.pow(verticalMapY - playerPosY, 2.0));
                            break;
                        }

                        verticalMapX += xStep;
                        verticalMapY += 1.0;
                    } else {
                        if (map[(int) verticalMapX][(int) (verticalMapY - 1.0)] > 0) {
                            verticalRayLength = Math.sqrt(Math.pow(verticalMapX - playerPosX, 2.0)
                                    + Math.pow(verticalMapY - playerPosY, 2.0));
                            break;
                        }

                        verticalMapX -= xStep;
                        verticalMapY -= 1.0;
                    }
                }

                if (verticalRayLength == -1.0) {
                    verticalRayLength = Double.MAX_VALUE;
                }

                double rayLength = 0.0;
                double wallHitLocation = 0.0;

                if (horizontalRayLength < verticalRayLength) {
                    rayLength = horizontalRayLength;
                    wallHitLocation = horizontalMapY - Math.floor(horizontalMapY);
                } else {
                    rayLength = verticalRayLength;
                    wallHitLocation = verticalMapX - Math.floor(verticalMapX);
                }

                int textureX = (int) (wallHitLocation * textureSize);

                double correctedRay = rayLength * Math.cos(rayAngle - playerAngle);
                double rayHeight = screenHeight / correctedRay;
                double windowOffset = (screenHeight - rayHeight) / 2.0;

                int column = (int) xDrawPos;
                int startRow = (int) windowOffset;
                int endRow = (int) (screenHeight - windowOffset);
                startRow = Math.max(0, startRow);
                endRow = Math.min(screenHeight - 1, endRow);

                if (column >= 0 && column < screenWidth) {
                    spriteBuffer[column] = correctedRay;
                }

                double textureDrawStep = textureSize / rayHeight;
                double textureDrawPos = (startRow - screenHeight / 2.0 + rayHeight / 2.0) * textureDrawStep;

                for (int row = startRow; row <= endRow; row++) {
                    int textureY = (int) textureDrawPos & (textureSize - 1);

                    if (horizontalRayLength < verticalRayLength) {
                        terminal.setForegroundColor(nsTexture[textureSize * textureY + textureX]);

                    } else {
                        terminal.setForegroundColor(ewTexture[textureSize * textureY + textureX]);
                    }

                    terminal.setCursorPosition(column, row);
                    terminal.putCharacter('█');

                    textureDrawPos += textureDrawStep;
                }

                xDrawPos += 1.0;
                rayAngle += angleIncrement;

            }

            for (int i = 0; i < spriteAmount; i++) {
                Player sprite = game.getOtherPlayers(player)[i];
                double spriteX = sprite.getX();
                double spriteY = sprite.getY();

                double spriteDist = Math.pow(playerPosX - spriteX, 2.0) + Math.pow(playerPosY - spriteY, 2.0);

                spriteOrder[i] = i;
                spriteDistance[i] = spriteDist;
            }

            SpriteSorter.sortSprites(spriteOrder, spriteDistance);

            double dirX = Math.cos(playerAngle);
            double dirY = Math.sin(playerAngle);
            double planeX = -dirY * Math.tan(FOV / 2);
            double planeY = dirX * Math.tan(FOV / 2);

            for (int i = 0; i < spriteAmount; i++) {
                double spriteX = game.getOtherPlayers(player)[spriteOrder[i]].getX() - playerPosX;
                double spriteY = game.getOtherPlayers(player)[spriteOrder[i]].getY() - playerPosY;

                double invDet = 1.0 / (planeX * dirY - dirX * planeY);

                double transformX = invDet * (dirY * spriteX - dirX * spriteY);
                double transformY = invDet * (-planeY * spriteX + planeX * spriteY);

                if (transformY > 0) {
                    int spriteScreenX = (int) ((screenWidth / 2) * (1 + transformX / transformY));
                    int spriteHeight = Math.abs((int) (screenHeight / transformY));

                    int drawStartY = -spriteHeight / 2 + ((int) (screenHeight / 1.85));
                    if (drawStartY < 0) {
                        drawStartY = 0;
                    }
                    int drawEndY = spriteHeight / 2 + ((int) (screenHeight / 1.85));
                    if (drawEndY >= screenHeight) {
                        drawEndY = screenHeight - 1;
                    }

                    int spriteWidth = Math.abs((int) (screenHeight / (transformY)));

                    int drawStartX = -spriteWidth / 2 + spriteScreenX;
                    if (drawStartX < 0) {
                        drawStartX = 0;
                    }
                    int drawEndX = spriteWidth / 2 + spriteScreenX;
                    if (drawEndX >= screenWidth) {
                        drawEndX = screenWidth - 1;
                    }

                    for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
                        if (stripe >= 0 && stripe < screenWidth && transformY < spriteBuffer[stripe]) {
                            for (int y = drawStartY; y < drawEndY; y++) {
                                terminal.setForegroundColor(new TextColor.RGB(0, 255, 0));
                                terminal.setCursorPosition(stripe, y);
                                terminal.putCharacter('█');
                            }
                        }
                    }
                }
            }

            terminal.flush();
            Thread.sleep(10);

            terminal.clearScreen();
            terminal.resetColorAndSGR();
        }
    }
}