/*
 * Copyright 2010 - 2015 Leo Sutic <leo@sutic.nu>
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
if (!self["bigshot"]) {
    /**
     * @namespace Bigshot namespace.
     *
     * Bigshot is a toolkit for zoomable images and VR panoramas.
     * 
     * <h3>Zoomable Images</h3>
     *
     * <p>The two classes that are needed for zoomable images are:
     *
     * <ul>
     * <li>{@link bigshot.Image}: The main class for making zoomable images. See the class docs
     *     for a tutorial.
     * <li>{@link bigshot.ImageParameters}: Parameters for zoomable images.
     * <li>{@link bigshot.SimpleImage}: A class for making simple zoomable images that don't
     * require the generation of an image pyramid.. See the class docs for a tutorial.
     * </ul>
     *
     * For hotspots, see:
     *
     * <ul>
     * <li>{@link bigshot.HotspotLayer}
     * <li>{@link bigshot.Hotspot}
     * <li>{@link bigshot.LabeledHotspot}
     * <li>{@link bigshot.LinkHotspot}
     * </ul>
     *
     * <h3>VR Panoramas</h3>
     *
     * <p>The two classes that are needed for zoomable VR panoramas (requires WebGL) are:
     *
     * <ul>
     * <li>{@link bigshot.VRPanorama}: The main class for making VR panoramas. See the class docs
     *     for a tutorial.
     * <li>{@link bigshot.VRPanoramaParameters}: Parameters for VR panoramas. 
     * </ul>
     *
     * For hotspots, see:
     *
     * <ul>
     * <li>{@link bigshot.VRHotspot}
     * <li>{@link bigshot.VRRectangleHotspot}
     * <li>{@link bigshot.VRPointHotspot}
     * </ul>
     */
    bigshot = {};
    
    /*
     * This is supposed to be processed using a minimalhttpd.IncludeProcessor
     * during development. The files must be listed in dependency order.
     */
    #include types.js
    #include AdaptiveLODMonitor.js
    #include AdaptiveLODMonitorParameters.js
    #include CachingDataLoader.js
    #include HTMLElementLayer.js
    #include HotspotLayer.js
    #include Image.js
    #include ImageParameters.js
    #include LinkHotspot.js
    #include PointHotspot.js
    #include SimpleImage.js
    #include TexturedQuad.js
    #include TexturedQuadScene.js
    #include VRPanoramaParameters.js
    #include VRPointHotspot.js
    #include VRRectangleHotspot.js
}