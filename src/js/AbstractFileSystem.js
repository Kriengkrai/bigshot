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
 * Creates a new filesystem instance.
 *
 * @class Abstract base class for filesystems.
 */
bigshot.AbstractFileSystem = function () {
    
}

bigshot.AbstractFileSystem.prototype = {
    mediaTypesForExtension : {
        "jpg" : "image",
        "jpeg" : "image",
        "png" : "image",
        "gif" : "image",
        
        "webm" : "video",
        "mp4" : "video",
        "m4v" : "video",
        "flv" : "video",
        "mpg" : "video",        
        "avi" : "video"
    },
    
    getMediaTypeForExtension : function (extension) {
        return this.mediaTypesForExtension[extension.toLowerCase ()];
    },
    
    chooseMediaFormat : function (formatString, altFormats) {
        return {
            suffix : formatString,
            type : this.getMediaTypeForExtension (formatString)
        };
    }    
};



