package dev.jstock.client;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_KEYDOWN;
import static io.github.libsdl4j.api.event.SDL_EventType.SDL_QUIT;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_A;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_D;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_ESCAPE;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_S;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.SDLK_W;
import static io.github.libsdl4j.api.render.SDL_RendererFlags.SDL_RENDERER_ACCELERATED;
import static io.github.libsdl4j.api.render.SdlRender.SDL_CreateRenderer;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderClear;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderDrawLine;
import static io.github.libsdl4j.api.render.SdlRender.SDL_RenderPresent;
import static io.github.libsdl4j.api.render.SdlRender.SDL_SetRenderDrawColor;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_OPENGL;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_RESIZABLE;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

import java.io.IOException;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.video.SDL_Window;

public class Entry {
    public static final int[][] MAP = {
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
            { 1, 0, 1, 0, 0, 0, 1, 0, 0, 1 },
            { 1, 0, 1, 0, 1, 0, 1, 0, 0, 1 },
            { 1, 0, 1, 0, 1, 0, 1, 0, 0, 1 },
            { 1, 0, 0, 0, 1, 0, 0, 0, 0, 1 },
            { 1, 0, 1, 0, 1, 1, 0, 0, 0, 1 },
            { 1, 0, 1, 0, 0, 1, 0, 0, 0, 1 },
            { 1, 0, 1, 1, 1, 1, 1, 1, 0, 1 },
            { 1, 0, 0, 0, 1, 0, 0, 0, 0, 1 },
            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
    };

    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 540;

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

    public static void terminalRayCaster() throws IOException {
        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        Terminal terminal = factory.createTerminal();

        double playerPosX = 1.5;
        double playerPosY = 1.5;
        double playerAngle = 0.0;

        main_loop: while (true) {
            TerminalSize size = terminal.getTerminalSize();
            int screenWidth = size.getColumns();
            int screenHeight = size.getRows();

            double playerMovementXOffset = MOVE_AMOUNT * Math.cos(playerAngle);
            double playerMovementYOffset = MOVE_AMOUNT * Math.sin(playerAngle);

            while (true) {
                KeyStroke keyStroke = terminal.pollInput();

                if (keyStroke != null) {
                    switch (keyStroke.getKeyType()) {
                        case Character:
                            char key = keyStroke.getCharacter();

                            switch (key) {
                                case 'w':
                                    playerPosX += playerMovementXOffset;
                                    playerPosY += playerMovementYOffset;
                                    break;
                                case 's':
                                    playerPosX -= playerMovementXOffset;
                                    playerPosY -= playerMovementYOffset;
                                    break;
                                case 'a':
                                    playerAngle -= ROTATE_AMOUNT;
                                    break;
                                case 'd':
                                    playerAngle += ROTATE_AMOUNT;
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
                } else {
                    break;
                }
            }

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

                while (horizontalMapX < MAP.length &&
                        horizontalMapX > 0.0 &&
                        horizontalMapY < MAP.length &&
                        horizontalMapY > 0.0) {
                    if (Math.signum(Math.cos(horizontalRayAngle)) >= 0.0) {
                        if (MAP[(int) horizontalMapX][(int) horizontalMapY] > 0) {
                            horizontalRayLength = Math.sqrt(Math.pow(horizontalMapX - playerPosX, 2.0)
                                    + Math.pow(horizontalMapY - playerPosY, 2.0));
                            break;
                        }

                        horizontalMapX += 1.0;
                        horizontalMapY += yStep;
                    } else {
                        if (MAP[(int) (horizontalMapX - 1.0)][(int) horizontalMapY] > 0) {
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

                while (verticalMapX < MAP.length &&
                        verticalMapX > 0.0 &&
                        verticalMapY < MAP.length &&
                        verticalMapY > 0.0) {
                    if (Math.signum(Math.cos(verticalRayAngle)) == 1.0) {
                        if (MAP[(int) verticalMapX][(int) verticalMapY] > 0) {
                            verticalRayLength = Math.sqrt(Math.pow(verticalMapX - playerPosX, 2.0)
                                    + Math.pow(verticalMapY - playerPosY, 2.0));
                            break;
                        }

                        verticalMapX += xStep;
                        verticalMapY += 1.0;
                    } else {
                        if (MAP[(int) verticalMapX][(int) (verticalMapY - 1.0)] > 0) {
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

            try {
                Thread.sleep(10);
            } catch (Exception e) {
                // Should never fail?
            }

            terminal.clearScreen();
            terminal.resetColorAndSGR();
        }
    }
}
