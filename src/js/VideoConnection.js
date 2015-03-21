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

/**
 * @class A connection to a VR video source.
 */
bigshot.VideoConnection = function (src) {
    this.useCount = 1;
    this.src = src;
    this.videoElement = document.createElement ("video");
    this.videoElement.loop = true;
    this.videoElement.src = src;
    this.videoElement.play ();
    this.frameListeners = [];
    var that = this;
    this.lastTime = -1;
    this.playMonitor = function () {
        var current = that.videoElement.currentTime;
        if (that.lastTime != current) {
            that.lastTime = current;
            that.fireFrame ();
        }
        if (that.useCount > 0) {
            setTimeout (that.playMonitor, 1000 / 50);
        }
    }
    this.playMonitor ();
}

bigshot.VideoConnection.cache = {};

bigshot.VideoConnection.getConnection = function (url) {
    var connection = bigshot.VideoConnection.cache[url];
    if (connection == null) {
        connection = new bigshot.VideoConnection (url);
        bigshot.VideoConnection.cache[url] = connection;
    } else {
        connection.addUseCount ();
    }
    return connection;
};

bigshot.VideoConnection.prototype = {
    addAnycastFrameListener : function (listener) {
        this.frameListeners.push (listener);
    },
    
    removeAnycastFrameListener : function (listener) {
        var idx = this.frameListeners.indexOf (listener);
        if (idx >= 0) {
            this.frameListeners.splice (idx, 1);
        }
    },
    
    fireFrame : function (listener) {
        for (var i = 0; i < this.frameListeners.length; ++i) {
            this.frameListeners[i] (i == 0, false);
        }
        for (var i = 0; i < this.frameListeners.length; ++i) {
            this.frameListeners[i] (i == 0, true);
        }
    },
    
    getVideo : function () {
        return this.videoElement;
    },
    
    addUseCount : function () {
        ++this.useCount;
    },
    
    decUseCount : function () {
        --this.useCount;
    },
    
    dispose : function () {
        this.decUseCount ();
        if (this.useCount == 0) {
            delete bigshot.VideoConnection.cache[this.src];
            // ?
        }
    }
}

