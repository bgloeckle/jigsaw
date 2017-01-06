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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Step;
import com.github.bgloeckle.jigsaw.util.Pair;
import com.github.bgloeckle.jigsaw.util.Quantile;

/**
 * Executes the "double threshold" and "edge tracking by hysteresis" steps of the Canny algorithm. This thins out edges.
 * 
 * <p>
 * See https://en.wikipedia.org/wiki/Canny_edge_detector#Double_threshold.
 *
 * @author Bastian Gloeckle
 */
public class EdgeTrackingByDoubleThreshold implements Step {
    private static final Logger logger = LoggerFactory.getLogger(EdgeTrackingByDoubleThreshold.class);

    private double lowerThresholdPercentage;
    private double upperThresholdPercentage;

    private RandomProvider randomProvider;

    /**
     * @param lowerThreshold
     *            threshold of color values under which the pixels should be removed. This parameters value is a
     *            percentage (0.0<=v<=1.0) of the 90%-quantile of all pixel values.
     * @param upperThreshold
     *            threshold of color values over which the pixels should be kept. This parameters value is a percentage
     *            (0.0<=v<=1.0) of the 90%-quantile of all pixel values. Must be >= lowerThreshold.
     */
    public EdgeTrackingByDoubleThreshold(double lowerThreshold, double upperThreshold) {
        this(lowerThreshold, upperThreshold, upperBound -> ThreadLocalRandom.current().nextInt(upperBound));
    }

    /* package */ EdgeTrackingByDoubleThreshold(double lowerThreshold, double upperThreshold,
                    RandomProvider randomProvider) {
        this.lowerThresholdPercentage = lowerThreshold;
        this.upperThresholdPercentage = upperThreshold;
        this.randomProvider = randomProvider;
    }

    @Override
    public void accept(Image t) {
        Image original = t.copy();

        int cleanCount = 0;

        double quantile90 = approximateQuantile90(original);
        int lowerThresholdValue = (int) Math.round(quantile90 * lowerThresholdPercentage);
        int upperThresholdValue = (int) Math.round(quantile90 * upperThresholdPercentage);
        logger.info("Tracking edges using double threshold (factors: lower={}, upper={}): approx90percentile={}, "
                        + "lower={}, upper={}", lowerThresholdPercentage, upperThresholdPercentage, quantile90,
                        lowerThresholdValue, upperThresholdValue);

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                int curColor = original.getColor(x, y);
                if (curColor < lowerThresholdValue) {
                    t.setColor(x, y, 0);
                    t.setDirection(x, y, Image.DIRECTION_UNDEFINED);
                    cleanCount++;
                } else if (curColor < upperThresholdValue) {
                    boolean foundStrongPixel = false;
                    for (int compareX = x - 1; compareX <= x + 1 && !foundStrongPixel; compareX++) {
                        for (int compareY = y - 1; compareY <= y + 1 && !foundStrongPixel; compareY++) {
                            int actualCompareX = Math.min(Math.max(0, compareX), original.getWidth() - 1);
                            int actualCompareY = Math.min(Math.max(0, compareY), original.getHeight() - 1);
                            foundStrongPixel = original.getColor(actualCompareX, actualCompareY) >= upperThresholdValue;
                        }
                    }
                    if (!foundStrongPixel) {
                        // no strong pixel near, remove this pixel!
                        t.setColor(x, y, 0);
                        t.setDirection(x, y, Image.DIRECTION_UNDEFINED);
                        cleanCount++;
                    }
                } // else >= upperThresholdValue -> keep pixel.
            }
        }

        logger.debug("Cleaned {} unneeded pixels", cleanCount);
    }

    private double approximateQuantile90(Image i) {
        long numberOfPixelsTotal = (long) i.getWidth() * i.getHeight();
        Quantile q = new Quantile();

        if (numberOfPixelsTotal > 100_000) {
            // let's sample the pixels.
            Set<Pair<Integer, Integer>> colorIndices = new HashSet<>();
            while (colorIndices.size() < 1_000 || colorIndices.size() < numberOfPixelsTotal / 1_000) {
                colorIndices.add(new Pair<>(randomProvider.provideRandomInt(i.getWidth()),
                                randomProvider.provideRandomInt(i.getHeight())));
            }

            for (Pair<Integer, Integer> p : colorIndices) {
                q.addValues(i.getColor(p.getLeft(), p.getRight()));
            }
        } else {
            for (int x = 0; x < i.getWidth(); x++) {
                for (int y = 0; y < i.getHeight(); y++) {
                    q.addValues(i.getColor(x, y));
                }
            }
        }
        return q.calculateCurrentQuantile(.9);
    }

    public interface RandomProvider {
        public int provideRandomInt(int upperBound);
    }
}
