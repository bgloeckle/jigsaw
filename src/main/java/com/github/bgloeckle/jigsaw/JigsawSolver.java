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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.cutjudge.EdgeCutJudge;
import com.github.bgloeckle.jigsaw.image.AwtImageIo;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Pipeline;
import com.github.bgloeckle.jigsaw.steps.EdgeTrackingByDoubleThreshold;
import com.github.bgloeckle.jigsaw.steps.GaussianBlur;
import com.github.bgloeckle.jigsaw.steps.NonMaximumSuppression;
import com.github.bgloeckle.jigsaw.steps.SobelFilter;
import com.github.bgloeckle.jigsaw.steps.ToSimpleLuminosityGreyscale;
import com.github.bgloeckle.jigsaw.util.JigsawCollectors;
import com.github.bgloeckle.jigsaw.util.Pair;
import com.google.common.collect.Sets;

public class JigsawSolver {
    private static final Logger logger = LoggerFactory.getLogger(JigsawSolver.class);

    /** {@link Comparator} which compares by the Double, sorting highest first. */
    private static final Comparator<Pair<Integer, Double>> CUT_JUDGE_COMPARATOR_HIGHEST_FRONT = (l,
                    r) -> -l.getRight().compareTo(r.getRight());
    private static final double CUT_JUDGE_BATCH_PERCENT = .85;

    /**
     * Use this many pixels before/after a cut position to also judge cuts at those positions. This is needed since the
     * cuts might not be 100% exact.
     */
    private static final int POSSIBLE_CUTS_HALO = 1;

    private File inputFile;
    private File outputFile;

