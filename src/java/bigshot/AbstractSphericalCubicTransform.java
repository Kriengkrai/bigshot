/*
 * Copyright 2010 - 2012 Leo Sutic <leo.sutic@gmail.com>
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

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageReader;
import bigshot.mp.WorkSet;
import bigshot.mp.ParallelFor;

import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for transforms where the transformation to image map space can be expressed in 
 * terms of the two angles theta and phi of the sight ray. Subclasses should override
 * {@link #transformPoint}.
 *
 * @param <Derived> for subclasses, the most derived class name. Used to implement the
 * method chaining for builder methods.
 */
public abstract class AbstractSphericalCubicTransform<Derived extends AbstractCubicTransform> extends AbstractCubicTransform<Derived> {
    
    /**
     * Creates a new transform instance.
     */
    public AbstractSphericalCubicTransform () {
    }
    
    /**
     * Performs the transformation.
     */
    @Override
        public Image transform () throws Exception {
        final Image output = new Image (width, height);
        
        final Point3D topLeft = new Point3D (-Math.tan (vfov / 2) * width / height, -Math.tan (vfov / 2), 1.0);
        final Point3D uv = new Point3D (- 2 * topLeft.x / width, - 2 * topLeft.y / height, 0.0);
        if (oversampling != 1) {
            uv.scale (1.0 / oversampling);
        }
        
        final Point3DTransform transform = new Point3DTransform ();
        transform.rotateZ (MathUtil.toRad (roll));
        transform.rotateX (MathUtil.toRad (pitch));
        transform.rotateY (MathUtil.toRad (yaw));
        
        transform.rotateY (MathUtil.toRad (oy));
        transform.rotateX (MathUtil.toRad (op));
        transform.rotateZ (MathUtil.toRad (or));
        
        final FastTrigInverse.FastAcos fastAcos = new FastTrigInverse.FastAcos (input.width () * 2 * oversampling);
        final FastTrigInverse.FastAtan fastAtan = new FastTrigInverse.FastAtan (input.height () * 2 * oversampling);
        
        final Point2D topLinePhi = new Point2D ();
        invTransformPoint (0, 0, topLinePhi);
        final Point2D bottomLinePhi = new Point2D ();
        invTransformPoint (0, input.height () - 1, bottomLinePhi);
        
        WorkSet.pfor (0, height, new ParallelFor<Void> () {
                public Void call (int startY, int endY) {
                    
                    final Point3D point = new Point3D (0,0,0);
                    final int[] oversamplingBuffer = new int[width * 3];
                    final int[] sampleBuffer = new int[3];
                    final Point2D transformOut = new Point2D ();
                    for (int destY = startY; destY < endY; ++destY) {
                        Arrays.fill (oversamplingBuffer, 0);
                        for (int y = destY * oversampling; y < destY * oversampling + oversampling; ++y) {
                            for (int x = 0; x < width * oversampling; ++x) {
                                point.x = topLeft.x;
                                point.y = topLeft.y;
                                point.z = topLeft.z;
                                if (jitter > 0.0) {
                                    point.translate3D (
                                        (x + Math.random () * jitter) * uv.x,
                                        (y + Math.random () * jitter) * uv.y, 0.0);
                                } else {
                                    point.translate3D (x * uv.x, y * uv.y, 0.0);
                                }
                                
                                transform.transform (point);
                                
                                double theta = 0.0;
                                double phi = 0.0;
                                
                                double nxz = Math.sqrt (point.x * point.x + point.z * point.z);
                                if (nxz < Double.MIN_NORMAL) {
                                    if (point.y > 0) {
                                        phi = MathUtil.toRad (90);
                                    } else {
                                        phi = MathUtil.toRad (-90);
                                    }
                                } else {
                                    phi = fastAtan.f (point.y / nxz);
                                    theta = fastAcos.f (point.z / nxz); //Math.acos (
                                    if (point.x < 0) {
                                        theta = -theta;
                                    }
                                }
                                
                                transformPoint (theta, phi, transformOut);
                                double inX = transformOut.x;
                                double inY = transformOut.y;
                                
                                if (inY >= 0 && inY < input.height () + 1 && (horizontalWrap || (inX >= 0 && inX < input.width ()))) {
                                    if (inY >= input.height () - 1 || (!horizontalWrap && inX >= input.width () - 1)) {
                                        input.componentValue ((int) inX, (int) inY, sampleBuffer);
                                    } else {
                                        input.sampleComponents (inX, inY, sampleBuffer);
                                    }
                                } else if (inY < 0 && topCap) {
                                    double arcWidth = (phi - topLinePhi.y) / ((-Math.PI/2) - topLinePhi.y);
                                    
                                    // Here by the singularity we may get some calculations come out with the wrong sign
                                    // due to numerical imprecision. Just flip it back.
                                    if (arcWidth < 0) {
                                        arcWidth = -arcWidth;
                                    }
                                    
                                    if (arcWidth < 0.5) {
                                        arcWidth /= 2;
                                    } else {
                                        arcWidth = (arcWidth * arcWidth);
                                    }
                                    
                                    arcSample (0, arcWidth * input.width (), inX, sampleBuffer);
                                } else if (inY >= input.height () && bottomCap) {
                                    double arcWidth = (phi - bottomLinePhi.y) / ((Math.PI/2) - bottomLinePhi.y);
                                    
                                    // Here by the singularity we may get some calculations come out with the wrong sign
                                    // due to numerical imprecision. Just flip it back.
                                    if (arcWidth < 0) {
                                        arcWidth = -arcWidth;
                                    }
                                    
                                    if (arcWidth < 0.5) {
                                        arcWidth /= 2;
                                    } else {
                                        arcWidth = (arcWidth * arcWidth);
                                    }
                                    
                                    arcSample (input.height () - 1, arcWidth * input.width (), inX, sampleBuffer);
                                } else {
                                    sampleBuffer[0] = 0;
                                    sampleBuffer[1] = 0;
                                    sampleBuffer[2] = 0;
                                }
                                
                                int obx = x / oversampling;
                                obx *= 3;
                                for (int i = 0; i < sampleBuffer.length; ++i) {
                                    oversamplingBuffer[obx + i] += sampleBuffer[i];
                                }
                            }
                        }
                        int oversampling2 = oversampling * oversampling;
                        for (int x = 0; x < oversamplingBuffer.length; ++x) {
                            oversamplingBuffer[x] /= oversampling2;
                        }
                        for (int x = 0; x < width; ++x) {
                            output.componentValue (x, destY, oversamplingBuffer[x * 3 + 0], oversamplingBuffer[x * 3 + 1], oversamplingBuffer[x * 3 + 2]);
                        }
                    }
                    return null;
                }
            });
        
        return output;
    }
    
