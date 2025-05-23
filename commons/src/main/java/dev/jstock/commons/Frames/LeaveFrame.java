package dev.jstock.commons.Frames;

import dev.jstock.commons.FrameData;
import dev.jstock.commons.FrameDataFactory;

public class LeaveFrame extends FrameData {
    public LeaveFrame() {}

    @Override
    public byte[] encode() {
        return new byte[0];
    }

    @Override
    public byte getFrameIdentifier() {
        return FrameDataFactory.LEAVE_FRAME;
    }
    
}

