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

/**
 * Creates a new cache instance.
 *
 * @class Tile texture cache for a {@link bigshot.VRFace}.
 * @augments bigshot.VideoTextureTileCache
 * @param {function()} onLoaded function that is called whenever a texture tile has been loaded
 * @param {function()} onCacheInit function that is called when the texture cache is fully initialized
 * @param {bigshot.VRPanoramaParameters} parameters image parameters
 * @param {bigshot.WebGL} _webGl WebGL instance to use
 */
bigshot.VideoTextureTileCache = function (onLoaded, onCacheInit, parameters, _webGl) {
    this.parameters = parameters;
    this.webGl = _webGl;
    
    var key = parameters.fileSystem.prefix;
    key = key.substring (key.length - 1);
    
    var index = "bdflru".indexOf (key);
    var xpos = index % 3;
    var ypos = Math.floor (index / 3);
    var pxpos = (parameters.tileSize + 2 * parameters.overlap) * xpos + parameters.overlap;
    var pypos = (parameters.tileSize + 2 * parameters.overlap) * ypos + parameters.overlap;
    this.srcRect = {
        x : pxpos,
        y : pypos,
        w : parameters.width,
        h : parameters.height
    };
    
    this.source = parameters.basePath + "face_bdflru/9/0_0.webm";
    this.connection = new bigshot.VideoConnection (this.source);
    
    this.canvas = document.createElement ("canvas");
    this.canvas.width = this.srcRect.w;
    this.canvas.height = this.srcRect.h;
    
    this.onLoaded = onLoaded;
    this.browser = new bigshot.Browser ();
    this.texture = null;
    this.disposed = false;
    
    var that = this;
    this.frameListener = function () {
        that.captureVideo ();
    };
    this.connection.addAnycastFrameListener (this.frameListener);
    setTimeout (onCacheInit, 0);
}

bigshot.VideoTextureTileCache.prototype = {
    
    captureVideo : function () {
        var ctx = this.canvas.getContext ("2d");
        ctx.drawImage (this.connection.getVideo (), -this.srcRect.x, -this.srcRect.y);
        if (this.texture != null) {
            this.webGl.deleteTexture (this.texture);
        }
        this.texture = this.webGl.createImageTextureFromImage (this.canvas, this.parameters.textureMinFilter, this.parameters.textureMagFilter);
        this.onLoaded ();
    },
        
    getTexture : function (tileX, tileY, zoomLevel) {
        return this.texture;
    },
    
    purge : function () {
    },
    
    getImageFilename : function (tileX, tileY, zoomLevel) {
        var f = this.parameters.fileSystem.getImageFilename (tileX, tileY, zoomLevel);
        return f;
    },
    
    dispose : function () {
        this.disposed = true;
        this.connection.removeAnycastFrameListener (this.frameListener);
        this.connection.dispose ();
        if (this.texture != null) {
            this.webGl.deleteTexture (this.texture);
        }
    }
};


bigshot.Object.validate ("bigshot.VideoTextureTileCache", bigshot.VRTileCache);
