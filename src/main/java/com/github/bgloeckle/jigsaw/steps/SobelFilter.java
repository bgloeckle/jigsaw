package com.github.bgloeckle.jigsaw.steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bgloeckle.jigsaw.image.Image;
import com.github.bgloeckle.jigsaw.pipeline.Step;
import com.github.bgloeckle.jigsaw.util.Convolution;

public class SobelFilter implements Step {
    private static final Logger logger = LoggerFactory.getLogger(SobelFilter.class);

    private static final int[][] X_KERNEL = new int[][] { //
                    new int[] { -1, 0, 1 }, //
                    new int[] { -2, 0, 2 }, //
                    new int[] { -1, 0, 1 }, //
    };

    private static final int[][] Y_KERNEL = new int[][] { //
                    new int[] { 1, 2, 1 }, //
                    new int[] { 0, 0, 0 }, //
                    new int[] { -1, -2, -1 }, //
    };

    @Override
    public void accept(Image t) {
        logger.info("Applying Sobel filter");
        Image xImage = t.copy();
        Convolution.applyConvolution(X_KERNEL, xImage);

        Image yImage = t.copy();
        Convolution.applyConvolution(Y_KERNEL, yImage);
        
        for (int x = 0; x < t.getWidth(); x++) {
            for (int y = 0; y < t.getHeight(); y++) {
                t.setColor(x, y, (int) Math.hypot(xImage.getColor(x, y), yImage.getColor(x, y)));
            }
        }
    }

}
