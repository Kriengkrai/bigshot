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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageReader;
import javax.imageio.IIOImage;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageWriteParam;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import bigshot.mp.WorkSet;
import bigshot.mp.ParallelFor;

/**
 * Command-line tool to creates small "planets" from VR maps.
 * Run without parameters or with <code>--help</code> to see parameters.
 */
public class MakePlanet {
    
    private static void showHelp () throws Exception {
        byte[] buffer = new byte[1024];
        InputStream is = MakeImagePyramid.class.getResourceAsStream ("planet-help.txt");
        try {
            while (true) {
                int numRead = is.read (buffer);
                if (numRead < 1) {
                    break;
                }
                System.err.write (buffer, 0, numRead);
            }
        } finally {
            is.close ();
        }
    }
    
    /**
     * Command line interface. Parses the command line options and invokes the
     * {@link #process} method.
     * To run this fro your own code, use the {@link #process} method. This method
     * may call {@code System.exit()}.
     */
    public static void main (String[] args) throws Exception {
        if (args.length == 1 && (args[0].equals ("-h") || args[0].equals ("--help"))) {
            showHelp ();
            System.exit (0);
        } else if (args.length < 2) {
            showHelp ();
            System.err.println ("No input files specified.");
            System.exit (1);
        } else {
            File input = new File (args[0]);
            File output = new File (args[1]);
            Map<String,String> parameters = new HashMap<String,String> ();
            List<ImageInsert> inserts = new ArrayList<ImageInsert> ();
            for (int i = 2; i < args.length; i += 2) {
                if (args[i].startsWith ("--")) {
                    String key = args[i].substring (2);
                    String value = args[i + 1];
                    if (key.equals ("insert")) {
                        inserts.add (ImageInsert.parseAngles (value));
                    } else if (key.equals ("insert-vec")) {
                        inserts.add (ImageInsert.parseVectors (value));
                    } else {
                        parameters.put (key, value);
                    }
                }
            }
            
            process (input, output, new PlanetParameters (parameters), inserts);
        }
    }
    
    public static void process (File input, File output, PlanetParameters parameters, Collection<ImageInsert> inserts) throws Exception {
        AbstractSphericalCubicTransform<? extends AbstractCubicTransform> txform = null;
        
        if (parameters.transform () == PlanetParameters.Transform.CYLINDER) {
            txform = new CylindricalToCubic ();
        } else {
            txform = new EquirectangularToCubic ();
        }
        
        final AbstractSphericalCubicTransform<? extends AbstractCubicTransform> xform = txform;
        
        xform.input (Image.read (input, true))
            .offset (parameters.optYawOffset (0), parameters.optPitchOffset (0), parameters.optRollOffset (0));
        
        if (parameters.containsKey (PlanetParameters.TRANSFORM_PTO)) {
            xform.fromHuginPto (new File (parameters.transformPto ()));
        }
        if (parameters.containsKey (PlanetParameters.INPUT_VFOV)) {
            xform.inputVfov (parameters.inputVfov ());
        }
        if (parameters.containsKey (PlanetParameters.INPUT_HFOV)) {
            xform.inputHfov (parameters.inputHfov ());
        }
        if (parameters.containsKey (PlanetParameters.INPUT_HORIZON)) {
            xform.inputHorizon (parameters.inputHorizon ());
        }
        
        if (inserts != null) {
            for (ImageInsert ii : inserts) {
                ii.apply (xform);
            }
        }
        
        final int size = parameters.optOutputSize (1024);
        final double size2 = size / 2;
        final double inflectionIn = parameters.optInflectionIn (0.5f);
        final double inflectionOut = parameters.optInflectionOut (0.5f);
        final double groundInflection = 1.0 / parameters.optGroundInflection (1.0f);
        final double skyInflection = parameters.optSkyInflection (1.0f);
        final double scale = parameters.optScale (1.0f);
        final double center = parameters.optCenter (0.2f);
        final double maxNorm = parameters.optCover (false) ? 1.42 : 1.0;
        
        final boolean alpha = xform.input ().hasAlpha ();
        final Image outputImage = new Image (size, size);
        outputImage.addAlpha ();
        WorkSet.pfor (0, size, new ParallelFor<Void> () {
                public Void call (int a, int b) {
                    for (int y = a; y < b; ++y) {
                        Point2D p2d = new Point2D ();
                        for (int x = 0; x < size; ++x) {
                            double dx = (x - size2) / size2;
                            double dy = (y - size2) / size2;
                            double theta = Math.atan2 (dy, dx);
                            
                            double norm = Math.sqrt (dx * dx + dy * dy) / scale;
                            
                            double adjustedNorm = 0.0;
                            if (norm < inflectionIn) {
                                adjustedNorm = inflectionOut * Math.pow (norm / inflectionIn, groundInflection);
                            } else {
                                adjustedNorm = inflectionOut + (1.0 - inflectionOut) * Math.pow ((norm - inflectionIn) / (maxNorm - inflectionIn), skyInflection);
                            }
                            if (adjustedNorm < center) {
                                double w = adjustedNorm / center;
                                adjustedNorm = w * adjustedNorm + (1.0 - w) * norm;
                            }
                            
                            double phi = Math.PI / 2 - Math.PI * adjustedNorm;
                            
                            xform.transformPoint (theta, phi, p2d);
                            outputImage.value (x, y, xform.input ().sample (p2d.x, p2d.y));
                            if (adjustedNorm <= 1.0) {
                                if (alpha) {
                                    outputImage.alphaValue (x, y, xform.input ().sampleAlpha (p2d.x, p2d.y));
                                }
                            } else {
                                outputImage.alphaValue (x, y, 0);
                            }
                        }
                    }
                    return null;
                }
            });
        
        PlanetParameters.ImageFormat imageFormat = parameters.optImageFormat (PlanetParameters.ImageFormat.PNG);
        if (PlanetParameters.ImageFormat.PNG == imageFormat) {
            outputImage.write (output);
        } else if (PlanetParameters.ImageFormat.JPG == imageFormat) {
            outputImage.writeJpeg (output, parameters.optJpegQuality (0.7f));
        } else {
            assert false;
        }
    }
}