package dev.jstock.client;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.moandjiezana.toml.Toml;

import dev.jstock.commons.Frame;
import dev.jstock.commons.FrameDataFactory;
import dev.jstock.commons.FrameFactory;
import dev.jstock.commons.Game;
import dev.jstock.commons.Player;
import dev.jstock.commons.Frames.GameFrame;
import dev.jstock.commons.Frames.LeaveFrame;
import dev.jstock.commons.Frames.ObjectiveFrame;

public class Entry {

    // Set some constants for movement and rotation amounts
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
        // Load the config file
        String rawFile = Files.readString(Path.of("./client_config.toml"));
        Config config = new Toml().read(rawFile).to(Config.class);

        // Setup the queue for game loop and networking communication
        ConcurrentLinkedQueue<Frame> frameQueue = new ConcurrentLinkedQueue<>();
        // Setup the networking (websocket)
        Networking networking = new Networking(new URI(config.getServerAddress()), frameQueue);

        // Attempt a server connection
        if (networking.connectBlocking()) {
            System.out.println("Connected to server: " + networking.getURI());
        } else {
            throw new IOException("Failed to connect to server: " + networking.getURI());
        }

        // Generate a unique ID for our player
        UUID clientUUID = UUID.randomUUID();
        // Send a join frame to the server, indicating we want to join a game
        networking.send(FrameFactory.createJoinFrame(clientUUID).encodeFrame());
        while (frameQueue.isEmpty()) {
            Thread.sleep(100);
        }

        // Poll the next frame from the server and throw an exception if it is not a
        // game frame
        Frame joinFrame = frameQueue.poll();
        if (joinFrame.getType() != FrameDataFactory.GAME_FRAME) {
            throw new IOException("Failed to join game: TYPE " + joinFrame.getType());
        }

        // Decode our game frame data and get the map
        Game game = ((GameFrame) joinFrame.getFrameData()).toGame();
        byte[][] map = game.getMap();

        System.out.println("Game joined. You are: " + clientUUID);
        System.out.println("Map size: " + map.length + "x" + map[0].length);
        System.out.println("Players in game: " + game.getPlayers().length);
        System.out.println("Objective at: " + game.getObjectiveX() + ", " + game.getObjectiveY());

        // Find our player in the list;
        Player player = null;
        for (Player netPlayer : game.getPlayers()) {
            if (netPlayer.getIdentifier().equals(clientUUID)) {
                player = netPlayer;
            }
        }

        // Should never happen, but fail just in case we can't find our player
        if (player == null) {
            throw new Exception("Failed to find player with UUID: " + clientUUID);
        }

        // Setup our wall texture buffers
        int textureSize = 64;
        TextColor.RGB[] nsTexture = new TextColor.RGB[textureSize * textureSize];
        TextColor.RGB[] ewTexture = new TextColor.RGB[textureSize * textureSize];

