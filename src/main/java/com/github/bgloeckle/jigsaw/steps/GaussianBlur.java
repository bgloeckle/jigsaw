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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Step;
import com.github.bgloeckle.jigsaw.util.Pair;

public class GaussianBlur implements Step {
    private static final Logger logger = LoggerFactory.getLogger(GaussianBlur.class);

    private static final Map<Pair<Integer, Integer>, Double> WEIGHT_CACHE = new HashMap<>();

    private int sigma;

    public GaussianBlur(int sigma) {
        this.sigma = sigma;
    }

    @Override
    public void accept(Image output) {
        logger.info("Applying Gaussian blur filter with sigma={}", sigma);

        Image original = output.copy();

        int radius = sigma * 3; // according to wikipedia
        for (int x = 0; x < output.getWidth(); x++) {
            for (int y = 0; y < output.getHeight(); y++) {
                int resColor = 0;
                double weightSum = .0;
                for (int inputX = Math.max(0, x - radius); inputX < Math.min(output.getWidth(), x + radius); inputX++) {
                    for (int inputY = Math.max(0, y - radius); inputY < Math.min(output.getHeight(),
                                    y + radius); inputY++) {

                        double weight = getWeight(Math.abs(inputX - x), Math.abs(inputY - y));

                        resColor += weight * original.getColor(inputX, inputY);
                        weightSum += weight;
                    }
                }
                resColor /= weightSum;
                output.setColor(x, y, resColor);
            }
        }
    }

    private double getWeight(int deltaX, int deltaY) {
        Pair<Integer, Integer> pos = new Pair<>(deltaX, deltaY);
        if (!WEIGHT_CACHE.containsKey(pos)) {
            WEIGHT_CACHE.put(pos, Math.exp(-(Math.pow(deltaX, 2) + Math.pow(deltaY, 2)) / (2 * Math.pow(sigma, 2)))
                            / (2 * Math.PI * Math.pow(sigma, 2)));
        }
        return WEIGHT_CACHE.get(pos);
    }

}
