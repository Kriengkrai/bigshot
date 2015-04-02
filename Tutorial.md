Using Bigshot requires four steps:

  1. Creating the image pyramid
  1. (Optional) Installing the PHP image-server.
  1. Loading the JS library
  1. Setting up the Bigshot images

# Creating the Image Pyramid #

Bigshot relies on an "image pyramid" to efficiently display huge images in the web browser. An image pyramid is a sequence of images where each image is half the resolution of the preceding. See [Inside Deep Zoom](http://gasi.ch/blog/inside-deep-zoom-1/) for an overview of how this works.

```
java -jar bigshot.jar input.jpg output.bigshot --format archive
```

When this process completes, we have an [image pyramid archive](ArchiveFileFormat.md) in output.bigshot. If you want to use this format, you must install the PHP file bigshot.php on your web server. Alternatively, we can generate the image pyramid as a folder structure, using:

```
java -jar bigshot.jar input.jpg output.bigshot
```

This will result in a directory named `output.bigshot` that will contain the image pyramid.

# Installing the PHP image-server #

**Only do this if you chose the image archive option above.**

Copy the PHP image server to the same directory on your webserver where you keep the output.bigshot file.

# Loading the JS Library #

The JS library is loaded as you would load any other JS file:

```
<script src="bigshot.js">/* */</script>
```

or, if you prefer to use the compressed version:

```
<script src="bigshot-compressed.js">/* */</script>
```

# Setting up Images #

Setup comprises three steps: Creating a container `div` element and installing a bigshot image in the container.

```
<div style="border:1px solid black; background: black; height:600px;" 
    id="container">
</div>
```

The setup should be done in the `onLoad` event of the document:

```
function myOnLoad () {
    var container = document.getElementById ("container");

    // When using image archive
    bsi = new bigshot.Image (
        new bigshot.ImageParameters ({ 
            basePath : "bigshot.php?file=output.bigshot",
            fileSystemType : "archive",
            container : container
        })
    );

    // When using folders
    bsi = new bigshot.Image (
        new bigshot.ImageParameters ({ 
            basePath : "output.bigshot",
            container : container
        })
    );
}
```

Finally we add the myOnLoad function to the `body` element.

```
<body onload="myOnLoad();">
...
</body>
```

Done!