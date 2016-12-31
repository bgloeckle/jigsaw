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
package com.github.bgloeckle.jigsaw;

import org.junit.Assert;
import org.junit.Test;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.Convolution;


public class ConvolutionTest {
    @Test
    public void simpleTest() {
        // GIVEN
        int input[][] = new int[][] { //
                        new int[] { 1, 2, 3 }, //
                        new int[] { 4, 5, 6 }, //
                        new int[] { 7, 8, 9 } //
        };
        double kernel[][] = new double[][] { //
                        new double[] { -1, -2, -1 }, //
                        new double[] { 0, 0, 0 }, //
                        new double[] { 1, 2, 1 }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(input);

        // WHEN
        Convolution.applyConvolution(kernel, img, false);

        // THEN
        Assert.assertEquals("Expected correct dimension x", 3, img.getWidth());
        Assert.assertEquals("Expected correct dimension y", 3, img.getHeight());

        Assert.assertArrayEquals("Expected correct values for x=0", new int[] { -12, -12, -12 }, img.color[0]);
        Assert.assertArrayEquals("Expected correct values for x=1", new int[] { -24, -24, -24 }, img.color[1]);
        Assert.assertArrayEquals("Expected correct values for x=2", new int[] { -12, -12, -12 }, img.color[2]);
    }

    private class ArrayBasedImage implements Image {
        private static final long serialVersionUID = 1L;
        private int[][] color;

        public ArrayBasedImage(int[][] color) {
            this.color = color;
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
            return new ArrayBasedImage(newColor);
        }

    }
}
