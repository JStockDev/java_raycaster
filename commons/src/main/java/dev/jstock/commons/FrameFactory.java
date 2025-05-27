package dev.jstock.commons;

import java.util.UUID;

import dev.jstock.commons.Frames.GameFrame;
import dev.jstock.commons.Frames.JoinFrame;
import dev.jstock.commons.Frames.LeaveFrame;

public class FrameFactory {
    public static Frame createJoinFrame(UUID userId) {
        JoinFrame joinFrame = new JoinFrame();
        return new Frame(joinFrame.getFrameIdentifier(), userId, joinFrame);
    }

    public static Frame createLeaveFrame(UUID userId) {
        LeaveFrame leaveFrame = new LeaveFrame();
        return new Frame(leaveFrame.getFrameIdentifier(), userId, leaveFrame);
    }

    public static Frame createPlayerFrame(Player player) {
        return new Frame(player.getFrameIdentifier(), player.getIdentifier(), player);
    }

    public static Frame createGameFrame(Player player, Game game) {
        GameFrame gameFrame = GameFrame.fromGame(game);
        return new Frame(gameFrame.getFrameIdentifier(), player.getIdentifier(), gameFrame);
    }

    
}
