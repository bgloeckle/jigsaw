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
package com.github.bgloeckle.jigsaw.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Step;
import com.github.bgloeckle.jigsaw.util.Convolution;

public class GaussianBlur implements Step {
    private static final Logger logger = LoggerFactory.getLogger(GaussianBlur.class);

    private final double[][] kernel;
    private int sigma;

    public GaussianBlur(int sigma) {
        this.sigma = sigma;

        int kernelSize = sigma * 6; // according to wikipedia *6 is enough
        if (kernelSize % 2 == 0) {
            kernelSize++; // our impl needs a odd-sized kernel
        }

        kernel = new double[kernelSize][kernelSize];
        int kernelMidIdx = (kernelSize - 1) / 2;
        for (int x = 0; x < kernelSize; x++) {
            for (int y = 0; y < kernelSize; y++) {
                kernel[x][y] = kernelEntry(x - kernelMidIdx, y - kernelMidIdx, sigma);
            }
        }
    }

    private double kernelEntry(int deltaX, int deltaY, int sigma) {
        // See wikipedia.
        return Math.exp(-(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) / (2 * Math.pow(sigma, 2)))
                        / (2 * Math.PI * Math.pow(sigma, 2));
    }

    @Override
    public void accept(Image output) {
        logger.info("Applying Gaussian blur filter with sigma={}", sigma);

        Convolution.applyConvolution(kernel, output, true);
    }
}
