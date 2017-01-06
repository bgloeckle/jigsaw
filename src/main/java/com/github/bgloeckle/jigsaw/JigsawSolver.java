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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.AwtImageIo;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Pipeline;
import com.github.bgloeckle.jigsaw.steps.EdgeTrackingByDoubleThreshold;
import com.github.bgloeckle.jigsaw.steps.GaussianBlur;
import com.github.bgloeckle.jigsaw.steps.NonMaximumSuppression;
import com.github.bgloeckle.jigsaw.steps.SobelFilter;
import com.github.bgloeckle.jigsaw.steps.ToSimpleLuminosityGreyscale;

public class JigsawSolver {
    private static final Logger logger = LoggerFactory.getLogger(JigsawSolver.class);

    private File inputFile;
    private File outputFile;

    public JigsawSolver(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void solve() {
        Image inputImage;
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            inputImage = new AwtImageIo().loadImage(fis);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Could not load input file", e);
        }

        logger.info("Identifying edges in input image using Canny algorithm...");
        Image inputEdgeImage = new Pipeline(new ToSimpleLuminosityGreyscale(), new GaussianBlur(3), new SobelFilter(),
                        new NonMaximumSuppression(), new EdgeTrackingByDoubleThreshold(.4, .85)).process(inputImage);
        double inputEdgeImageJudgement = new Judge(inputEdgeImage).judge();
        logger.info("Input image has a judgement of: {}", inputEdgeImageJudgement);

        Set<Integer> possibleCutsX = new HashSet<>();
        for (int i = 2; i <= Math.round(inputImage.getWidth() / 2.); i++) {
            if (inputImage.getWidth() % i == 0) {
                possibleCutsX.add(i);
            }
        }
        possibleCutsX.add(inputImage.getWidth());
        Set<Integer> possibleCutsY = new HashSet<>();
        for (int i = 2; i <= Math.round(inputImage.getHeight() / 2.); i++) {
            if (inputImage.getHeight() % i == 0) {
                possibleCutsY.add(i);
            }
        }
        possibleCutsY.add(inputImage.getHeight());

    }
}
