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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RenderedImage;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageReader;
import javax.imageio.IIOImage;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageWriteParam;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Future;

/**
 * Command-line tool to creates the tiled image pyramids that are used by Bigshot.
 * Run without parameters or with <code>--help</code> to see parameters.
 */
public class MakeImagePyramid {
    
    private static interface DescriptorOutput {
        public void setSuffix (String suffix);
        public void setFullSize (int width, int height);
        public void setTileSize (int tileSize, int overlap, int minZoom);
        public void setPosterSize (int posterSize, int pw, int ph);
        public void configure (ImagePyramidParameters parameters);
        public void output (File targetFile) throws Exception;
    }
    
    private static class BigshotDescriptorOutput implements DescriptorOutput {
        
        private final StringBuilder descriptor = new StringBuilder ();
        
        public void setSuffix (String suffix) {
            descriptor.append (":suffix:" + suffix);
        }        
        
        public void setFullSize (int width, int height) {
            descriptor.append (":width:" + width + ":height:" + height);
        }
        
        public void setTileSize (int tileSize, int overlap, int minZoom) {
            descriptor.append (":tileSize:" + tileSize + ":overlap:" + overlap);
            descriptor.append (":minZoom:" + minZoom);
        }
        
        public void setPosterSize (int posterSize, int pw, int ph) {
            descriptor.append (":posterSize:" + posterSize + ":posterWidth:" + pw + ":posterHeight:" + ph);
        }
        
        public void configure (ImagePyramidParameters parameters) {
            
        }
        
        public void output (File folders) throws Exception {
            FileOutputStream descriptorOut = new FileOutputStream (new File (folders, "descriptor"));
            try {
                String d = descriptor.toString ();
                if (d.startsWith (":")) {
                    d = d.substring (1);
                }
                descriptorOut.write (d.getBytes ());
            } finally {
                descriptorOut.close ();
            }
        }
    }
    
    private static class DziDescriptorOutput implements DescriptorOutput {
        
        private final StringBuilder descriptor = new StringBuilder ();
        
        private String suffix;
        private int width;
        private int height;
        private int tileSize;
        private int overlap;
        
        public void setSuffix (String suffix) {
            this.suffix = suffix;
            if (this.suffix.startsWith (".")) {
                this.suffix = this.suffix.substring (1);
            }
        }        
        
        public void setFullSize (int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public void setTileSize (int tileSize, int overlap, int minZoom) {
            this.tileSize = tileSize;
            this.overlap = overlap;
        }
        
        public void setPosterSize (int posterSize, int pw, int ph) {
        }
        
        public void configure (ImagePyramidParameters parameters) {
            
        }
        
        public void output (File folders) throws Exception {
            /*
             * <?xml version=\"1.0\" encoding=\"utf-8\"?>
             * <Image TileSize=\"375\" Overlap=\"1\" Format=\"jpg\" ServerFormat=\"Default\" xmnls=\"http://schemas.microsoft.com/deepzoom/2009\">
             * <Size Width=\"1500\" Height=\"1500\" />
             * </Image>
             */
            StringBuilder descriptor = new StringBuilder (
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<Image TileSize=\"" + tileSize + "\" Overlap=\"" + overlap + "\" Format=\"" + suffix + "\" ServerFormat=\"Default\" xmnls=\"http://schemas.microsoft.com/deepzoom/2009\">\n" +
                "<Size Width=\"" + width + "\" Height=\"" + height + "\" />\n" +
                "</Image>\n"
                );
            
            FileOutputStream descriptorOut = new FileOutputStream (new File (folders.getParentFile (), folders.getName () + ".xml"));
            try {
                descriptorOut.write (descriptor.toString ().getBytes ());
            } finally {
                descriptorOut.close ();
            }
        }
    }
    
    
    private static interface Output {
        public void write (BufferedImage image, File output) throws Exception;
        public void writePoster (BufferedImage image, File output) throws Exception;
        public String getSuffix ();
        public void configure (ImagePyramidParameters parameters);
    }
    
    private static class PngOutput implements Output {
        public String getSuffix () {
            return ".png";
        }
        
        public void write (BufferedImage image, File output) throws Exception {
            ImageIO.write (image, "PNG", output);
        }
        
        public void writePoster (BufferedImage image, File output) throws Exception {
            write (image, output);
        }
        
        public void configure (ImagePyramidParameters parameters) {
        }
    }
    
    private static class JpegOutput implements Output {
        
