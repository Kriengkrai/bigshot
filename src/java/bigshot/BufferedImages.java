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
 * Utility functions for {@link BufferedImage}s.
 */
public class BufferedImages {
    
    /**
     * Image composition filter.
     */
    public static interface CompositeFilter {
        public void apply (int[] src, int[] dest, int[] out);
    }
    
    /**
     * Apply a composite filter to a pair of images. The operation is {@code dest = filter (src, dest)}.
     */
    public static void applyCompositeFilter (BufferedImage src, BufferedImage dest, CompositeFilter filter) throws Exception {
        int width = src.getWidth ();
        int height = src.getHeight ();
        
        final int srcLine[] = new int[width];
        final int destLine[] = new int[width];
        final int srcElements[] = new int[4];
        final int destElements[] = new int[4];
        final int outElements[] = new int[4];
        for (int y = 0; y < height; ++y) {
            src.getRGB (0, y, srcLine.length, 1, srcLine, 0, srcLine.length);
            dest.getRGB (0, y, destLine.length, 1, destLine, 0, destLine.length);
            
            for (int x = 0; x < width; ++x) {
                int p0 = srcLine[x];
                int p1 = destLine[x];
                
                srcElements[0] = (p0 >> 16) & 0xff;
                srcElements[1] = (p0 >> 8)  & 0xff;
                srcElements[2] = (p0)       & 0xff;
                srcElements[3] = (p0 >> 24) & 0xff;
                
                destElements[0] = (p1 >> 16) & 0xff;
                destElements[1] = (p1 >> 8)  & 0xff;
                destElements[2] = (p1)       & 0xff;
                destElements[3] = (p1 >> 24) & 0xff;
                
                filter.apply (srcElements, destElements, outElements);
                
                int p2 = (outElements[0] << 16) | (outElements[1] << 8) | (outElements[2]);
                destLine[x] = p2;
            }
            
            dest.setRGB (0, y, destLine.length, 1, destLine, 0, destLine.length);
        }
    }
    
}