    protected void arcSample (int y, double arcWidth, double inX, int[] sampleBuffer) {
        // Should really use two summed area tables here - one for the top line,
        // one for the bottom line. But nobody is complaining about performance yet.
        
        int arcMin = (int) (inX - arcWidth / 2);
        int arcMax = ((int) (inX + arcWidth / 2)) + 1;
        
        int[] asb = new int[3];
        sampleBuffer[0] = 0;
        sampleBuffer[1] = 0;
        sampleBuffer[2] = 0;
        
        int c = 0;
        
        int step = (arcMax - arcMin) / 256;
        if (step < 1) {
            step = 1;
        }
        
        for (int ax = arcMin; ax < arcMax; ax += step) {
            input.sampleComponents (ax, y, asb);
            sampleBuffer[0] += asb[0];
            sampleBuffer[1] += asb[1];
            sampleBuffer[2] += asb[2];
            ++c;
        }
        
        sampleBuffer[0] /= c;
        sampleBuffer[1] /= c;
        sampleBuffer[2] /= c;
    }
    
    public void insert (final Image image, final Point3D topLeft, final Point3D u, final Point3D v) throws Exception {
        final Point3D planeNormal = u.cross (v);
        
        final double num = topLeft.dot (planeNormal);
        final double u2 = u.norm2 ();
        final double v2 = v.norm2 ();
        
        WorkSet.pfor (0, input.height (), new ParallelFor<Void> () {
                public Void call (int a, int b) {
                    int[] sba = new int[4];
                    int[] sbb = new int[3];
                    
                    Point3D ray = new Point3D ();
                    Point2D rayAngle = new Point2D ();
                    
                    for (int y = a; y < b; ++y) {
                        for (int x = 0; x < input.width (); ++x) {
                            invTransformPoint (x, y, rayAngle);
                            double theta = rayAngle.x;
                            double phi = rayAngle.y;
                            ray.y = Math.sin (phi);
                            double cosphi = Math.cos (phi);
                            ray.x = Math.cos (Math.PI / 2 - theta) * cosphi;
                            ray.z = Math.sin (Math.PI / 2 - theta) * cosphi;
                            double denom = ray.dot (planeNormal);
                            
                            if (denom < 0.000001) {
                                continue;
                            }
                            double d = num / denom;
                            if (d < 0) {
                                continue;
                            }
                            ray.scale (d);
                            
                            ray.translate3D (-topLeft.x, -topLeft.y, -topLeft.z);
                            
                            double uN = ray.dot (u) / u2;
                            double vN = ray.dot (v) / v2;
                            
                            double imageX = uN * image.width ();
                            double imageY = vN * image.height ();
                            
                            if (imageX >= 0 && imageX <= image.width () - 1 && imageY >= 0 && imageY <= image.height () - 1) {
                                image.sampleComponents (imageX, imageY, sba);
                                input.componentValue (x, y, sbb);
                                for (int i = 0; i < sbb.length; ++i) {
                                    sbb[i] = sbb[i] * (Image.COMPONENT_MAX_VALUE - sba[3]) / Image.COMPONENT_MAX_VALUE + sba[i] * sba[3] / Image.COMPONENT_MAX_VALUE;
                                }
                                
                                input.componentValue (x, y, sbb[0], sbb[1], sbb[2]);
                            }
                        }
                    }
                    return null;
                }
            });
    }
    
    /**
        * Transforms a ray in 3d-space, given by {@code theta} and {@code phi} to 
        * image map coordinates.
        *
        * @param theta the yaw angle of the ray, in radians. Increases clockwise.
        * @param phi the pitch angle of the ray, in radians. Increases downwards.
        * @param output the result of the transformation. Gives the image map coordinates
        * in pixels
        */
    protected abstract void transformPoint (double theta, double phi, Point2D output);
    
    /**
        * Transforms an image map coordinate to a ray in 3d-space.
        *
        * @param x the x-coordinate in the image map.
        * @param y the y-coordinate in the image map.
        * @param output the result of the transformation. Gives the ray angle in radians
        * as x = theta, y = phi
        */
    protected abstract void invTransformPoint (int x, int y, Point2D output);
}