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

/**
 * Applies a SobelFilter on the input image. This calculates the edges of the image by using the gradient of the
 * intensity function of the image.
 * 
 * <p>
 * This will also set the direction of the pixels in the image according to the direction of the edge that is available
 * at specific pixels.
 * 
 * <p>
 * See https://en.wikipedia.org/wiki/Sobel_operator.
 *
 * @author Bastian Gloeckle
 */
public class SobelFilter implements Step {
    private static final Logger logger = LoggerFactory.getLogger(SobelFilter.class);

    private static final double[][] X_KERNEL = new double[][] { //
                    new double[] { -1, 0, 1 }, //
                    new double[] { -2, 0, 2 }, //
                    new double[] { -1, 0, 1 }, //
    };

    private static final double[][] Y_KERNEL = new double[][] { //
                    new double[] { 1, 2, 1 }, //
                    new double[] { 0, 0, 0 }, //
                    new double[] { -1, -2, -1 }, //
    };

    @Override
    public void accept(Image t) {
        logger.info("Applying Sobel filter");
        // Do not normalize, since the sum of all weights for our kernels is 0.
        Image xImage = t.copy();
        Convolution.applyConvolution(X_KERNEL, xImage, false);

        Image yImage = t.copy();
        Convolution.applyConvolution(Y_KERNEL, yImage, false);
        
        for (int x = 0; x < t.getWidth(); x++) {
            for (int y = 0; y < t.getHeight(); y++) {
                int xColor = xImage.getColor(x, y);
                int yColor = yImage.getColor(x, y);

                t.setColor(x, y, (int) Math.round(Math.hypot(xColor, yColor)));
                if (xColor == 0 && yColor == 0) {
                    t.setDirection(x, y, Image.DIRECTION_UNDEFINED);
                } else if (xColor == 0) {
                    // kernel for vertical line identification did not find anything -> horizontal line
                    t.setDirection(x, y, .0);
                } else if (yColor == 0) {
                    // kernel for horizontal line identification did not find anything -> vertical line
                    t.setDirection(x, y, Math.PI / 2);
                } else {
                    t.setDirection(x, y, Math.atan2(yColor, xColor) + Math.PI);
                }
            }
        }
    }

}
