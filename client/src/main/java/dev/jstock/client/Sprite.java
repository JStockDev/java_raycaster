package dev.jstock.client;

import com.googlecode.lanterna.TextColor;

public class Sprite {
    private Double x;
    private Double y;

    private TextColor.RGB color;

    public Sprite(Double x, Double y, TextColor.RGB color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public TextColor.RGB getColor() {
        return color;
    }
}
