package dev.jstock;

import java.lang.Math;
import java.lang.Thread;

import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.video.SDL_Window;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.*;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.keycode.SDL_Keycode.*;
import static io.github.libsdl4j.api.render.SDL_RendererFlags.SDL_RENDERER_ACCELERATED;
import static io.github.libsdl4j.api.render.SdlRender.*;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_RESIZABLE;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_OPENGL;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

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
        if (SDL_Init(SDL_INIT_EVERYTHING) != 0) {
            throw new IllegalStateException("Unable to initialize SDL library: " + SDL_GetError());
        }

        SDL_Window window = SDL_CreateWindow("Ray caster", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, SCREEN_WIDTH,
                SCREEN_HEIGHT, SDL_WINDOW_SHOWN | SDL_WINDOW_RESIZABLE | SDL_WINDOW_OPENGL);
        if (window == null) {
            throw new IllegalStateException("Unable to create SDL window: " + SDL_GetError());
        }

        SDL_Renderer renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
        if (renderer == null) {
            throw new IllegalStateException("Unable to create SDL renderer: " + SDL_GetError());
        }
        SDL_Event evt = new SDL_Event();

        // Set color of renderer to green

        double playerPosX = 1.5;
        double playerPosY = 1.5;
        double playerAngle = 0.0;

        main_loop: while (true) {
            SDL_SetRenderDrawColor(renderer, (byte) 0, (byte) 0, (byte) 0, (byte) 255);
            SDL_RenderClear(renderer);

            double playerMovementXOffset = MOVE_AMOUNT * Math.cos(playerAngle);
            double playerMovementYOffset = MOVE_AMOUNT * Math.sin(playerAngle);

            while (SDL_PollEvent(evt) != 0) {
                switch (evt.type) {
                    case SDL_QUIT:
                        break main_loop;
                    case SDL_KEYDOWN:
                        switch (evt.key.keysym.sym) {
                            case SDLK_W:
                                playerPosX += playerMovementXOffset;
                                playerPosY += playerMovementYOffset;
                                break;
                            case SDLK_S:
                                playerPosX -= playerMovementXOffset;
                                playerPosY -= playerMovementYOffset;
                                break;
                            case SDLK_A:
                                playerAngle -= ROTATE_AMOUNT;
                                break;
                            case SDLK_D:
                                playerAngle += ROTATE_AMOUNT;
                                break;
                            case SDLK_ESCAPE:
                                break main_loop;
                            default:
                                break;
                        }
                }
            }

            double rayAngle = playerAngle - FOV / 2.0;
            double angleIncrement = FOV / SCREEN_WIDTH;

            double xDrawPos = 0;

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
                        verticalMapY > 0.0) 
                {
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
                    SDL_SetRenderDrawColor(renderer, (byte) 255, (byte) 0, (byte) 0, (byte) 255);
                } else {
                    rayLength = verticalRayLength;
                    SDL_SetRenderDrawColor(renderer, (byte) 205, (byte) 0, (byte) 0, (byte) 255);
                }

                double correctRay = rayLength * Math.cos(rayAngle - playerAngle);

                double rayHeight = SCREEN_HEIGHT / correctRay;
                double windowOffset = (SCREEN_HEIGHT - rayHeight) / 2.0;

                SDL_RenderDrawLine(renderer, (int) xDrawPos, (int) windowOffset, (int) xDrawPos,
                        (int) (SCREEN_HEIGHT - windowOffset));

                xDrawPos += 1.0;
                rayAngle += angleIncrement;

                
            }
            // try {
                // Thread.sleep(10);
                // } catch (Exception e) {
                // e.printStackTrace();
                // }
            SDL_RenderPresent(renderer);
        }
        SDL_Quit();
    }
}
