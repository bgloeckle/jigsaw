package com.github.bgloeckle.jigsaw.util;

import com.github.bgloeckle.jigsaw.image.Image;

public class Convolution {
    /**
     * Apply a kernel on the given image using convolution, writing the results back into that image.
     * 
     * <p>
     * Edges of the image will be "extended", i.e. when the convolution needs a pixel outside the image, the color of
     * the border pixel will be used.
     * 
     * <p>
     * See https://en.wikipedia.org/wiki/Kernel_(image_processing)#Convolution.
     * 
     * @param kernel
     *            Kernel to apply. Needs to be square and have odd number of rows and columns.
     * @param img
     *            The input image which will be adjusted.
     */
    public static void applyConvolution(int[][] kernel, Image img) {
        if (kernel.length != kernel[0].length || kernel.length % 2 == 0) {
            throw new IllegalArgumentException("kernel not square or not odd size.");
        }

        Image original = img.copy();

        int kernelCenterIdx = (kernel.length - 1) / 2;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int sum = 0;
                for (int kernelDeltaX = -kernelCenterIdx; kernelDeltaX <= kernelCenterIdx; kernelDeltaX++) {
                    for (int kernelDeltaY = -kernelCenterIdx; kernelDeltaY <= kernelCenterIdx; kernelDeltaY++) {
                        int sourceX = Math.min(Math.max(0, x + kernelDeltaX), img.getWidth() - 1);
                        int sourceY = Math.min(Math.max(0, y + kernelDeltaY), img.getHeight() - 1);

                        sum += original.getColor(sourceX, sourceY)
                                        * kernel[kernelCenterIdx - kernelDeltaX][kernelCenterIdx - kernelDeltaY];
                    }
                }
                img.setColor(x, y, sum);
            }
        }
    }
}
