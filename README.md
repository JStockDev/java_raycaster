# CS1OP-CW1

Module Code: CS1OP  
Assignment Report Title: Project  
Student Number: 33015092  
Actual Hrs: 15hrs
Which artificial intelligence techniques are used in your project?: ChatGPT

## Introduction

Here this project implements a multiplayer maze game where the user has to navigate through the maze and find the blue objective point. The objective of the game is to be the first player to reach the blue objective point! This game utilizes several design techniques, including a DDA ray caster, to turn the 2D map into a 3D maze, a 1D camera space to draw the player sprites and maze objective sprites, a terminal rasterizer turning the ray-casted scene into ASCII characters and rendering direct into a terminal, client-server architecture to allow for a multiplayer experience, using websockets and a frame based architecture for client-server communication, and a commons library to handle networking and game logic.

## Building

Build configuration I have been using:
- OpenJDK 21
- Gradle 8.13

To build the project, run the following command in the root directory of the project:

```bash
gradle :server:build
gradle :client:build
```

To run the project, open seperate terminals for the server and client, and run the following commands in the respective terminals:

```bash
gradle :server:run
```

```bash
gradle :client:run
```

## Configuration

The server and clients can be configured through their respective `server_config.toml` and `client_config.toml` files in the root project directory. Both config files allow port changing, the server allows for a custom map to be implemented, and the client allows for a custom server address to be set.

## Requirements

### Game World:
Create a structured game world with multiple interconnected rooms or locations.
Each location should have descriptions and may include items, puzzles, or NPCs (non-player characters).
- Success, the game world is a maze with multiple interconnected rooms, and the player can navigate through it to find the blue objective point.

### Player System:
Support multiple players interacting simultaneously.
- Success, the game supports multiple players through the multiplayer server system

Players should be able to perform actions such as exploring, picking up items, and interacting with the environment or each other.
- Success, players can explore the maze, and win the game by reaching the blue objective point.

### Game Logic:
Implement rules for player interaction, item management, and progression (e.g., solving puzzles to unlock new areas).

- Success, player logic implemented through raycasting to handle player movement and collision detection, and through sprite detection to handle game objective updates.

Include a clear win condition or objectives.
- Success, the game ends when a player reaches the blue objective point.

### Networking (Optional):

Allow players to connect to a shared game session over a network  
- Success, client-server architecture implements through the use of websockets and data framing

## Assumptions

The client used for accessing and playing the maze game, was built on a terminal interface, as it was assumed the additional requirements were required, however it was not specified whether this was explicitly required.