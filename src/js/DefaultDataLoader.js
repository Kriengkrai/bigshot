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
 * Creates a new data loader.
 *
 * @param {int} [maxRetries=0] the maximum number of times to retry requests
 * @param {String} [crossOrigin] the CORS crossOrigin parameter to use when loading images
 * @class Data loader using standard browser functions.
 * @augments bigshot.DataLoader
 */
bigshot.DefaultDataLoader = function (maxRetries, crossOrigin) {
    this.maxRetries = maxRetries;
    this.crossOrigin = crossOrigin;
    
    if (!this.maxRetries) {
        this.maxRetries = 0;
    }    
}

bigshot.DefaultDataLoader.prototype = {
    browser : new bigshot.Browser (),
    
    loadImage : function (url, onloaded) {
        var tile = document.createElement ("img");
        tile.retries = 0;
        if (this.crossOrigin != null) {
            tile.crossOrigin = this.crossOrigin;
        }
        var that = this;
        this.browser.registerListener (tile, "load", function () {
                if (onloaded) {
                    onloaded (tile);
                }
            }, false);
        this.browser.registerListener (tile, "error", function () {
                tile.retries++;
                if (tile.retries <= that.maxRetries) {
                    setTimeout (function () {
                            tile.src = url;
                        }, tile.retries * 1000);
                } else {
                    if (onloaded) {
                        onloaded (null);
                    }
                }
            }, false);
        tile.src = url;
        return tile;
    },
    
    loadXml : function (url, asynchronous, onloaded) {
        if (asynchronous) {
            this.asyncLoadXml (url, onloaded, 0);
        } else {
            return this.syncLoadXml (url, onloaded);
        }
    },
    
    asyncLoadXml : function (url, onloaded, attempt) {
        var req = this.browser.createXMLHttpRequest ();
        var that = this;
        req.addEventListener ("load", function () {
                onloaded (req.responseXML);
            });
        req.addEventListener ("error", function () {
                if (req.status != 403 && req.status != 404 && attempt < that.maxRetries) {
                    that.asyncLoadXml (url, onloaded, attempt + 1);
                } else {
                    onloaded (null);
                }
            });
        req.open ("GET", url, true);   
        req.overrideMimeType ('text/xml');
        req.send ();
    },
    
    syncLoadXml : function (url) {
        for (var tries = 0; tries <= this.maxRetries; ++tries) {
            var req = this.browser.createXMLHttpRequest ();
            
            req.open("GET", url, false);
            req.overrideMimeType ('text/xml');
            req.send(null); 
            if (req.status == 200) {
                var xml = req.responseXML;
                if (xml != null) {
                    if (onloaded != null) {
                        onloaded (xml);
                    }
                    return xml;
                }
            } 
            
            // Give up immediately on 403 and 404
            if (req.status == 403 || req.status == 404 || tries == that.maxRetries) {
                if (onloaded != null) {
                    onloaded (null);
                }
                return null;
            }
        }
    }
}

bigshot.Object.validate ("bigshot.DefaultDataLoader", bigshot.DataLoader);
