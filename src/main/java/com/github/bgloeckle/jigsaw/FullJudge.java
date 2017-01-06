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

import java.util.HashMap;
import java.util.Map;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.util.EdgeDirection;
import com.github.bgloeckle.jigsaw.util.Pair;

/**
 * Judges a "edge image" in quality according to the edges shown.
 *
 * @author Bastian Gloeckle
 */
public class FullJudge {
    private Image img;
    private Double result = null;
    private Map<Pair<Integer, Integer>, Integer> edgeByPosition = new HashMap<>();
    private int nextEdgeId = 0;

    public FullJudge(Image edgeImg) {
        this.img = edgeImg;
    }

    /**
     * Judge the given image.
     * 
     * @return A double value denoting how well the images edges are connected. Higher number means "more connected".
     */
    public double judge() {
        if (result != null) {
            return result;
        }
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Pair<Integer, Integer> pos = new Pair<>(x, y);
                if (edgeByPosition.containsKey(pos)) {
                    continue;
                }

                if (img.getColor(x, y) != 0) {
                    followEdge(x, y);
                }
            }
        }

        result = ((long) img.getHeight() * img.getWidth()) / ((double) nextEdgeId);
        return result;
    }

    private void followEdge(int x, int y) {
        EdgeDirection direction = EdgeDirection.fromGradientRadian(img.getDirection(x, y));
        int edgeId = nextEdgeId++;

        boolean jumped = false;
        int curY = y;
        for (int curX = x; curX < img.getWidth() && curX >= 0;) {
            if (curY >= img.getHeight() || curY < 0) {
                return;
            }
            if (img.getColor(curX, curY) == 0) {
                return;
            }

            EdgeDirection curDirection = EdgeDirection.fromGradientRadian(img.getDirection(curX, curY));
            if (direction.equals(curDirection)) {
                jumped = false;

                edgeByPosition.put(new Pair<>(curX, curY), edgeId);
            } else {
                if (jumped) {
                    // we jumped over a different edge already, do not jump again. Assume our edge is broken
                    return;
                }
                jumped = true;
            }

            switch (direction) {
                case EAST_WEST:
                    curX++;
                    break;
                case NORTH_SOUTH:
                    curY++;
                    break;
                case NORTHEAST_SOUTHWEST:
                    curX++;
                    curY--;
                    break;
                case SOUTHEAST_NORTHWEST:
                    curX++;
                    curY++;
                    break;
            }
        }
    }
}
