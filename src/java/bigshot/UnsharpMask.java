/*
 * Copyright 2010 - 2013 Leo Sutic <leo.sutic@gmail.com>
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package bigshot;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * An "unsharp mask" filter.
 */
public class UnsharpMask implements BufferedImages.CompositeFilter {
    
    private final ConvolveOp sharpenOp;
    private final float radius;
    private final float intensity;
    private final int threshold;
    
    /**
     * Creates a new filter.
     *
     * @param radius the radius of the blur
     * @param intensity the alpha value that is used when writing back the results of the filter.
     * @param threshold the difference in levels (in any channel) at which the filter is applied at all.
     */
    public UnsharpMask (float radius, float intensity, int threshold) {
        this.radius = radius;
        this.intensity = intensity;
        this.threshold = threshold;
        
        int kernelRadius = (int) Math.ceil (radius);
        int kernelSize = (2 * kernelRadius) + 1;
        float[] sharpenKernelValues = new float[kernelSize * kernelSize];
        for (int y = 0; y < kernelSize; ++y) {
            for (int x = 0; x < kernelSize; ++x) {
                int index = y * kernelSize + x;
                if (x != kernelRadius || y != kernelRadius) {
                    sharpenKernelValues[index] = - (float) MathUtil.kernelGaussianValue (x - kernelRadius, radius / 3.0f) - (float) MathUtil.kernelGaussianValue (y - kernelRadius, radius / 3.0f);
                }
            }
        }
        sharpenKernelValues[kernelRadius * kernelSize + kernelRadius] = MathUtil.sum (sharpenKernelValues) * (-2);
        
        Kernel sharpenKernel = new Kernel(kernelSize, kernelSize, MathUtil.normalizeKernel (sharpenKernelValues)); 
        
        sharpenOp = new ConvolveOp (sharpenKernel, ConvolveOp.EDGE_NO_OP, null);
    }
    
    /**
     * Applies the filter to an image.
     */
    public void apply (BufferedImage image) throws Exception {
        BufferedImage sharpened = sharpenOp.filter (image, null);
        BufferedImages.applyCompositeFilter (sharpened, image, this);
    }
    
    /**
     * Applies the final alpha-over effect - this method is used internally.
     */
    @Override
        public void apply (int[] src, int[] dest, int[] out) {
        if (Math.abs (src[0] - dest[0]) >= threshold || 
            Math.abs (src[1] - dest[1]) >= threshold ||
            Math.abs (src[2] - dest[2]) >= threshold) {
            
            out[0] = MathUtil.alphaOver (src[0], dest[0], intensity);
            out[1] = MathUtil.alphaOver (src[1], dest[1], intensity);
            out[2] = MathUtil.alphaOver (src[2], dest[2], intensity);
        } else {
            System.arraycopy (src, 0, out, 0, out.length);
        }
    }
}
