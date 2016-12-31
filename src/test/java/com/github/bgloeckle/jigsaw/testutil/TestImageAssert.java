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
package com.github.bgloeckle.jigsaw.testutil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.function.Supplier;

import org.junit.Assert;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.image.ImageIo;

public class TestImageAssert {
    /**
     * Assert that a given image is just like an expected one. The latter is available in a serialized form.
     */
    public static void assertAsExpected(Image actualImage, Supplier<InputStream> expectedImageStreamSupplier) {
        boolean equals = false;
        Throwable exception = null;

        try (InputStream expectedStream = expectedImageStreamSupplier.get()) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(expectedStream)) {
                Image expectedImg = (Image) objectInputStream.readObject();

                equals = expectedImg.equals(actualImage);
            } catch (ClassNotFoundException | RuntimeException e) {
                exception = e;
            }
        } catch (IOException | RuntimeException e) {
            exception = e;
        }

        if (equals) {
            return;
        }

        // find calling class
        Exception callerException = new RuntimeException();
        String testClassName = callerException.getStackTrace()[1].getClassName();

        try {
            File tempImageTarget = File.createTempFile(testClassName, ".png");
            new ImageIo().writeImage(actualImage, tempImageTarget.getAbsolutePath());

            File tempSerializedTarget = File.createTempFile(testClassName, ".serialized");
            try (FileOutputStream fos = new FileOutputStream(tempSerializedTarget)) {
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fos)) {
                    objectOutputStream.writeObject(actualImage);
                }
            }
            if (exception != null) {
                exception.printStackTrace();
                Assert.fail("Exception while comparing images. Saved actual image to '"
                                + tempImageTarget.getAbsolutePath() + "' and serialized form to '"
                                + tempSerializedTarget.getAbsolutePath() + "'.");
            }
            Assert.fail("Expected image to be as expected. Saved actual image to '" + tempImageTarget.getAbsolutePath()
                            + "' and serialized form to '" + tempSerializedTarget.getAbsolutePath() + "'.");
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("IOException while trying to store actual data");
        }

    }
}
