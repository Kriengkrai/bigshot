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

/**
 * Math utility functions.
 */
public class MathUtil {
    
    /**
     * Private ctor.
     */
    private MathUtil () {
    }
    
    /**
     * Converts degrees to radians.
     *
     * @param deg the angle in degrees
     * @return the angle in radians
     */
    public static double toRad (double deg) {
        return deg * Math.PI / 180;
    }
    
    /**
     * Converts radians to degrees.
     *
     * @param rad the angle in radians
     * @return the angle in degrees
     */
    public static double toDeg (double rad) {
        return rad * 180 / Math.PI;
    }
    
    /**
     * Clamps the value between two endpoints.
     *
     * @param a the lowest value that will be returned
     * @param b the highest value that will be returned
     * @param x the value to clamp
     * @return a, if x &lt;= a; b, if x &gt;= b; x otherwise
     */
    public static int clamp (int a, int x, int b) {
        if (x < a) {
            return a;
        } else if (x > b) {
            return b;
        } else {
            return x;
        }
    }
    
    public static float sum (float[] kernel) {
        float sum = 0.0f;
        for (int i = 0; i < kernel.length; i++) {
            sum += kernel[i];
        }
        return sum;
    }
    
    public static float[] normalizeKernel (float[] kernel) {
        float sum = sum (kernel);
        if (sum > 0.0 || sum < 0.0) {
            for (int i = 0; i < kernel.length; i++) {
                kernel[i] /= sum;
            }
        }
        return kernel;
    }
    
    /**
     * Utility function to compute values for gaussian blur kernels.
     *
     * @param x position in the kernel array (zero should be the center element)
     * @param size the length of the kernel array
     */
    public static double kernelGaussianValueAt (int x, int size) {
        double sigma = size / 3.0;
        return kernelGaussianValue (x, sigma);
    }
    
    public static double kernelGaussianValue (int x, double sigma) {
        double factor = 1 / (Math.sqrt (2 * Math.PI) * sigma);
        return factor * Math.exp (-(x * x) / (2 * sigma * sigma));
    }
    
    /**
     * Alpha-adds x on top of y with alpha a.
     */
    public static int alphaOver (int x, int y, float a) {
        return (int) (x * a + y * (1.0f - a));
    }
}