        private double quality;
        private double posterQuality;
        private int posterBlur;
        
        public void configure (ImagePyramidParameters parameters) {
            quality = parameters.optJpegQuality (0.7f);
            posterQuality = parameters.optJpegPosterQuality (0.7f);
            posterBlur = parameters.optJpegPosterBlur (0);
        }
        
        public String getSuffix () {
            return ".jpg";
        }
        
        public void writeImage (BufferedImage image, File output, double imageQuality) throws Exception {
            ImageWriter writer = ImageIO.getImageWritersByFormatName ("jpeg").next ();
            try {
                ImageWriteParam iwp = writer.getDefaultWriteParam();
                
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwp.setCompressionQuality ((float) imageQuality);
                
                FileImageOutputStream os = new FileImageOutputStream (output);
                try {
                    writer.setOutput(os);
                    IIOImage iioImage = new IIOImage ((RenderedImage) image, null, null);
                    writer.write (null, iioImage, iwp);
                } finally {
                    os.close ();
                }
                
            } finally {
                writer.dispose();
            }
        }
        
        public void write (BufferedImage image, File output) throws Exception {
            writeImage (image, output, quality);
        }
        
        private double gaussianValueAt (int x, int size) {
            double sigma = size / 3.0;
            double factor = 1 / (Math.sqrt (2 * Math.PI) * sigma);
            return factor * Math.exp (-(x * x) / (2 * sigma * sigma));
        }
        
        private BufferedImage prepareForBlur (BufferedImage image) throws Exception {
            BufferedImage newImage = new BufferedImage(
                image.getWidth () + posterBlur,
                image.getHeight () + posterBlur,
                BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = newImage.createGraphics();
            g2.drawImage (
                image, 
                0, 0, image.getWidth() + posterBlur, image.getHeight() + posterBlur, 
                0, 0, image.getWidth (), image.getHeight (), 
                null);
            g2.dispose ();
            return newImage;
        }
        
        public void writePoster (BufferedImage image, File output) throws Exception {
            if (posterBlur > 0) {
                float[] blurKernel = new float[posterBlur];
                float sum = 0.0f;
                for (int i = 0; i < blurKernel.length; i++) {
                    blurKernel[i] = (float) gaussianValueAt (i - blurKernel.length / 2, blurKernel.length / 2);
                    sum += blurKernel[i];
                }
                for (int i = 0; i < blurKernel.length; i++) {
                    blurKernel[i] /= sum;
                }
                
                BufferedImageOp opW = new ConvolveOp (new Kernel (posterBlur, 1, blurKernel), ConvolveOp.EDGE_ZERO_FILL, null);
                BufferedImageOp opH = new ConvolveOp (new Kernel (1, posterBlur, blurKernel), ConvolveOp.EDGE_ZERO_FILL, null);
                
                BufferedImage prepared = prepareForBlur (image);
                BufferedImage blurredW = opW.filter (prepared, opW.createCompatibleDestImage (prepared, null));
                prepared = null;
                BufferedImage blurredWH = opH.filter (blurredW, opH.createCompatibleDestImage (blurredW, null));
                blurredW = null;
                
                BufferedImage finalImage = blurredWH.getSubimage (posterBlur / 2, posterBlur / 2, image.getWidth (), image.getHeight ());
                blurredWH = null;
                
                writeImage (finalImage, output, posterQuality);
            } else {
                writeImage (image, output, posterQuality);
            }
        }
    }
    
    private static void tile (BufferedImage full, int tileWidth, int overlap, File outputBase, Output output) throws Exception {
        BufferedImage tile = new BufferedImage (tileWidth, tileWidth, BufferedImage.TYPE_INT_RGB);
        int startOffset = 0;
        
        int ty = 0;
        for (int y = startOffset; y < full.getHeight () - overlap; y += tileWidth - overlap) {
            int tx = 0;
            for (int x = startOffset; x < full.getWidth () - overlap; x += tileWidth - overlap) {
                int w = Math.min (x + tileWidth, full.getWidth ()) - x;
                int h = Math.min (y + tileWidth, full.getHeight ()) - y;
                
                // System.out.println ("Generating tile " + tx + "," + ty + " = [" + x + "," + y + "] + [" + w + "," + h + "] -> [" + (x + w) + "," + (y + h) + "]...");
                
                BufferedImage section = full.getSubimage (x, y, w, h);
                Graphics2D g = tile.createGraphics ();
                g.setColor (Color.BLACK);
                g.fillRect (0, 0, tileWidth, tileWidth);
                g.drawImage (section, 0, 0, null);
                g.dispose ();
                String filename = tx + "_" + ty + output.getSuffix ();
                output.write (tile, new File (outputBase, filename));
                
                ++tx;
            }
            ++ty;
        }
    }
    