        // Make a wall gradient pattern for north-south walls
        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                int colour = y * (256 / textureSize);
                nsTexture[textureSize * y + x] = new TextColor.RGB(colour, 0, 0);
            }
        }

        // Make a slightly darker wall gradient pattern for east-west walls
        for (int x = 0; x < textureSize; x++) {
            for (int y = 0; y < textureSize; y++) {
                int colour = y * (128 / textureSize);
                ewTexture[textureSize * y + x] = new TextColor.RGB(colour, 0, 0);
            }
        }

        // Open another terminal window for rendering
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        Terminal terminal = factory.createTerminal();

        main_loop: while (true) {
            // Get the terminal size to determine the screen width and height
            TerminalSize size = terminal.getTerminalSize();
            int screenWidth = size.getColumns();
            int screenHeight = size.getRows();

            // Calculate the player's movement offsets based on their facing direction
            double playerMovementXOffset = MOVE_AMOUNT * Math.cos(player.getFacing());
            double playerMovementYOffset = MOVE_AMOUNT * Math.sin(player.getFacing());

            // Sync the frame queue, poll frames, and process them
            synchronized (frameQueue) {
                while (!frameQueue.isEmpty()) {
                    Frame frame = frameQueue.poll();

                    switch (frame.getType()) {
                        case FrameDataFactory.PLAYER_FRAME:
                            // Decode the player frame and add the player into the game
                            Player recvPlayer = (Player) frame.getFrameData();
                            if (game.containsPlayer(recvPlayer.getIdentifier())) {
                                game.updatePlayer(recvPlayer);
                            } else {
                                System.out.println("New player: " + recvPlayer.getIdentifier());
                                game.addPlayer(recvPlayer);
                            }

                            break;
                        case FrameDataFactory.LEAVE_FRAME:
                            // Decode the leave frame and remove the player from the game
                            UUID leavePlayerUUID = ((LeaveFrame) frame.getFrameData()).getClientUUID();
                            game.removePlayer(leavePlayerUUID);
                            System.out.println("Player left: " + leavePlayerUUID);
                            break;

                        case FrameDataFactory.OBJECTIVE_FRAME:
                            // Decode the objective frame and end the game
                            UUID objectiveUUID = ((ObjectiveFrame) frame.getFrameData()).getClientUUID();
                            System.out.println("Player " + objectiveUUID + " wins the game!");

                            System.exit(0);
                        default:
                            break;
                    }
                }
            }

            // Poll the terminal for input
            KeyStroke keyStroke = terminal.pollInput();

            if (keyStroke != null) {
                switch (keyStroke.getKeyType()) {
                    case Character:
                        char key = keyStroke.getCharacter();
                        // Calculate the new players position
                        // Add the offset to the player's position based on the key pressed
                        // Check if the location resides within a wall and if not, update the player's position
                        // If player's position is updated, send the player frame to the server
                        // Check if the player has reached the objective, and send a win frame and end the game

                        switch (key) {
                            case 'w':
                                double posXOffset = player.getX() + playerMovementXOffset;
                                double posYOffset = player.getY() + playerMovementYOffset;

                                if (posXOffset >= 0 && posXOffset < map.length &&
                                        posYOffset >= 0 && posYOffset < map.length &&
                                        map[(int) posXOffset][(int) posYOffset] != 1) {

                                    player.setX(posXOffset);
                                    player.setY(posYOffset);
                                    networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());

                                    int playerMapSquareX = (int) player.getX();
                                    int playerMapSquareY = (int) player.getY();

                                    int objectiveSquareX = (int) (game.getObjectiveX() - 0.5);
                                    int objectiveSquareY = (int) (game.getObjectiveY() - 0.5);

                                    if (playerMapSquareX == objectiveSquareX &&
                                            playerMapSquareY == objectiveSquareY) {
                                        networking.send(FrameFactory.createObjectiveFrame(player.getIdentifier())
                                                .encodeFrame());
                                        System.out.println("You win!");
                                    }
                                }

                                break;

                            case 's':

                                double negativeXOffset = player.getX() - playerMovementXOffset;
                                double negativeYOffset = player.getY() - playerMovementYOffset;

                                if (negativeXOffset >= 0 && negativeXOffset < map.length &&
                                        negativeYOffset >= 0 && negativeYOffset < map.length &&
                                        map[(int) negativeXOffset][(int) negativeYOffset] != 1) {

                                    player.setX(negativeXOffset);
                                    player.setY(negativeYOffset);
                                    networking.send(FrameFactory.createPlayerFrame(player).encodeFrame());

                                    int playerMapSquareX = (int) player.getX();
                                    int playerMapSquareY = (int) player.getY();

                                    int objectiveSquareX = (int) (game.getObjectiveX() - 0.5);
                                    int objectiveSquareY = (int) (game.getObjectiveY() - 0.5);

                                    if (playerMapSquareX == objectiveSquareX &&
                                            playerMapSquareY == objectiveSquareY) {
                                        networking.send(FrameFactory.createObjectiveFrame(player.getIdentifier())
                                                .encodeFrame());
                                        System.out.println("You win!");
                                    }
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
                        System.exit(0);
                        break main_loop;
                    default:
                        break;

                }
            }

            // Calculate sprite amount by getting every other player in the game and add one for the win objective
            int spriteAmount = game.getOtherPlayers(player).length + 1;

            // Create a sprite buffer which will be used to draw sprites on top of the screen after raycasting is done
            double[] spriteBuffer = new double[screenWidth];

            // Create arrays for sprite order and distance, which will be used to sort sprites based on their distance
            int[] spriteOrder = new int[spriteAmount];
            double[] spriteDistance = new double[spriteAmount];

            // Get player position and facing angle
            double playerPosX = player.getX();
            double playerPosY = player.getY();
            double playerAngle = player.getFacing();

            // Calculate a ray angle increment based on the screen width and field of view
            double rayAngle = playerAngle - FOV / 2.0;
            double angleIncrement = FOV / screenWidth;

            double xDrawPos = 0;

            // Set a ceiling color for the top half of the screen
            for (int x = 0; x < screenWidth; x++) {
                for (int y = 0; y < screenHeight / 2; y++) {
                    terminal.setCursorPosition(x, y);
                    terminal.setForegroundColor(new TextColor.RGB(50, 50, 50));
                    terminal.putCharacter('█');
                }
            }


            // Main raycasting loop
            while (rayAngle < playerAngle + FOV / 2.0) {
                double horizontalRayAngle = rayAngle;
                double xStep = 0;
                
                // Determine which way to step and check for walls depending on player position
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

                // Calculate the initial size of the step based on the player angle
                double yStep = xStep * Math.tan(horizontalRayAngle);

                // Initial ray step to check in
                double horizontalMapX = playerPosX + xStep;
                double horizontalMapY = playerPosY + yStep;
                double horizontalRayLength = -1.0;

                // Calculate the next step size
                yStep = yStep * (1.0 / xStep);

                while (horizontalMapX < map.length &&
                        horizontalMapX > 0.0 &&
                        horizontalMapY < map.length &&
                        horizontalMapY > 0.0) {
                    // Check if the ray is hitting a wall at the current step
                    if (Math.signum(Math.cos(horizontalRayAngle)) >= 0.0) {
                        if (map[(int) horizontalMapX][(int) horizontalMapY] == 1) {
                            // If so, perform pythagorean theorem to calculate the ray length
                            horizontalRayLength = Math.sqrt(Math.pow(horizontalMapX - playerPosX, 2.0)
                                    + Math.pow(horizontalMapY - playerPosY, 2.0));
                            break;
                        }

                        // If not, continue stepping in the ray direction
                        horizontalMapX += 1.0;
                        horizontalMapY += yStep;
                    } else {
                        // Same process repeated
                        if (map[(int) (horizontalMapX - 1.0)][(int) horizontalMapY] == 1) {
                            horizontalRayLength = Math.sqrt(Math.pow(horizontalMapX - playerPosX, 2.0)
                                    + Math.pow(horizontalMapY - playerPosY, 2.0));
                            break;
                        }

                        horizontalMapX -= 1.0;
                        horizontalMapY -= yStep;
                    }
                }


                // If no wall was hit, set the ray length to a maximum value
                // This is done as the shorter ray is always chosen at the end                
                if (horizontalRayLength == -1.0) {
                    horizontalRayLength = Double.MAX_VALUE;
                }

                // Now we do the repeat the process for vertical rays
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
                        if (map[(int) verticalMapX][(int) verticalMapY] == 1) {
                            verticalRayLength = Math.sqrt(Math.pow(verticalMapX - playerPosX, 2.0)
                                    + Math.pow(verticalMapY - playerPosY, 2.0));
                            break;
                        }

                        verticalMapX += xStep;
                        verticalMapY += 1.0;
                    } else {
                        if (map[(int) verticalMapX][(int) (verticalMapY - 1.0)] == 1) {
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

                // Initialize our final ray length and wall hit location variables

                double rayLength = 0.0;
                double wallHitLocation = 0.0;

                // Set the final ray length to the shortest ray length
                if (horizontalRayLength < verticalRayLength) {
                    rayLength = horizontalRayLength;
                    wallHitLocation = horizontalMapY - Math.floor(horizontalMapY);
                } else {
                    rayLength = verticalRayLength;
                    wallHitLocation = verticalMapX - Math.floor(verticalMapX);
                }

                // Calculate the x draw position based on the ray angle and screen width
                int textureX = (int) (wallHitLocation * textureSize);

                // The current render would be subjected to the Euclidean fisheye effect, apply a correction basic on the player and ray angle
                double correctedRay = rayLength * Math.cos(rayAngle - playerAngle);
                // Caulculate the x draw position based on the ray angle and screen width
                double rayHeight = screenHeight / correctedRay;
                double windowOffset = (screenHeight - rayHeight) / 2.0;

                // Calculate the start and end row for the ray to be drawn in the terminal
                int column = (int) xDrawPos;
                int startRow = (int) windowOffset;
                int endRow = (int) (screenHeight - windowOffset);
                startRow = Math.max(0, startRow);
                endRow = Math.min(screenHeight - 1, endRow);

                // Add the ray distance to the sprite buffer for the column
                if (column >= 0 && column < screenWidth) {
                    spriteBuffer[column] = correctedRay;
                }

                // Do some texture height calculations
                double textureDrawStep = textureSize / rayHeight;
                double textureDrawPos = (startRow - screenHeight / 2.0 + rayHeight / 2.0) * textureDrawStep;

                for (int row = startRow; row <= endRow; row++) {
                    int textureY = (int) textureDrawPos & (textureSize - 1);

                    // Set the texture to be used depending on whether the wall hit was North-South or East-West
                    if (horizontalRayLength < verticalRayLength) {
                        terminal.setForegroundColor(nsTexture[textureSize * textureY + textureX]);

                    } else {
                        terminal.setForegroundColor(ewTexture[textureSize * textureY + textureX]);
                    }

                    // Set the cursor position and add the appropriate character to the terminal
                    terminal.setCursorPosition(column, row);
                    terminal.putCharacter('█');

                    // Increment the texture draw position for the next row
                    textureDrawPos += textureDrawStep;
                }

                // Increment the x draw position and ray angle for the next ray
                xDrawPos += 1.0;
                rayAngle += angleIncrement;

            }

            // Initialize the sprite array and fill it with the objective and other players
            Sprite[] sprites = new Sprite[spriteAmount];

            // Objective sprite
            sprites[0] = new Sprite(game.getObjectiveX(), game.getObjectiveY(), new TextColor.RGB(0, 0, 255));

            // Players
            for (int i = 1; i < spriteAmount; i++) {
                Player sprite = game.getOtherPlayers(player)[i - 1];
                sprites[i] = new Sprite(sprite.getX(), sprite.getY(), new TextColor.RGB(0, 255, 0));
            }

            // For each of our sprites, calculate the players distance relative to the sprite position
            for (int i = 0; i < spriteAmount; i++) {
                Sprite sprite = sprites[i];
                double spriteX = sprite.getX();
                double spriteY = sprite.getY();

                double spriteDist = Math.pow(playerPosX - spriteX, 2.0) + Math.pow(playerPosY - spriteY, 2.0);

                spriteOrder[i] = i;
                spriteDistance[i] = spriteDist;
            }

            // Sort the sprites based on their distance from the player, to ensure they are properly draw on top of each other
            SpriteSorter.sortSprites(spriteOrder, spriteDistance);

            // Do a load of maths which I can't honestly remember how it works, to calculate which part of the sprite to draw
            double dirX = Math.cos(playerAngle);
            double dirY = Math.sin(playerAngle);
            double planeX = -dirY * Math.tan(FOV / 2);
            double planeY = dirX * Math.tan(FOV / 2);

            for (int i = 0; i < spriteAmount; i++) {
                double spriteX = sprites[i].getX() - playerPosX;
                double spriteY = sprites[i].getY() - playerPosY;

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
                                terminal.setForegroundColor(sprites[i].getColor());
                                terminal.setCursorPosition(stripe, y);
                                terminal.putCharacter('█');
                            }
                        }
                    }
                }
            }

            // Make sure all the terminal characters are flushed and draw to the screen
            terminal.flush();
            // Wait a bit so the screen doesn't update too quickly
            Thread.sleep(10);

            // Clear the screen, reset the colours and continue the loop
            terminal.clearScreen();
            terminal.resetColorAndSGR();
        }
    }
}