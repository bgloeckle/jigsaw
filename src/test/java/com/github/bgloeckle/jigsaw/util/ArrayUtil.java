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
