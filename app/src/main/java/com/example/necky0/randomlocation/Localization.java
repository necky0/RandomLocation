package com.example.necky0.randomlocation;

public class Localization {
    private String name;
    private double length;
    private double width;

    public Localization(String name, double length, double width) {
        this.name = name;
        this.length = length;
        this.width = width;
    }

    public String getName() {
        return name;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }
}
