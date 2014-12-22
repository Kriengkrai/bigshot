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
 * @param {function()} onLoaded function that is called whenever a texture tile has been loaded
 * @param {function()} onCacheInit function that is called when the texture cache is fully initialized
 * @param {bigshot.VRPanoramaParameters} parameters image parameters
 */
bigshot.AbstractVideoTileCache = function (onLoaded, onCacheInit, parameters) {
    this.parameters = parameters;
    
    var key = parameters.fileSystem.prefix;
    key = key.substring (key.length - 1);
    
    this.onLoaded = onLoaded;
    this.browser = new bigshot.Browser ();
    this.disposed = false;
    
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
    
    this.source = parameters.basePath + "face_bdflru/9/0_0" + parameters.suffix;
    this.connection = bigshot.VideoConnection.getConnection (this.source);
}

bigshot.AbstractVideoTileCache.prototype = {
    
    dispose : function () {
        this.disposed = true;
        this.connection.dispose ();
    }
};



