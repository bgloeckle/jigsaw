/**
 * jigsaw: Solve image jigsaws.
 *
 * Copyright (C) 2016, 2017 Bastian Gloeckle
 *
 * This file is part of jigsaw.
 *
 * diqube is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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