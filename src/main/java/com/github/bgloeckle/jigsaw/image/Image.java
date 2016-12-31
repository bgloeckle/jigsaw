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

import java.io.Serializable;

import com.github.bgloeckle.jigsaw.pipeline.Pipeline;

/**
 * Representation of an image with a specific size and a simple representation of pixels in a 2-dim array. Additionally
 * it provides the possibility to store "direction" of pixels.
 *
 * @author Bastian Gloeckle
 */
public interface Image extends Serializable {
    public static final double DIRECTION_UNDEFINED = Double.NEGATIVE_INFINITY;

    /**
     * @param x
     *            >= 0 && x < {@link #getWidth()}
     * @param y
     *            >= 0 && y < {@link #getHeight()}
     * @return The color value at the given location. Note: What exactly the returned integer represents is not
     *         generally specified, see the current {@link Pipeline}.
     */
    public int getColor(int x, int y);

    /**
     * @param x
     *            >= 0 && x < {@link #getWidth()}
     * @param y
     *            >= 0 && y < {@link #getHeight()}
     * @param newColor
     *            The value of the new color to set for the given pixel. Note: What exactly the integer represents is
     *            not generally specified, it depends on the current {@link Pipeline}.
     */
    public void setColor(int x, int y, int newColor);

    /**
     * @param x
     *            >= 0 && x < {@link #getWidth()}
     * @param y
     *            >= 0 && y < {@link #getHeight()}
     * @return The direction of a specific pixel in radian. Max be {@link #DIRECTION_UNDEFINED}.
     */
    public double getDirection(int x, int y);

    /**
     * @param x
     *            >= 0 && x < {@link #getWidth()}
     * @param y
     *            >= 0 && y < {@link #getHeight()}
     * @param direction
     *            The new direction of the given pixel in radian.
     */
    public void setDirection(int x, int y, double direction);

    /**
     * @return number of pixels on the x axis
     */
    public int getWidth();

    /**
     * @return number of pixels on the y axis
     */
    public int getHeight();

    /**
     * @return A full copy of the image.
     */
    public Image copy();
}
