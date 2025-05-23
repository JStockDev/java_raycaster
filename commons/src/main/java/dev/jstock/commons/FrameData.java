package dev.jstock.commons;

public abstract class FrameData {
    public abstract byte[] encode();
    public abstract byte getFrameIdentifier();
}