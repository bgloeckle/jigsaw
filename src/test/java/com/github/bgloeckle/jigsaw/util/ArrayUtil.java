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

public class ArrayUtil {
    /**
     * Transposes the given array.
     * 
     * <p>
     * Note that for tests it is simpler to write an array [y][x] style, but jigsaw expects it in [x][y] style. 
     */
    public static int[][] transpose(int[][] a) {
        int[][] res = new int[a[0].length][a.length];
        for (int y = 0; y < a.length; y++) {
            for (int x = 0; x < a[0].length; x++) {
                res[x][y] = a[y][x];
            }
        }
        return res;
    }

    /**
     * Transposes the given array.
     * 
     * <p>
     * Note that for tests it is simpler to write an array [y][x] style, but jigsaw expects it in [x][y] style.
     */
    public static double[][] transpose(double[][] a) {
        double[][] res = new double[a[0].length][a.length];
        for (int y = 0; y < a.length; y++) {
            for (int x = 0; x < a[0].length; x++) {
                res[x][y] = a[y][x];
            }
        }
        return res;
    }
}
