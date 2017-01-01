package com.github.bgloeckle.jigsaw;

import org.junit.Assert;
import org.junit.Test;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.testutil.ArrayBasedImage;
import com.github.bgloeckle.jigsaw.util.ArrayUtil;
import com.github.bgloeckle.jigsaw.util.EdgeDirection;

public class JudgeTest {
    private static final double UNDEF = Image.DIRECTION_UNDEFINED;
    @Test
    public void singleLineVertical() {
        // GIVEN
        int[][] color = new int[][] { //
                        new int[] { 0, 1, 0, 0 }, //
                        new int[] { 0, 1, 0, 0 }, //
                        new int[] { 0, 1, 0, 0 }, //
                        new int[] { 0, 1, 0, 0 }, //
        };
        double south = EdgeDirection.NORTH_SOUTH.getGradientRadian();
        double[][] direction = new double[][] { //
                        new double[] { UNDEF, south, UNDEF, UNDEF }, //
                        new double[] { UNDEF, south, UNDEF, UNDEF }, //
                        new double[] { UNDEF, south, UNDEF, UNDEF }, //
                        new double[] { UNDEF, south, UNDEF, UNDEF }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(ArrayUtil.transpose(color), ArrayUtil.transpose(direction));

        // WHEN
        double res = new Judge(img).judge();

        // THEN
        Assert.assertEquals("Expected correct judge result", 16. / 1., res, 1e-4);
    }

    @Test
    public void doubleLineVertical() {
        // GIVEN
        int[][] color = new int[][] { //
                        new int[] { 0, 1, 1, 0 }, //
                        new int[] { 0, 1, 1, 0 }, //
                        new int[] { 0, 1, 1, 0 }, //
                        new int[] { 0, 1, 1, 0 }, //
        };
        double south = EdgeDirection.NORTH_SOUTH.getGradientRadian();
        double[][] direction = new double[][] { //
                        new double[] { UNDEF, south, south, UNDEF }, //
                        new double[] { UNDEF, south, south, UNDEF }, //
                        new double[] { UNDEF, south, south, UNDEF }, //
                        new double[] { UNDEF, south, south, UNDEF }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(ArrayUtil.transpose(color), ArrayUtil.transpose(direction));

        // WHEN
        double res = new Judge(img).judge();

        // THEN
        Assert.assertEquals("Expected correct judge result", 16. / 2., res, 1e-4);
    }

    @Test
    public void singleLineHorizontal() {
        // GIVEN
        int[][] color = new int[][] { //
                        new int[] { 0, 0, 0, 0 }, //
                        new int[] { 1, 1, 1, 1 }, //
                        new int[] { 0, 0, 0, 0 }, //
                        new int[] { 0, 0, 0, 0 }, //
        };
        double east = EdgeDirection.EAST_WEST.getGradientRadian();
        double[][] direction = new double[][] { //
                        new double[] { UNDEF, UNDEF, UNDEF, UNDEF }, //
                        new double[] { east, east, east, east }, //
                        new double[] { UNDEF, UNDEF, UNDEF, UNDEF }, //
                        new double[] { UNDEF, UNDEF, UNDEF, UNDEF }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(ArrayUtil.transpose(color), ArrayUtil.transpose(direction));

        // WHEN
        double res = new Judge(img).judge();

        // THEN
        Assert.assertEquals("Expected correct judge result", 16. / 1., res, 1e-4);
    }

    @Test
    public void doubleLineHorizontal() {
        // GIVEN
        int[][] color = new int[][] { //
                        new int[] { 0, 0, 0, 0 }, //
                        new int[] { 1, 1, 1, 1 }, //
                        new int[] { 0, 0, 0, 0 }, //
                        new int[] { 1, 1, 1, 1 }, //
        };
        double east = EdgeDirection.EAST_WEST.getGradientRadian();
        double[][] direction = new double[][] { //
                        new double[] { UNDEF, UNDEF, UNDEF, UNDEF }, //
                        new double[] { east, east, east, east }, //
                        new double[] { UNDEF, UNDEF, UNDEF, UNDEF }, //
                        new double[] { east, east, east, east }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(ArrayUtil.transpose(color), ArrayUtil.transpose(direction));

        // WHEN
        double res = new Judge(img).judge();

        // THEN
        Assert.assertEquals("Expected correct judge result", 16. / 2., res, 1e-4);
    }

    @Test
    public void tripleLineSoutheast() {
        // GIVEN
        int[][] color = new int[][] { //
                        new int[] { 1, 0, 0, 0 }, //
                        new int[] { 0, 1, 0, 0 }, //
                        new int[] { 0, 0, 1, 0 }, //
                        new int[] { 1, 0, 1, 0 }, //
        };
        double se = EdgeDirection.SOUTHEAST_NORTHWEST.getGradientRadian();
        double[][] direction = new double[][] { //
                        new double[] { se, UNDEF, UNDEF, UNDEF }, //
                        new double[] { UNDEF, se, UNDEF, UNDEF }, //
                        new double[] { UNDEF, UNDEF, se, UNDEF }, //
                        new double[] { se, UNDEF, se, se }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(ArrayUtil.transpose(color), ArrayUtil.transpose(direction));

        // WHEN
        double res = new Judge(img).judge();

        // THEN
        Assert.assertEquals("Expected correct judge result", 16. / 3., res, 1e-4);
    }

    @Test
    public void tripleLineNortheast() {
        // GIVEN
        int[][] color = new int[][] { //
                        new int[] { 0, 0, 0, 0 }, //
                        new int[] { 0, 1, 0, 0 }, //
                        new int[] { 1, 0, 1, 0 }, //
                        new int[] { 0, 0, 0, 1 }, //
        };
        double ne = EdgeDirection.NORTHEAST_SOUTHWEST.getGradientRadian();
        double[][] direction = new double[][] { //
                        new double[] { UNDEF, UNDEF, UNDEF, UNDEF }, //
                        new double[] { UNDEF, ne, UNDEF, UNDEF }, //
                        new double[] { ne, UNDEF, ne, UNDEF }, //
                        new double[] { UNDEF, UNDEF, UNDEF, ne }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(ArrayUtil.transpose(color), ArrayUtil.transpose(direction));

        // WHEN
        double res = new Judge(img).judge();

        // THEN
        Assert.assertEquals("Expected correct judge result", 16. / 3., res, 1e-4);
    }

    @Test
    public void threeLinesCross() {
        // GIVEN
        int[][] color = new int[][] { //
                        new int[] { 1, 1, 1, 1 }, //
                        new int[] { 0, 1, 1, 0 }, //
                        new int[] { 0, 0, 1, 0 }, //
                        new int[] { 0, 0, 1, 1 }, //
        };
        double se = EdgeDirection.SOUTHEAST_NORTHWEST.getGradientRadian();
        double east = EdgeDirection.EAST_WEST.getGradientRadian();
        double south = EdgeDirection.NORTH_SOUTH.getGradientRadian();
        double[][] direction = new double[][] { //
                        new double[] { se, east, south, east }, //
                        new double[] { UNDEF, se, south, UNDEF }, //
                        new double[] { UNDEF, UNDEF, south, UNDEF }, //
                        new double[] { UNDEF, UNDEF, south, se }, //
        };
        ArrayBasedImage img = new ArrayBasedImage(ArrayUtil.transpose(color), ArrayUtil.transpose(direction));

        // WHEN
        double res = new Judge(img).judge();

        // THEN
        Assert.assertEquals("Expected correct judge result", 16. / 3., res, 1e-4);
    }
}
