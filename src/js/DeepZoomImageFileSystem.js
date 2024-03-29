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

#include AbstractFileSystem.js
#include FileSystem.js
#include Object.js

/**
 * Creates a new instance of a Deep Zoom Image folder-based filesystem adapter.
 *
 * @augments bigshot.FileSystem
 * @augments bigshot.AbstractFileSystem
 * @class A Deep Zoom Image filesystem.
 * @param {bigshot.ImageParameters|bigshot.VRPanoramaParameters} parameters the associated image parameters
 * @constructor
 */
bigshot.DeepZoomImageFileSystem = function (parameters) {
    this.prefix = "";
    this.suffix = "";
    
    this.DZ_NAMESPACE = "http://schemas.microsoft.com/deepzoom/2009";
    this.fullZoomLevel = 0;
    this.posterName = "";
    this.parameters = parameters;
}

bigshot.DeepZoomImageFileSystem.prototype = {    
    getDescriptor : function (callback) {
        if (callback != null) {
            var that = this;
            this.parameters.dataLoader.loadXml (this.parameters.basePath + this.prefix + ".xml", true, function (xml) {
                    that.processDescriptor (xml, callback);
                });
        } else {
            var xml = this.parameters.dataLoader.loadXml (this.parameters.basePath + this.prefix + ".xml", false);
            return this.processDescriptor (xml, callback);
        }
        
    },
    
    processDescriptor : function (xml, callback) {
        var descriptor = {};
        var image = xml.getElementsByTagName ("Image")[0];
        var size = xml.getElementsByTagName ("Size")[0];
        descriptor.width = parseInt (size.getAttribute ("Width"));
        descriptor.height = parseInt (size.getAttribute ("Height"));
        descriptor.tileSize = parseInt (image.getAttribute ("TileSize"));
        descriptor.overlap = parseInt (image.getAttribute ("Overlap"));
        
        var mediaFormat = this.chooseMediaFormat (image.getAttribute ("Format"));
        descriptor.suffix = "." + mediaFormat.suffix;
        descriptor.mediaType = mediaFormat.type;
        
        descriptor.posterSize = descriptor.tileSize;
        
        this.suffix = descriptor.suffix;
        this.fullZoomLevel = Math.ceil (Math.log (Math.max (descriptor.width, descriptor.height)) / Math.LN2);
        
        descriptor.minZoom = -this.fullZoomLevel;
        var posterZoomLevel = Math.ceil (Math.log (descriptor.tileSize) / Math.LN2);
        this.posterName = this.getImageFilename (0, 0, posterZoomLevel - this.fullZoomLevel);
        if (callback) {
            callback (descriptor);
        }
        return descriptor;
    },
    
    setPrefix : function (prefix) {
        this.prefix = prefix;
    },
    
    getPosterFilename : function () {
        return this.posterName;
    },
    
    getFilename : function (name) {
        return this.parameters.basePath + this.prefix + "/" + name;
    },
    
    getImageFilename : function (tileX, tileY, zoomLevel) {
        var dziZoomLevel = this.fullZoomLevel + zoomLevel;
        var key = dziZoomLevel + "/" + tileX + "_" + tileY + this.suffix;
        return this.getFilename (key);
    }
};

bigshot.Object.extend (bigshot.DeepZoomImageFileSystem, bigshot.AbstractFileSystem);
bigshot.Object.validate ("bigshot.DeepZoomImageFileSystem", bigshot.FileSystem);
