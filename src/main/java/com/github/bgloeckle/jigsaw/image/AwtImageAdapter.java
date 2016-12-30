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
package com.github.bgloeckle.jigsaw.image;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class AwtImageAdapter implements Image {
    private static final long serialVersionUID = 1L;

    private int[][] color;
    private transient int origImageType;

    public AwtImageAdapter(BufferedImage img) {
        importFrom(img);
    }

    public AwtImageAdapter(AwtImageAdapter other) {
        color = new int[other.color.length][other.color[0].length];
        for (int x = 0; x < color.length; x++) {
            for (int y = 0; y < color[0].length; y++) {
                color[x][y] = other.color[x][y];
            }
        }
        origImageType = other.origImageType;
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
    public int getWidth() {
        return color.length;
    }

    @Override
    public int getHeight() {
        return color[0].length;
    }

    public BufferedImage toBufferedImage() {
        BufferedImage res = new BufferedImage(color.length, color[0].length, origImageType);
        for (int x = 0; x < color.length; x++) {
            for (int y = 0; y < color[0].length; y++) {
                res.setRGB(x, y, color[x][y]);
            }
        }
        return res;
    }

    public void importFrom(BufferedImage img) {
        color = new int[img.getWidth()][img.getHeight()];
        origImageType = img.getType();
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                color[x][y] = img.getRGB(x, y);
            }
        }
    }

    @Override
    public Image copy() {
        return new AwtImageAdapter(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.deepHashCode(color);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AwtImageAdapter other = (AwtImageAdapter) obj;
        if (!Arrays.deepEquals(color, other.color))
            return false;
        return true;
    }
}
