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
package com.github.bgloeckle.jigsaw.testutil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Step;

public class FromSimpleLuminosityGreyscale implements Step {
    private static final Logger logger = LoggerFactory.getLogger(FromSimpleLuminosityGreyscale.class);

    @Override
    public void accept(Image t) {
        logger.info("Converting from simple greyscale to image format");
        for (int x = 0; x < t.getWidth(); x++) {
            for (int y = 0; y < t.getHeight(); y++) {
                int red = t.getColor(x, y);
                int green = red;
                int blue = red;

                int rgb = (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);

                t.setColor(x, y, rgb);
            }
        }
    }

}
