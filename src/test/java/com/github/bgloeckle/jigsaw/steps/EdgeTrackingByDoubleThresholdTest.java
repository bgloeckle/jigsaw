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
package com.github.bgloeckle.jigsaw.steps;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;

import com.github.bgloeckle.jigsaw.TestResources;
import com.github.bgloeckle.jigsaw.image.AwtImageIo;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Pipeline;
import com.github.bgloeckle.jigsaw.steps.EdgeTrackingByDoubleThreshold.RandomProvider;
import com.github.bgloeckle.jigsaw.testutil.ProprietaryOnlyRule;
import com.github.bgloeckle.jigsaw.testutil.ProprietaryOnlyRule.ProprietaryOnly;
import com.github.bgloeckle.jigsaw.testutil.TestImageAssert;

public class EdgeTrackingByDoubleThresholdTest {
    private static final Supplier<InputStream> FOREST_ROAD_EXPECTED = () -> EdgeTrackingByDoubleThresholdTest.class
                    .getResourceAsStream("/" + EdgeTrackingByDoubleThresholdTest.class.getSimpleName()
                                    + "/road-in-autumn-forest-1318271179yAn.serialized");

    private static final Supplier<InputStream> PROPRIETARY_1_EXPECTED = () -> EdgeTrackingByDoubleThresholdTest.class
                    .getResourceAsStream("/" + EdgeTrackingByDoubleThresholdTest.class.getSimpleName()
                                    + "/38e10b0bbad21a5915557cf78d9ff41867d4e3c0.serialized");

    private static final Supplier<InputStream> PROPRIETARY_2_EXPECTED = () -> EdgeTrackingByDoubleThresholdTest.class
                    .getResourceAsStream("/" + EdgeTrackingByDoubleThresholdTest.class.getSimpleName()
                                    + "/6593ff05d1b5d9cfbfa681d47d6948a5fccf3f5a.serialized");
    @Rule
    public ProprietaryOnlyRule proprietaryOnlyRule = new ProprietaryOnlyRule();

    @Test
    public void edgeTracking40_85() throws IOException {
        // GIVEN
        Image img = new AwtImageIo().loadImage(TestResources.FOREST_ROAD.get());
        Pipeline p = new Pipeline(new ToSimpleLuminosityGreyscale(), new GaussianBlur(3), new SobelFilter(),
                        new NonMaximumSuppression(),
                        new EdgeTrackingByDoubleThreshold(.4, .85, new FixedRandomProvider()),
                        new FromSimpleLuminosityGreyscale());

        // WHEN
        img = p.process(img);

        // THEN
        TestImageAssert.assertAsExpected(img, FOREST_ROAD_EXPECTED);
    }

    @Test
    @ProprietaryOnly
    public void proprietary1Test() {
        // GIVEN
        Image img = new AwtImageIo().loadImage(TestResources.PROPRIETARY_1.get());
        Pipeline p = new Pipeline(new ToSimpleLuminosityGreyscale(), new GaussianBlur(3), new SobelFilter(),
                        new NonMaximumSuppression(),
                        new EdgeTrackingByDoubleThreshold(.4, .85, new FixedRandomProvider()),
                        new FromSimpleLuminosityGreyscale());

        // WHEN
        img = p.process(img);

        // THEN
        TestImageAssert.assertAsExpected(img, PROPRIETARY_1_EXPECTED);
    }

    @Test
    @ProprietaryOnly
    public void proprietary2Test() {
        // GIVEN
        Image img = new AwtImageIo().loadImage(TestResources.PROPRIETARY_2.get());
        Pipeline p = new Pipeline(new ToSimpleLuminosityGreyscale(), new GaussianBlur(3), new SobelFilter(),
                        new NonMaximumSuppression(),
                        new EdgeTrackingByDoubleThreshold(.4, .85, new FixedRandomProvider()),
                        new FromSimpleLuminosityGreyscale());

        // WHEN
        img = p.process(img);

        // THEN
        TestImageAssert.assertAsExpected(img, PROPRIETARY_2_EXPECTED);
    }

    private class FixedRandomProvider implements RandomProvider {
        private Map<Integer, Integer> nextInt = new HashMap<>();

        @Override
        public int provideRandomInt(int upperBound) {
            if (!nextInt.containsKey(upperBound)) {
                nextInt.put(upperBound, 0);
            }

            int res = nextInt.get(upperBound);
            if (res >= upperBound) {
                res = 0;
            }
            nextInt.put(upperBound, res + 1);
            return res;
        }
    }
}
