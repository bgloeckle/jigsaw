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

import com.github.bgloeckle.jigsaw.image.Image;

public enum EdgeDirection {
    NORTH_SOUTH, EAST_WEST, NORTHEAST_SOUTHWEST, SOUTHEAST_NORTHWEST;

    public static EdgeDirection fromGradientRadian(double radianDirection) {
        if (radianDirection == Image.DIRECTION_UNDEFINED) {
            return null;
        }

        double norm = radianDirection / Math.PI;
        if (norm > 1. + 1e4) {
            norm /= 2.;
        }
        if (norm <= 1. / 8. || norm >= 7. / 8.) {
            return EdgeDirection.NORTH_SOUTH;
        }
        if (norm <= 3. / 8.) {
            return EdgeDirection.SOUTHEAST_NORTHWEST;
        }
        if (norm >= 5. / 8.) {
            return EdgeDirection.NORTHEAST_SOUTHWEST;
        }

        return EdgeDirection.EAST_WEST;
    }
}