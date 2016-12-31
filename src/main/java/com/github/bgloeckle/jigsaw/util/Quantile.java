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
package com.github.bgloeckle.jigsaw.util;

import com.tdunning.math.stats.AVLTreeDigest;

/**
 * Capable of calculating approximations of specific quantiles on a stream of values.
 * 
 * <p>
 * This uses Ted Dunnings t-digest!
 *
 * @author Bastian Gloeckle
 */
public class Quantile {
    private AVLTreeDigest tdigest = new AVLTreeDigest(100.);

    /**
     * Add values of which a quantile should be calculated
     */
    public Quantile addValues(int... values) {
        for (int v : values) {
            tdigest.add(v);
        }
        return this;
    }

    /**
     * @return Approximation of the given quantile.
     */
    public double calculateCurrentQuantile(double quantile) {
        return tdigest.quantile(quantile);
    }
}
