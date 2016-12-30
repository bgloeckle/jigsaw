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
