package dev.jstock.commons;

import java.util.UUID;

import dev.jstock.commons.Frames.GameFrame;
import dev.jstock.commons.Frames.JoinFrame;
import dev.jstock.commons.Frames.LeaveFrame;

public class FrameFactory {
    public static Frame createJoinFrame(UUID userId) {
        JoinFrame joinFrame = new JoinFrame(userId);
        return new Frame(joinFrame.getFrameIdentifier(), joinFrame);
    }

    public static Frame createLeaveFrame(UUID userId) {
        LeaveFrame leaveFrame = new LeaveFrame(userId);
        return new Frame(leaveFrame.getFrameIdentifier(), leaveFrame);
    }

    public static Frame createPlayerFrame(Player player) {
        return new Frame(player.getFrameIdentifier(), player);
    }

    public static Frame createGameFrame(Game game) {
        GameFrame gameFrame = GameFrame.fromGame(game);
        return new Frame(gameFrame.getFrameIdentifier(), gameFrame);
    }

    
}
