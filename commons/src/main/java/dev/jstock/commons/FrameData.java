package dev.jstock.commons;

// Abstract class representing the different frame data types
public abstract class FrameData {
    public abstract byte[] encode();
    public abstract byte getFrameIdentifier();
}