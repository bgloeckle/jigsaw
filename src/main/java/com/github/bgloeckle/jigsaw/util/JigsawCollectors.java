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

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Just like {@link Collectors}.
 *
 * @author Bastian Gloeckle
 */
public class JigsawCollectors {
    /**
     * Just like {@link Collectors#toSet()}, but returns a {@link NavigableSet}.
     */
    public static <T> Collector<T, ?, NavigableSet<T>> toNavigableSet() {
        return Collector.of( //
                        (Supplier<NavigableSet<T>>) TreeSet::new, //
                        Set::add, //
                        (left, right) -> {
                            left.addAll(right);
                            return left;
                        }, //
                        Collector.Characteristics.UNORDERED);
    }
}
