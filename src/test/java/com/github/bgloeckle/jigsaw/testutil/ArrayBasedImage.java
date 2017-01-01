package com.github.bgloeckle.jigsaw.testutil;

import com.github.bgloeckle.jigsaw.image.Image;

/**
 * An {@link Image} that is based on external arrays.
 *
 * @author Bastian Gloeckle
 */
public class ArrayBasedImage implements Image {
    private static final long serialVersionUID = 1L;
    private int[][] color;
    private double[][] direction;

    public ArrayBasedImage(int[][] color, double[][] direction) {
        this.color = color;
        this.direction = direction;
    }

    public ArrayBasedImage(int[][] color) {
        this.color = color;
        this.direction = new double[color.length][color[0].length];
        for (int x = 0; x < direction.length; x++) {
            for (int y = 0; y < direction[0].length; y++) {
                direction[x][y] = Image.DIRECTION_UNDEFINED;
            }
        }
    }

    @Override
    public int getColor(int x, int y) {
        return color[x][y];
    }

    @Override
    public void setColor(int x, int y, int newColor) {
        color[x][y] = newColor;
    }

    @Override
    public double getDirection(int x, int y) {
        return direction[x][y];
    }

    @Override
    public void setDirection(int x, int y, double direction) {
        this.direction[x][y] = direction;
    }

    @Override
    public int getWidth() {
        return color[0].length;
    }

    @Override
    public int getHeight() {
        return color.length;
    }

    @Override
    public Image copy() {
        int[][] newColor = new int[color.length][color[0].length];
        for (int x = 0; x < color.length; x++) {
            for (int y = 0; y < color[0].length; y++) {
                newColor[x][y] = color[x][y];
            }
        }
        double[][] newDirection = new double[direction.length][direction[0].length];
        for (int x = 0; x < direction.length; x++) {
            for (int y = 0; y < direction[0].length; y++) {
                newDirection[x][y] = direction[x][y];
            }
        }
        return new ArrayBasedImage(newColor, newDirection);
    }

    public int[][] getAllColors() {
        return color;
    }

    public double[][] getAllDirections() {
        return direction;
    }
}