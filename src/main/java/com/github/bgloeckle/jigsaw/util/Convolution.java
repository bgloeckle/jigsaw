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
package com.github.bgloeckle.jigsaw.util;

import com.github.bgloeckle.jigsaw.image.Image;

public class Convolution {
    /**
     * Apply a kernel on the given image using convolution, writing the results back into that image.
     * 
     * <p>
     * Edges of the image will be "extended", i.e. when the convolution needs a pixel outside the image, the color of
     * the border pixel will be used.
     * 
     * <p>
     * See https://en.wikipedia.org/wiki/Kernel_(image_processing)#Convolution.
     * 
     * @param kernel
     *            Kernel to apply. Needs to be square and have odd number of rows and columns.
     * @param img
     *            The input image which will be adjusted.
     * @param normalize
     *            true if normalization should be applied: In that case, each pixel of the resulting matrix will be
     *            divided by the sum of the weights of all its summands (which are calculated using the kernel).
     */
    public static void applyConvolution(double[][] kernel, Image img, boolean normalize) {
        if (kernel.length != kernel[0].length || kernel.length % 2 == 0) {
            throw new IllegalArgumentException("kernel not square or not odd size.");
        }

        Image original = img.copy();

        int kernelCenterIdx = (kernel.length - 1) / 2;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                double sum = 0;
                double weightSum = 0;
                for (int kernelDeltaX = -kernelCenterIdx; kernelDeltaX <= kernelCenterIdx; kernelDeltaX++) {
                    for (int kernelDeltaY = -kernelCenterIdx; kernelDeltaY <= kernelCenterIdx; kernelDeltaY++) {
                        int sourceX = Math.min(Math.max(0, x + kernelDeltaX), img.getWidth() - 1);
                        int sourceY = Math.min(Math.max(0, y + kernelDeltaY), img.getHeight() - 1);

                        sum += original.getColor(sourceX, sourceY)
                                        * kernel[kernelCenterIdx - kernelDeltaX][kernelCenterIdx - kernelDeltaY];
                        weightSum += kernel[kernelCenterIdx - kernelDeltaX][kernelCenterIdx - kernelDeltaY];
                    }
                }
                if (normalize) {
                    img.setColor(x, y, (int) Math.round(sum / weightSum));
                } else {
                    img.setColor(x, y, (int) Math.round(sum));
                }
            }
        }
    }
}
