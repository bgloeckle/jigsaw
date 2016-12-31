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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.steps.ToSimpleLuminosityGreyscale;

/**
 * Input/output of images based on AWT.
 *
 * @author Bastian Gloeckle
 */
public class AwtImageIo {
    private static final Logger logger = LoggerFactory.getLogger(AwtImageIo.class);

    /**
     * @return An image loaded from the given stream or <code>null</code>. The pixels of the returned image are in a
     *         format such that they can be passed to {@link #writeImage(Image, String)} or to
     *         {@link ToSimpleLuminosityGreyscale}.
     */
    public Image loadImage(InputStream stream) {
        try {
            return new AwtImageAdapter(ImageIO.read(stream));
        } catch (IOException e) {
            logger.error("Could not convert image stream!", e);
            return null;
        }
    }

    /**
     * Write the given image to the given location. The image MUST have pixels of the format that AWT
     * {@link BufferedImage} expects!
     */
    public void writeImage(Image img, String fileLocation) {
        if (!(img instanceof AwtImageAdapter)) {
            logger.error("Wrong image type: {}", img.getClass().getName());
            return;
        }

        BufferedImage bufImg = ((AwtImageAdapter) img).toBufferedImage();
        try {
            ImageIO.write(bufImg, "png", new File(fileLocation));
        } catch (IOException e) {
            logger.error("Could not write to '{}'", fileLocation, e);
        }
    }
}
