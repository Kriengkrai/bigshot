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

#include AbstractVideoTileCache.js
#include Object.js
#include VRTileCache.js

/**
 * Creates a new cache instance.
 *
 * @class Tile texture cache for a {@link bigshot.VRFace}.
 * @augments bigshot.AbstractVideoTileCache
 * @param {function()} onLoaded function that is called whenever a texture tile has been loaded
 * @param {function()} onCacheInit function that is called when the texture cache is fully initialized
 * @param {bigshot.VRPanoramaParameters} parameters image parameters
 * @param {bigshot.WebGL} _webGl WebGL instance to use
 */
bigshot.VideoTextureTileCache = function (onLoaded, onCacheInit, parameters, _webGl) {
    bigshot.AbstractVideoTileCache.call (this, onLoaded, onCacheInit, parameters);
    this.webGl = _webGl;
    
    this.canvas = document.createElement ("canvas");
    this.canvas.width = this.srcRect.w;
    this.canvas.height = this.srcRect.h;
    
    this.texture = null;
    
    var that = this;
    this.frameListener = function (isFirst, secondPass) {
        if (!secondPass) {
            that.captureVideo ();
        }
        if (secondPass && isFirst) {
            that.onLoaded ();
        }
    };
    this.connection.addAnycastFrameListener (this.frameListener);
    parameters.overlap = 1;
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
    
    dispose : function dispose () {
        this.connection.removeAnycastFrameListener (this.frameListener);
        if (this.texture != null) {
            this.webGl.deleteTexture (this.texture);
        }
        dispose._super.call (this);
    }
};

bigshot.Object.extend (bigshot.VideoTextureTileCache, bigshot.AbstractVideoTileCache);
bigshot.Object.validate ("bigshot.VideoTextureTileCache", bigshot.VRTileCache);