    private static void showHelp () throws Exception {
        byte[] buffer = new byte[1024];
        InputStream is = MakeImagePyramid.class.getResourceAsStream ("help.txt");
        try {
            while (true) {
                int numRead = is.read (buffer);
                if (numRead < 1) {
                    break;
                }
                System.err.write (buffer, 0, numRead);
            }
        } finally {
            is.close ();
        }
    }
    
    private static boolean isPowerOfTwo (int i) {
        return (i & (i - 1)) == 0;
    }
    
    private static void presetDziCubemap (ImagePyramidParameters parameters) throws Exception {
        int overlap = parameters.optOverlap (2);
        int tileSize = parameters.optTileSize (256 - overlap);
        
        int optimalImageSize = parameters.inputWidth () / 4;
        int optimalFaceSize = tileSize;
        while (optimalFaceSize < (optimalImageSize * 1.2) / 2) {
            optimalFaceSize <<= 1;
        }
        
        int faceSize = parameters.optFaceSize (optimalFaceSize);
        
        System.out.println ("Using face size " + faceSize + " optimal is " + optimalImageSize);
        
        if (!isPowerOfTwo (tileSize + overlap)) {
            System.err.println ("WARNING: Resulting image tile size (tile-size + overlap) is not a power of two:" + (tileSize + overlap));
        }
        
        if ((faceSize % tileSize) != 0) {
            System.err.println ("WARNING: face-size is not an even multiple of tile-size:" + faceSize + " % " + tileSize + " != 0");
        }
        
        parameters.putIfEmpty (ImagePyramidParameters.OVERLAP, String.valueOf (overlap));
        parameters.putIfEmpty (ImagePyramidParameters.TILE_SIZE, String.valueOf (tileSize));
        parameters.putIfEmpty (ImagePyramidParameters.FACE_SIZE, String.valueOf (faceSize));
        parameters.putIfEmpty (ImagePyramidParameters.TRANSFORM, ImagePyramidParameters.Transform.FACEMAP.toString ());
        
        int levels = (int) Math.ceil (Math.log (faceSize + overlap) / Math.log (2));
        parameters.putIfEmpty (ImagePyramidParameters.LEVELS, String.valueOf (levels + 1));
        parameters.putIfEmpty (ImagePyramidParameters.DESCRIPTOR_FORMAT, ImagePyramidParameters.DescriptorFormat.DZI.toString ());
        parameters.putIfEmpty (ImagePyramidParameters.FOLDER_LAYOUT, ImagePyramidParameters.FolderLayout.DZI.toString ());
        parameters.putIfEmpty (ImagePyramidParameters.LEVEL_NUMBERING, ImagePyramidParameters.LevelNumbering.INVERT.toString ());
    }
    
    private static void setInputImageParameters (ImagePyramidParameters parameters, File input) throws Exception {
        String path = input.getPath ();
        
        String suffix = path.substring (path.lastIndexOf ('.') + 1);
        for (Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix (suffix);
            iter.hasNext ();
            ) {
            
            ImageReader reader = iter.next ();
            try {
                ImageInputStream stream = new FileImageInputStream(new File(path));
                reader.setInput(stream);
                int width = reader.getWidth (reader.getMinIndex ());
                int height = reader.getHeight (reader.getMinIndex ());
                
                parameters
                    .inputWidth (width)
                    .inputHeight (height);
                return;
            } finally {
                reader.dispose();
            }
        }
        throw new Exception ("Unable to get input image size.");
    }
    
    /**
     * Command line interface. Parses the command line options and invokes the
     * {@link #process} method.
     * To run this fro your own code, use the {@link #process} method. This method
     * may call {@code System.exit()}.
     */
    public static void main (String[] args) throws Exception {
        if (args.length == 1 && (args[0].equals ("-h") || args[0].equals ("--help"))) {
            showHelp ();
            System.exit (0);
        } else if (args.length < 2) {
            showHelp ();
            System.err.println ("No input files specified.");
            System.exit (1);
        } else {
            File input = new File (args[0]);
            File outputBase = new File (args[1]);
            Map<String,String> parameters = new HashMap<String,String> ();
            List<ImageInsert> inserts = new ArrayList<ImageInsert> ();
            for (int i = 2; i < args.length; i += 2) {
                if (args[i].startsWith ("--")) {
                    String key = args[i].substring (2);
                    String value = args[i + 1];
                    if (key.equals ("insert")) {
                        inserts.add (ImageInsert.parseAngles (value));
                    } else if (key.equals ("insert-vec")) {
                        inserts.add (ImageInsert.parseVectors (value));
                    } else {
                        parameters.put (key, value);
                    }
                }
            }
            
            process (input, outputBase, new ImagePyramidParameters (parameters), inserts);
        }
    }
    
