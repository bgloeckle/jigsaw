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
import java.util.Iterator;
import java.util.Map;

import com.github.bgloeckle.jigsaw.cutjudge.CutJudge;
import com.github.bgloeckle.jigsaw.util.EdgeDirection;
import com.github.bgloeckle.jigsaw.util.Pair;

/**
 * Decorator around a {@link CutJudge} providing easier accessible methods and caching intermediate results.
 *
 * @author Bastian Gloeckle
 */
public class CachingCutJudgeDecorator {

    private CutJudge cutJudge;
    private int width;
    private int height;
    private Map<Integer, Double> horizontalCache = new HashMap<>();
    private Map<Integer, Double> verticalCache = new HashMap<>();

    public CachingCutJudgeDecorator(CutJudge cutJudge, int width, int height) {
        this.cutJudge = cutJudge;
        this.width = width;
        this.height = height;
    }

    /**
     * Calculate judgment of a single cut at the given location with the cut being horizontal.
     * 
     * @param cut
     *            y value at which to cut.
     * @return see {@link CutJudge#judge(Iterator, EdgeDirection)}
     */
    public double judgeHorizontal(int cut) {
        if (!horizontalCache.containsKey(cut)) {
            int y = cut;
            double res = cutJudge.judge(new Iterator<Pair<Integer, Integer>>() {
                private int curX = 0;

                @Override
                public boolean hasNext() {
                    return curX < width - 1;
                }

                @Override
                public Pair<Integer, Integer> next() {
                    return new Pair<>(curX++, y);
                }
            }, EdgeDirection.EAST_WEST);
            horizontalCache.put(cut, res);
        }

        return horizontalCache.get(cut);
    }

    public double judgeHorizontalEvery(int cut) {
        int count = 0;
        double resSum = .0;
        int pos = cut;
        while (pos < width - 1) {
            resSum += judgeHorizontal(pos);
            pos += cut;
            count++;
        }

        if (count == 0) {
            return Double.NEGATIVE_INFINITY;
        }

        return resSum / count;
    }

    /**
     * Calculate judgment of a single cut at the given location with the cut being vertical.
     * 
     * @param cut
     *            x value at which to cut.
     * @return see {@link CutJudge#judge(Iterator, EdgeDirection)}
     */
    public double judgeVertical(int cut) {
        if (!verticalCache.containsKey(cut)) {
            int x = cut;
            double res = cutJudge.judge(new Iterator<Pair<Integer, Integer>>() {
                private int curY = 0;

                @Override
                public boolean hasNext() {
                    return curY < height - 1;
                }

                @Override
                public Pair<Integer, Integer> next() {
                    return new Pair<>(x, curY++);
                }
            }, EdgeDirection.NORTH_SOUTH);
            verticalCache.put(cut, res);
        }
        return verticalCache.get(cut);
    }

    public double judgeVerticalEvery(int cut) {
        int count = 0;
        double resSum = .0;
        int pos = cut;
        while (pos < height - 1) {
            resSum += judgeVertical(pos);
            pos += cut;
            count++;
        }

        if (count == 0) {
            return Double.NEGATIVE_INFINITY;
        }

        return resSum / count;
    }
}
