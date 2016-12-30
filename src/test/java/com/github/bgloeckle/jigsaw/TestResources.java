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

import java.io.InputStream;
import java.util.function.Supplier;

public class TestResources {
    public static final Supplier<InputStream> FOREST_ROAD = () -> TestResources.class
                    .getResourceAsStream("/road-in-autumn-forest-1318271179yAn.png");

    public static final Supplier<InputStream> PROPRIETARY_1 = () -> TestResources.class
                    .getResourceAsStream("/38e10b0bbad21a5915557cf78d9ff41867d4e3c0.png");
    public static final Supplier<InputStream> PROPRIETARY_2 = () -> TestResources.class
                    .getResourceAsStream("/6593ff05d1b5d9cfbfa681d47d6948a5fccf3f5a.png");
    public static final Supplier<InputStream> PROPRIETARY_3 = () -> TestResources.class
                    .getResourceAsStream("/7952556075c7a428c286b1d3025ab5762e70088c.png");
    public static final Supplier<InputStream> PROPRIETARY_4 = () -> TestResources.class
                    .getResourceAsStream("/e30538c5d41121b68f20e7e245590a0114682d1e.png");
}