    /**
     * Creates an image pyramid.
     * 
     * @param input the input image map
     * @param outputBase the output base directory (for folder output) or bigshot archive file (for archive output)
     */
    public static void process (File input, File outputBase, ImagePyramidParameters parameters) throws Exception {
        process (input, outputBase, parameters, null);
    }
    
    public static void process (File input, File outputBase, ImagePyramidParameters parameters, Collection<ImageInsert> inserts) throws Exception {
        setInputImageParameters (parameters, input);
        
        if (parameters.preset () == ImagePyramidParameters.Preset.DZI_CUBEMAP) {
            presetDziCubemap (parameters);
        }
        
        if (parameters.transform () == ImagePyramidParameters.Transform.FACEMAP || 
            parameters.transform () == ImagePyramidParameters.Transform.CYLINDER_FACEMAP) {
            boolean archive = parameters.format () == ImagePyramidParameters.Format.ARCHIVE;
            
            AbstractCubicTransform<? extends AbstractCubicTransform> xform = null;
            if (parameters.transform () == ImagePyramidParameters.Transform.CYLINDER_FACEMAP) {
                xform = new CylindricalToCubic ();
            } else {
                xform = new EquirectangularToCubic ();
            }
            int xformFaceSize = parameters.optFaceSize (2048) + parameters.optOverlap (0);
            xform.input (input)
                .offset (parameters.optYawOffset (0), parameters.optPitchOffset (0), parameters.optRollOffset (0));
            
            if (parameters.containsKey (ImagePyramidParameters.TRANSFORM_PTO)) {
                xform.fromHuginPto (new File (parameters.transformPto ()));
            }
            if (parameters.containsKey (ImagePyramidParameters.INPUT_VFOV)) {
                xform.inputVfov (parameters.inputVfov ());
            }
            if (parameters.containsKey (ImagePyramidParameters.INPUT_HFOV)) {
                xform.inputHfov (parameters.inputHfov ());
            }
            if (parameters.containsKey (ImagePyramidParameters.INPUT_HORIZON)) {
                xform.inputHorizon (parameters.inputHorizon ());
            }
            
            if (inserts != null) {
                for (ImageInsert ii : inserts) {
                    ii.apply (xform);
                }
            }
            
            xform
                .vfov (90)
                .size (xformFaceSize, xformFaceSize)
                .oversampling (parameters.optOversampling (1))
                .jitter (parameters.optJitter (-1))
                .topCap (parameters.optTopCap (false))
                .bottomCap (parameters.optBottomCap (false));
            
            System.out.println (String.format (Locale.US, "Input FOV: %.2f x %.2f degrees", xform.inputHfov (), xform.inputVfov ()));
            
            parameters.remove (ImagePyramidParameters.FORMAT);
            parameters.remove (ImagePyramidParameters.FOLDER_LAYOUT);
            
            File pyramidBase = outputBase;
            if (archive) {
                pyramidBase = File.createTempFile ("makeimagepyramid", "bigshot");
                pyramidBase.delete ();
                pyramidBase.mkdirs ();
            }
            
            for (Future<Image> face : xform.transformToFaces ()) {
                Image img = face.get ();
                System.out.println ("Making pyramid for " + img.getName ());
                File out = new File (pyramidBase, img.getName ());
                BufferedImage buffered = img.toBuffered ();
                img = null;
                
                makePyramid (buffered, out, parameters);
            }
            
            if (archive) {
                pack (pyramidBase, outputBase);
                deleteAll (pyramidBase);
            }
        } else if (parameters.transform () == ImagePyramidParameters.Transform.FACE) {
            double fov = parameters.optFov (60);
            double yaw = parameters.optYaw (0);
            double pitch = parameters.optPitch (0);
            double roll = parameters.optRoll (0);
            double yawOffset = parameters.optYawOffset (0);
            double pitchOffset = parameters.optPitchOffset (0);
            double rollOffset = parameters.optRollOffset (0);
            int oversampling = parameters.optOversampling (1);
            double jitter = parameters.optJitter (-1);
            
            int outputSizeW = parameters.optOutputWidth (640);
            int outputSizeH = parameters.optOutputHeight (480);
            
            Output output = null;
            ImagePyramidParameters.ImageFormat imageFormat = parameters.optImageFormat (ImagePyramidParameters.ImageFormat.JPG);
            if (ImagePyramidParameters.ImageFormat.JPG == imageFormat) {
                output = new JpegOutput ();
            } else if (ImagePyramidParameters.ImageFormat.PNG == imageFormat) {
                output = new PngOutput ();
            } else {
                System.err.println ("Unknown image format: \"" + imageFormat + "\". Using JPEG.");
                output = new JpegOutput ();
            }
            output.configure (parameters);
            
            Image in = Image.read (input);
            
            Image outImage = new EquirectangularToCubic ()
                .input (in)
                .vfov (fov)
                .offset (yawOffset, pitchOffset, rollOffset)
                .view (yaw, pitch, roll)
                .size (outputSizeW, outputSizeH)
                .oversampling (oversampling)
                .jitter (jitter)
                .transform ();
            
            output.write (outImage.toBuffered (), outputBase);
        } else {
            makePyramid (input, outputBase, parameters);
        }
    }
    