    public JigsawSolver(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void solve() {
        logger.info("Working on file '{}'", inputFile.getAbsolutePath());
        Image inputImage;
        try (FileInputStream fis = new FileInputStream(inputFile)) {
            inputImage = new AwtImageIo().loadImage(fis);
        } catch (IOException | RuntimeException e) {
            throw new RuntimeException("Could not load input file", e);
        }

        logger.info("Identifying edges in input image using Canny algorithm...");
        Image inputEdgeImage = new Pipeline(new ToSimpleLuminosityGreyscale(), new GaussianBlur(3), new SobelFilter(),
                        new NonMaximumSuppression(), new EdgeTrackingByDoubleThreshold(.4, .85)).process(inputImage);
        double inputEdgeImageJudgement = new FullJudge(inputEdgeImage).judge();
        logger.info("Input image has a full judgement of: {}", inputEdgeImageJudgement);

        // Find and judge all the possible cut locations (judgement based on edge image).
        CachingCutJudgeDecorator cutJudge = new CachingCutJudgeDecorator(new EdgeCutJudge(inputEdgeImage),
                        inputEdgeImage.getWidth(), inputEdgeImage.getHeight());

        NavigableSet<Pair<Integer, Double>> possibleCutsX = findPossibleCutsAndJudgeThem(inputImage,
                        inputImage.getWidth(), cutJudge::judgeVerticalEvery);
        NavigableSet<Pair<Integer, Double>> possibleCutsY = findPossibleCutsAndJudgeThem(inputImage,
                        inputImage.getHeight(), cutJudge::judgeHorizontalEvery);

        logger.debug("Possible vertical cuts with x value and their judgement: {}", possibleCutsX);
        logger.debug("Possible horizontal cuts with y value and their judgement: {}", possibleCutsY);

        // now thin out those positions. Use all locations whose judgment is within CUT_JUDGE_BATCH_PERCENT of the best
        // one
        double bestJudgementX = possibleCutsX.first().getRight();
        double bestJudgementY = possibleCutsY.first().getRight();
        NavigableSet<Integer> cutsToInspectX = possibleCutsX
                        .headSet(new Pair<>(Integer.MIN_VALUE, CUT_JUDGE_BATCH_PERCENT * bestJudgementX), true).stream()
                        .map(p -> p.getLeft()).collect(JigsawCollectors.toNavigableSet());

        NavigableSet<Integer> cutsToInspectY = possibleCutsY
                        .headSet(new Pair<>(Integer.MIN_VALUE, CUT_JUDGE_BATCH_PERCENT * bestJudgementY), true).stream()
                        .map(p -> p.getLeft()).collect(JigsawCollectors.toNavigableSet());

        if (logger.isDebugEnabled()) {
            logger.debug("Removed unlikely cut positions: x={}, y={}",
                            Sets.difference(possibleCutsX.stream().map(p -> p.getLeft()).collect(Collectors.toSet()),
                                            cutsToInspectX),
                            Sets.difference(possibleCutsY.stream().map(p -> p.getLeft()).collect(Collectors.toSet()),
                                            cutsToInspectY));
        }

        // now thin out further by removing all multiples of other values. Assume there's x = 2*y with x and y being in
        // a set. If we then cut by x, this will also allow us to organize the tiles later on in the same manner as if
        // we did only cut by y.
        Set<Integer> multipleHaloRemovedX = removeMultiplicatesAndHaloNeighbours(cutsToInspectX);
        Set<Integer> multipleHaloRemovedY = removeMultiplicatesAndHaloNeighbours(cutsToInspectY);
        logger.debug("Removed positions because they are multiples of others/belong to the halo of anoter: x={}, y={}",
                        multipleHaloRemovedX, multipleHaloRemovedY);

        // Now we have a set of cuts of which we'd like to try all combinations of:
        logger.info("Identified following x/y values to cut the image. All {} combinations will be evaluated: x={}, y={}",
                        cutsToInspectX.size() * cutsToInspectY.size(), cutsToInspectX, cutsToInspectY);
    }

    private NavigableSet<Pair<Integer, Double>> findPossibleCutsAndJudgeThem(Image inputImage, int dimensionMax,
                    Function<Integer, Double> judgeFn) {
        NavigableSet<Pair<Integer, Double>> res = new TreeSet<>(CUT_JUDGE_COMPARATOR_HIGHEST_FRONT);
        for (int divisor = 2; (double) dimensionMax / divisor >= 5.; divisor++) {
            int value = (int) Math.round((double) dimensionMax / divisor);
            for (int delta = -POSSIBLE_CUTS_HALO; delta <= POSSIBLE_CUTS_HALO; delta++) {
                if (value + delta >= 2 && value + delta < dimensionMax - 1) {
                    res.add(new Pair<>(value + delta, judgeFn.apply(value + delta)));
                }
            }
        }
        return res;
    }

    private Set<Integer> removeMultiplicatesAndHaloNeighbours(NavigableSet<Integer> set) {
        Set<Integer> allRemoved = new HashSet<>();

        // first: find and remove all halos.
        Set<Integer> previousValues = new HashSet<>();
        Set<Integer> halosFound = new HashSet<>();
        for (Iterator<Integer> it = set.iterator(); it.hasNext();) {
            int location = it.next();
            for (int prev : previousValues) {
                if (location - prev <= POSSIBLE_CUTS_HALO + 1) { // mark as halo if haloMiddle is not contained, but
                                                                 // right and left values
                    halosFound.add(location);
                }
            }
            previousValues.add(location);
        }

        // ensure that from CUT Halos, only the halo middle is contained!
        Set<Integer> halosWorkedOn = new HashSet<>();
        for (int halo : halosFound) {
            if (halosWorkedOn.contains(halo)) {
                continue;
            }

            int realHaloFrom = halo - 1; // actual first halo index is -1, since halosFound does not contain the first
                                         // value of the halo.
            int haloMiddle = realHaloFrom + POSSIBLE_CUTS_HALO;
            for (int delta = -POSSIBLE_CUTS_HALO; delta <= POSSIBLE_CUTS_HALO; delta++) {
                halosWorkedOn.add(haloMiddle + delta);
                set.remove(haloMiddle + delta);
                allRemoved.add(haloMiddle + delta);
            }
            set.add(haloMiddle);
            allRemoved.remove(haloMiddle);
        }

        // remove all multiples
        previousValues = new HashSet<>();
        for (Iterator<Integer> it = set.iterator(); it.hasNext();) {
            int location = it.next();

            for (int prev : previousValues) {
                if (location % prev == 0) {
                    allRemoved.add(location);
                    it.remove();
                    break;
                }
            }
            previousValues.add(location);
        }

        return allRemoved;
    }
}
