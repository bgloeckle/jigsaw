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

import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.AwtImageAdapter;
import com.github.bgloeckle.jigsaw.image.AwtImageIo;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Step;

/**
 * Converts AWT color values as returned by {@link AwtImageIo} into greyscale values using a simple luminosity approach.
 * 
 * <p>
 * Note that images that have been processed by this step are no longer passable to
 * {@link AwtImageIo#writeImage(Image, String)}.
 *
 * @author Bastian Gloeckle
 */
public class ToSimpleLuminosityGreyscale implements Step {
    private static final Logger logger = LoggerFactory.getLogger(ToSimpleLuminosityGreyscale.class);

    @Override
    public void accept(Image t) {
        logger.info("Converting to simple luminosity greyscale");
        BufferedImage img = ((AwtImageAdapter) t).toBufferedImage();

        for (int x = 0; x < t.getWidth(); x++) {
            for (int y = 0; y < t.getHeight(); y++) {
                Object colorData = img.getRaster().getDataElements(x, y, null);
                int red = img.getColorModel().getRed(colorData);
                int green = img.getColorModel().getGreen(colorData);
                int blue = img.getColorModel().getBlue(colorData);

                // according to wikipedia https://en.wikipedia.org/wiki/Grayscale
                int color = (int) Math.round(.2126 * red + .7152 * green + .0722 * blue);

                t.setColor(x, y, color);
            }
        }
    }

}