    private static class PackageEntry {
        public String key;
        public File file;
        public long start;
        public long length;
        public String toString () {
            return key + ":" + start + "+" + length;
        }
    }
    
    private static long scan (File directory, List<PackageEntry> result, String relativePath, long currentPosition) {
        for (File f : directory.listFiles ()) {
            if (f.isDirectory ()) {
                currentPosition = scan (f, result, relativePath + f.getName () + "/", currentPosition);
            } else {
                PackageEntry p = new PackageEntry ();
                p.key = relativePath + f.getName ();
                p.file = f;
                p.start = currentPosition;
                p.length = f.length ();
                
                currentPosition += p.length;
                result.add (p);
            }
        } 
        return currentPosition;
    }
    
    private static void pack (File source, File outputBase) throws Exception {
        File packedOutput = outputBase;
        List<PackageEntry> fileList = new ArrayList<PackageEntry> ();
        scan (source, fileList, "", 0);
        System.out.println ("Packing " + fileList.size () + " files to " + packedOutput.getName ());
        
        byte[] buffer = new byte[128000];
        BufferedOutputStream packageOs = new BufferedOutputStream (new FileOutputStream (packedOutput));
        try {
            StringBuilder index = new StringBuilder ();
            for (PackageEntry pe : fileList) {
                index.append (pe.key);
                index.append (":");
                index.append (pe.start);
                index.append (":");
                index.append (pe.length);
                index.append (":");
            }
            
            byte[] indexBytes = index.toString ().getBytes ();
            byte[] header = String.format ("BIGSHOT %16x", indexBytes.length).getBytes ();
            
            packageOs.write (header);
            packageOs.write (indexBytes);
            
            for (PackageEntry pe : fileList) {
                FileInputStream is = new FileInputStream (pe.file);
                try {
                    while (true) {
                        int numRead = is.read (buffer);
                        if (numRead <= 0) {
                            break;
                        }
                        packageOs.write (buffer, 0, numRead);
                    }
                } finally {
                    is.close ();
                }
            }
        } finally {
            packageOs.close ();
        }
    }
    
    private static void makePyramid (File input, File outputBase, ImagePyramidParameters parameters) throws Exception {
        BufferedImage full = ImageIO.read (input);
        makePyramid (full, outputBase, parameters);
    }   
    
