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

import com.github.bgloeckle.jigsaw.util.EdgeDirection;
import com.github.bgloeckle.jigsaw.util.Pair;

/**
 * Judges a cut of an image at a specific x or y position.
 *
 * @author Bastian Gloeckle
 */
public interface CutJudge {
    /**
     * Judges a given image on the indices returned by the given iterator.
     * 
     * @param positionIt
     *            An Iterator returning Pairs of (X/Y) values. These values have to be the indices of the intended cut.
     * @param cutDirection
     *            Direction of the intended cut.
     * @return A double value denoting the fuzzyness of the cut. Higher number means "more fuzzy", lower number means
     *         "looks like no cut needed here.
     */
    public double judge(Iterator<Pair<Integer, Integer>> positionIt, EdgeDirection cutDirection);
}
