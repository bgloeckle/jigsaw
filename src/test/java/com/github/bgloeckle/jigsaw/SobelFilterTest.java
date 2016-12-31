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

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;

import com.github.bgloeckle.jigsaw.image.AwtImageIo;
import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Pipeline;
import com.github.bgloeckle.jigsaw.steps.GaussianBlur;
import com.github.bgloeckle.jigsaw.steps.SobelFilter;
import com.github.bgloeckle.jigsaw.steps.ToSimpleLuminosityGreyscale;
import com.github.bgloeckle.jigsaw.testutil.FromSimpleLuminosityGreyscale;
import com.github.bgloeckle.jigsaw.testutil.TestImageAssert;

public class SobelFilterTest {
    private static final Supplier<InputStream> FOREST_ROAD_EXPECTED = () -> SobelFilterTest.class.getResourceAsStream(
                    "/" + SobelFilterTest.class.getSimpleName() + "/road-in-autumn-forest-1318271179yAn.serialized");

    private static final Supplier<InputStream> FOREST_ROAD_WITH_GAUSSIAN_EXPECTED = () -> SobelFilterTest.class
                    .getResourceAsStream("/" + SobelFilterTest.class.getSimpleName()
                                    + "/road-in-autumn-forest-1318271179yAn-gaussian.serialized");

    private Image img;

    @Before
    public void before() throws IOException {
        img = new AwtImageIo().loadImage(TestResources.FOREST_ROAD.get());
    }

    @Test
    public void simple() throws IOException {
        Pipeline p = new Pipeline(new ToSimpleLuminosityGreyscale(), new SobelFilter(),
                        new FromSimpleLuminosityGreyscale());
        img = p.process(img);

        TestImageAssert.assertAsExpected(img, FOREST_ROAD_EXPECTED);
    }

    @Test
    public void withGaussian() throws IOException {
        Pipeline p = new Pipeline(new ToSimpleLuminosityGreyscale(), new GaussianBlur(2), new SobelFilter(),
                        new FromSimpleLuminosityGreyscale());
        img = p.process(img);

        TestImageAssert.assertAsExpected(img, FOREST_ROAD_WITH_GAUSSIAN_EXPECTED);
    }

}