    private static void makePyramid (BufferedImage full, File outputBase, ImagePyramidParameters parameters) throws Exception {
        boolean outputPackage = parameters.format () == ImagePyramidParameters.Format.ARCHIVE;
        boolean dziLayout = parameters.folderLayout () == ImagePyramidParameters.FolderLayout.DZI;
        
        File folders = outputBase;
        
        if (outputPackage) {
            folders = File.createTempFile ("pyramid", "dir");
            folders.delete ();
            folders.mkdirs ();
        }
        folders.mkdirs ();
        
        if (dziLayout) {
            folders = new File (folders, outputBase.getName ());
            folders.mkdirs ();
        }
        
        Output output = null;
        ImagePyramidParameters.ImageFormat imageFormat = parameters.optImageFormat (ImagePyramidParameters.ImageFormat.JPG);
        if (ImagePyramidParameters.ImageFormat.JPG == imageFormat) {
            output = new JpegOutput ();
        } else if (ImagePyramidParameters.ImageFormat.PNG == imageFormat) {
            output = new PngOutput ();
        } else {
            System.err.println ("Unknown image format: \"" + imageFormat + "\". Using JPEG.");
            output = new JpegOutput ();
        }
        output.configure (parameters);
        
        
        DescriptorOutput descriptor = null;
        ImagePyramidParameters.DescriptorFormat descriptorFormat = parameters.optDescriptorFormat (ImagePyramidParameters.DescriptorFormat.BIGSHOT);
        if (ImagePyramidParameters.DescriptorFormat.BIGSHOT.equals (descriptorFormat)) {
            descriptor = new BigshotDescriptorOutput ();
        } else if (ImagePyramidParameters.DescriptorFormat.DZI.equals (descriptorFormat)) {
            descriptor = new DziDescriptorOutput ();
        } else {
            System.err.println ("Unknown descriptor format: \"" + descriptorFormat + "\". Using Bigshot.");
            descriptor = new BigshotDescriptorOutput ();
        }
        descriptor.configure (parameters);
        
        descriptor.setSuffix (output.getSuffix ());
        
        int w = full.getWidth ();
        int h = full.getHeight ();
        
        System.out.println ("Full image size: " + w + " x " + h + "");
        
        descriptor.setFullSize (w, h);
        
        int maxDimension = Math.max (w, h);
        
        {
            int posterSize = parameters.optPosterSize (512);
            double posterScale = ((double) posterSize) / maxDimension;
            
            int pw = (int) (w * posterScale);
            int ph = (int) (h * posterScale);
            
            descriptor.setPosterSize (posterSize, pw, ph);
            
            System.out.println ("Creating " + pw + " x " + ph + " poster image.");
            
            BufferedImage poster = new BufferedImage (pw, ph, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = poster.createGraphics ();
            g.drawImage (full.getScaledInstance (pw, ph, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, null);
            g.dispose ();
            
            output.writePoster (poster, new File (folders, "poster" + output.getSuffix ()));
        }   
        
        
        int tileSize = parameters.optTileSize (256) + parameters.optOverlap (0);
        int heuristicMaxZoom = (int) (Math.ceil (Math.log (maxDimension) / Math.log (2)) - Math.floor (Math.log (tileSize) / Math.log (2)) + 2);
        
        int maxZoom = parameters.optLevels ((int) heuristicMaxZoom);
        if (parameters.optWrapX (false)) {
            maxZoom = 0;
            int wxw = w;
            while (wxw % tileSize == 0) {
                wxw /= 2;
                maxZoom++;
            }
        }
        
        int overlap = parameters.optOverlap (0);
        System.out.println ("Creating pyramid with " + maxZoom + " levels.");
        for (int zoom = 0; zoom < maxZoom; ++zoom) {
            File outputDir = 
                ImagePyramidParameters.LevelNumbering.INVERT == parameters.levelNumbering ()
                ?
                new File (folders, String.valueOf (maxZoom - zoom - 1))
                :
                new File (folders, String.valueOf (zoom));
            outputDir.mkdirs ();
            tile (full, tileSize, overlap, outputDir, output);
            
            w = (w - overlap) / 2 + overlap;
            h = (h - overlap) / 2 + overlap;
            
            if (zoom < maxZoom - 1) {
                //System.out.println ("Reducing by factor of 2...");
                
                BufferedImage reduced = new BufferedImage (w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = reduced.createGraphics ();
                g.drawImage (full.getScaledInstance (w, h, java.awt.Image.SCALE_AREA_AVERAGING), 0, 0, null);
                g.dispose ();
                full = reduced;
            }
        }
        
        descriptor.setTileSize (tileSize, overlap, (-maxZoom + 1));
        
        descriptor.output (folders);
        
        if (outputPackage) {
            if (dziLayout) {
                pack (folders.getParentFile (), outputBase);
                deleteAll (folders.getParentFile ());
            } else {
                pack (folders, outputBase);
                deleteAll (folders);
            }
        }
    }
    
    
    private static void deleteAll (File f) {
        if (f.isDirectory ()) {
            for (File f2 : f.listFiles ()) {
                deleteAll (f2);
            }
            f.delete ();
        } else {
            f.delete ();
        }
    }
}