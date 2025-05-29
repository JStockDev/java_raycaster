package dev.jstock.commons;

import java.util.UUID;

import dev.jstock.commons.Frames.GameFrame;
import dev.jstock.commons.Frames.JoinFrame;
import dev.jstock.commons.Frames.LeaveFrame;
import dev.jstock.commons.Frames.ObjectiveFrame;

// Small factory class for quickly creating frames for different purposes
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

    public static Frame createObjectiveFrame(UUID userId) {
        ObjectiveFrame objectiveFrame = new ObjectiveFrame(userId);
        return new Frame(objectiveFrame.getFrameIdentifier(), objectiveFrame);
    }
    
}
