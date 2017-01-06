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
package com.github.bgloeckle.jigsaw.cutjudge;

import java.util.Iterator;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.EdgeDirection;
import com.github.bgloeckle.jigsaw.util.Pair;

/**
 * A {@link CutJudge} inspecting a edge image and judging based on that.
 *
 * @author Bastian Gloeckle
 */
public class EdgeCutJudge implements CutJudge {
    private static final int CUT_HALO = 2;

    private Image img;

    public EdgeCutJudge(Image edgeImg) {
        this.img = edgeImg;
    }

    @Override
    public double judge(Iterator<Pair<Integer, Integer>> positionIt, EdgeDirection cutDirection) {
        int numberOfValidConnections = 0;
        int numberOfBrokenConnections = 0;
        for (Pair<Integer, Integer> pos = positionIt.next(); positionIt.hasNext(); pos = positionIt.next()) {
            Pair<Integer, Integer> posHalo1 = null;
            Pair<Integer, Integer> posHalo2 = null;
            for (int delta = 1; delta <= CUT_HALO; delta++) {
                if (cutDirection.equals(EdgeDirection.EAST_WEST)) {
                    if (posHalo1 == null) {
                        posHalo1 = new Pair<>(pos.getLeft(), pos.getRight() - delta);
                    }
                    if (posHalo2 == null) {
                        posHalo2 = new Pair<>(pos.getLeft(), pos.getRight() + delta);
                    }
                } else if (cutDirection.equals(EdgeDirection.NORTH_SOUTH)) {
                    if (posHalo1 == null) {
                        posHalo1 = new Pair<>(pos.getLeft() - delta, pos.getRight());
                    }
                    if (posHalo2 == null) {
                        posHalo2 = new Pair<>(pos.getLeft() + delta, pos.getRight());
                    }
                }

                if (posHalo1 != null && !isValidHaloPosition(posHalo1)) {
                    posHalo1 = null;
                }
                if (posHalo2 != null && !isValidHaloPosition(posHalo2)) {
                    posHalo2 = null;
                }
            }

            if (posHalo1 == null && posHalo2 == null) {
                // did not find valid halo position => out of image or no edge
                // noop.
            }
            else if (posHalo1 == null ^ posHalo2 == null) {
                // one has a pixel/edge, the other does not.
                numberOfBrokenConnections++;
            } else {
                // both have an edge.
                EdgeDirection dirHalo1 = EdgeDirection
                                .fromGradientRadian(img.getDirection(posHalo1.getLeft(), posHalo1.getRight()));
                EdgeDirection dirHalo2 = EdgeDirection
                                .fromGradientRadian(img.getDirection(posHalo2.getLeft(), posHalo2.getRight()));
                if (dirHalo1.equals(dirHalo2)) {
                    if (dirHalo1.equals(cutDirection)) {
                        // ignore since the edge is in the direction of the cut.
                    } else {
                        numberOfValidConnections++;
                    }
                } else {
                    numberOfBrokenConnections++;
                }
            }
        }

        if (numberOfBrokenConnections + numberOfValidConnections == 0) {
            return .0;
        }

        return (numberOfBrokenConnections - numberOfValidConnections)
                        / ((double) numberOfBrokenConnections + numberOfValidConnections);
    }

    private boolean isValidHaloPosition(Pair<Integer, Integer> pos) {
        return pos.getLeft() >= 0 && pos.getLeft() < img.getWidth() && pos.getRight() >= 0
                        && pos.getRight() < img.getHeight() && img.getColor(pos.getLeft(), pos.getRight()) > 0;
    }
}
