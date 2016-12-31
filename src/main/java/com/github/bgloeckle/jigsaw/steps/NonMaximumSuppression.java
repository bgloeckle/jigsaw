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
import com.github.bgloeckle.jigsaw.util.EdgeDirection;

/**
 * Implements the non-maximum-suppression of the Canny edge detection algorithm. This thins out the edges.
 * 
 * <p>
 * Expects as input an image that has been processed by {@link SobelFilter}, i.e. it expects that only "edges" have a
 * color value set and that the direction of the pixels is filled.
 * 
 * <p>
 * See https://en.wikipedia.org/wiki/Canny_edge_detector#Non-maximum_suppression
 *
 * @author Bastian Gloeckle
 */
public class NonMaximumSuppression implements Step {
    private static final Logger logger = LoggerFactory.getLogger(NonMaximumSuppression.class);



    @Override
    public void accept(Image t) {
        logger.info("Applying non-maxmimum suppression");
        Image original = t.copy();

        int cleanCount = 0;

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                EdgeDirection ourDir = EdgeDirection.fromGradientRadian(original.getDirection(x, y));
                if (ourDir == null) {
                    // Pixel is black already
                    continue;
                }

                int colorOtherPixel1;
                int colorOtherPixel2;
                switch (ourDir) {
                    case EAST_WEST:
                        // edge direction east-west -> check pixels above and below
                        colorOtherPixel1 = original.getColor(x, Math.max(0, y - 1));
                        colorOtherPixel2 = original.getColor(x, Math.min(original.getHeight() - 1, y + 1));
                        break;
                    case NORTH_SOUTH:
                        // edge direction north-south -> check pixels left and right
                        colorOtherPixel1 = original.getColor(Math.max(0, x - 1), y);
                        colorOtherPixel2 = original.getColor(Math.min(original.getWidth() - 1, x + 1), y);
                        break;
                    case SOUTHEAST_NORTHWEST:
                        colorOtherPixel1 = original.getColor(Math.min(original.getWidth() - 1, x + 1),
                                        Math.max(0, y - 1));
                        colorOtherPixel2 = original.getColor(Math.max(0, x - 1),
                                        Math.min(original.getHeight() - 1, y + 1));
                        break;
                    case NORTHEAST_SOUTHWEST:
                        colorOtherPixel1 = original.getColor(Math.max(0, x - 1), Math.max(0, y - 1));
                        colorOtherPixel2 = original.getColor(Math.min(original.getWidth() - 1, x + 1),
                                        Math.min(original.getHeight() - 1, y + 1));
                        break;
                    default:
                        throw new RuntimeException("Unknown coarse direction: " + ourDir);
                }

                int ourColor = original.getColor(x, y);
                if (ourColor < colorOtherPixel1 || ourColor < colorOtherPixel2) {
                    // no local maximum, disable pixel!
                    t.setColor(x, y, 0);
                    t.setDirection(x, y, Image.DIRECTION_UNDEFINED);
                    cleanCount++;
                }
            }
        }

        logger.debug("Cleaned {} unneeded pixels", cleanCount);
    }


}
