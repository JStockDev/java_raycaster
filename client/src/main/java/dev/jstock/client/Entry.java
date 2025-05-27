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

        if (joinFrame == null || joinFrame.getType() != FrameDataFactory.GAME_FRAME) {
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
                                System.out.println(recvPlayer.getIdentifier() + "-> " + recvPlayer.getX() + ", " + recvPlayer.getY() + ", " + recvPlayer.getFacing());
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
                                player.setX(player.getX() + playerMovementXOffset);
                                player.setY(player.getY() + playerMovementYOffset);

                                networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());
                                break;

                            case 's':
                                player.setX(player.getX() - playerMovementXOffset);
                                player.setY(player.getY() - playerMovementYOffset);

                                networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());
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

            double playerPosX = player.getX();
            double playerPosY = player.getY();
            double playerAngle = player.getFacing();

            double rayAngle = playerAngle - FOV / 2.0;
            double angleIncrement = FOV / screenWidth;

            double xDrawPos = 0;

            int currentTextMapX = 0;
            int currentTextMapY = 0;

            // Unused: was potentially used for proper lighting, texturing
            // int textMapX = 0;
            // int textMapY = 0;

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

                if (horizontalRayLength < verticalRayLength) {
                    rayLength = horizontalRayLength;
                    currentTextMapX = (int) horizontalMapX;
                    currentTextMapY = (int) horizontalMapY;

                    if ((currentTextMapX + currentTextMapY) % 2 == 0) {
                        terminal.setForegroundColor(new TextColor.RGB(255, 0, 0));
                    } else {
                        terminal.setForegroundColor(new TextColor.RGB(230, 0, 0));
                    }

                } else {
                    rayLength = verticalRayLength;
                    currentTextMapX = (int) verticalMapX;
                    currentTextMapY = (int) verticalMapY;

                    if ((currentTextMapX + currentTextMapY) % 2 == 0) {
                        terminal.setForegroundColor(new TextColor.RGB(205, 0, 0));
                    } else {
                        terminal.setForegroundColor(new TextColor.RGB(180, 0, 0));
                    }
                }

                double correctRay = rayLength * Math.cos(rayAngle - playerAngle);

                double rayHeight = screenHeight / correctRay;
                double windowOffset = (screenHeight - rayHeight) / 2.0;

                int column = (int) xDrawPos;
                int startRow = (int) windowOffset;
                int endRow = (int) (screenHeight - windowOffset);

                startRow = Math.max(0, startRow);
                endRow = Math.min(screenHeight - 1, endRow);

                for (int row = startRow; row <= endRow; row++) {
                    terminal.setCursorPosition(column, row);
                    terminal.putCharacter('█');
                }

                xDrawPos += 1.0;
                rayAngle += angleIncrement;

            }
            terminal.flush();
            Thread.sleep(10);

            terminal.clearScreen();
            terminal.resetColorAndSGR();
        }
    }
}
