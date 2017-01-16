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
package com.github.bgloeckle.jigsaw.assembly;

import com.github.bgloeckle.jigsaw.image.Image;

/**
 * Extracts a specific area from a source {@link Image} and provides a new {@link Image} out of that.
 *
 * @author Bastian Gloeckle
 */
public class Tile implements Image {
    private static final long serialVersionUID = 1L;

    private Image origImg;
    private int sourceX;
    private int sourceY;
    private int width;
    private int height;

    public Tile(Image origImg, int sourceX, int sourceY, int width, int height) {
        this.origImg = origImg;
        this.sourceX = sourceX;
        this.sourceY = sourceY;
        this.width = width;
        this.height = height;
    }

    public Tile(Image origImg, Tile other) {
        this(origImg, other.sourceX, other.sourceY, other.width, other.height);
    }

    private int sourceX(int x) {
        return x + sourceX;
    }

    private int sourceY(int y) {
        return y + sourceY;
    }

    @Override
    public int getColor(int x, int y) {
        return origImg.getColor(sourceX(x), sourceY(y));
    }

    @Override
    public void setColor(int x, int y, int newColor) {
        origImg.setColor(sourceX(x), sourceY(y), newColor);
    }

    @Override
    public double getDirection(int x, int y) {
        return origImg.getDirection(sourceX(x), sourceY(y));
    }

    @Override
    public void setDirection(int x, int y, double direction) {
        origImg.setDirection(sourceX(x), sourceY(y), direction);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Image copy() {
        throw new UnsupportedOperationException();
    }

    public int getSourceX() {
        return sourceX;
    }

    public int getSourceY() {
        return sourceY;
    }

    @Override
    public String toString() {
        return "Tile [sourceX=" + sourceX + ", sourceY=" + sourceY + "]";
    }

}
