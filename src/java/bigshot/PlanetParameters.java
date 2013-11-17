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

import java.util.Map;
import java.util.TreeMap;

/**
 * A typesafe interface to the parameters for MakePlanet.
 */
public class PlanetParameters extends TreeMap<String,String> {
    
    /**
     * Creates an empty parameter set.
     */
    public PlanetParameters () {
    }
    
    /**
     * Creates a parameter set by copying another set.
     */
    public PlanetParameters (Map<String,String> params) {
        this.putAll (params);
    }
    
    /**
     * Sets the given parameter if it is not set.
     */
    public void putIfEmpty (String key, String value) {
        if (!containsKey (key)) {
            put (key, value);
        }
    }
    
    @CLASSNAME
        PlanetParameters
        ;
    
    @STRINGENUM
        Transform
        The input image transform
        equirectangular
        Create a planet from an equirectangular projection image map
        cylinder
        Create a planet from a cylindrical projection image map
        ;
    
    @STRING 
        transformPto
        Sets the input-vfov, input-hfov and input-horizon parameters from a Hugin .pto file. Note: You must still specify the transform (cylindrical or equirectangular).
        ;
    
    @INTEGER
        inputWidth
        Width of the source image. Set internally by the application.
        ;
    
    @INTEGER
        inputHeight
        Height of the source image. Set internally by the application.
        ;
    
    @INTEGER
        outputSize
        Size of the output image.
        ;

    @FLOAT
        scale
        Linear scale applied to the image. Set to sqrt(2) (~1.42) to fill the output image.
        ;

    @FLOAT
        inflectionIn
        The relative phi angle at which mapping becomes linear.
        ;
    
    @FLOAT
        inflectionOut
        The (output) relative phi angle 
        ;
    
    @FLOAT
        groundInflection
        Exponent to use to remap angles below the horizon
        ;
    
    @FLOAT
        skyInflection
        Exponent to use to remap angles above the horizon
        ;
    
    @FLOAT
        center
        The extent (0.0-1.0) of the center patch where the output phi angle is blended with a linear curve to avoid a "pinch" effect.
        ;
    
    @FLOAT
        yawOffset
        The initial yaw offset to apply, in degrees.
        ;

    @FLOAT
        pitchOffset
        The initial pitch offset to apply, in degrees.
        ;

    @FLOAT
        rollOffset
        The initial roll offset to apply, in degrees.
        ;

    @INTEGER
        inputHorizon
        The y-coordinate of the horizon in the map image.
        ;

    @FLOAT
        inputVfov
        The vertical field of view of the map image, in degrees.
        ;

    @FLOAT
        inputHfov
        The horizontal field of view of the map image, in degrees.
        ;
    
    @STRINGENUM
        ImageFormat
        Image format for output. Default: PNG
        jpg 
        Create JPEG files.
        png 
        Create PNG files.
        ;
        
    @FLOAT
        jpegQuality
        Jpeg output quality, between 0.0 and 1.0. Only has effect if imageFormat is JPG. Default: 0.7
        ;	
    
    @BOOLEAN
        cover
        If set to true, will stretch the phi angles above the inflection point to cover the output square.
        ;	
}