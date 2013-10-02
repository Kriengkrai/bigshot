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

import java.io.File;
import java.util.StringTokenizer;

/**
 * An image that is projected into an existing panorama. The position of the image
 * can be given in two ways - either as a <i>{yaw, pitch, roll, vfov}</i> quadruplet
 * of angles, or as three vectors - <i>{topLeft, u, v}</i>. If the <i>topLeft</i>
 * vector is set, the image insert will use the latter method to insert the image.
 */
public class ImageInsert {
    
    private double yaw;
    private double pitch;
    private double roll;
    private double vfov;
    
    private Point3D topLeft;
    private Point3D u;
    private Point3D v;
    
    private File imageFile;
    private Image image;
    
    /**
     * Creates a new image insert.
     */
    public ImageInsert () {
    }
    
    /**
     * Gets the yaw of the center point of the image, in degrees.
     */
    public double yaw () {
        return yaw;
    }
    
    /**
     * Sets the yaw of the center point of the image, in degrees.
     *
     * @return this
     */
    public ImageInsert yaw (double yaw) {
        this.yaw = yaw;
        return this;
    }
    
    /**
     * Gets the pitch of the center point of the image, in degrees.
     */
    public double pitch () {
        return pitch;
    }
    
    /**
     * Sets the pitch of the center point of the image, in degrees.
     *
     * @return this
     */
    public ImageInsert pitch (double pitch) {
        this.pitch = pitch;
        return this;
    }
    
    /**
     * Gets the roll around the view axis (yaw and pitch)
     * of the image, in degrees.
     */
    public double roll () {
        return roll;
    }
    
    /**
     * Sets the roll around the view axis (yaw and pitch)
     * of the image, in degrees.
     *
     * @return this
     */
    public ImageInsert roll (double roll) {
        this.roll = roll;
        return this;
    }
    
    /**
     * Sets all view parameters at once.
     *
     * @param yaw the way in degrees
     * @param pitch the pitch in degrees
     * @param roll the roll in degrees
     *
     * @return this
     */
    public ImageInsert view (double yaw, double pitch, double roll) {
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        return this;
    }
    
    /**
     * Gets the image file to insert.
     */
    public File imageFile () {
        return imageFile;
    }
    
    /**
     * Sets an image file to insert. The image is loaded
     * when applying the insert.
     *
     * @return this
     */
    public ImageInsert imageFile (String imageFile) {
        this.imageFile = new File (imageFile);
        return this;
    }
    
    /**
     * Sets an image file to insert. The image is loaded
     * when applying the insert.
     *
     * @return this
     */
    public ImageInsert imageFile (File imageFile) {
        this.imageFile = imageFile;
        return this;
    }
    
    /**
     * Sets the image to insert. This overrides any {@link #imageFile(File)}
     * setting.
     *
     * @return this
     */
    public ImageInsert image (Image image) {
        this.image = image;
        return this;
    }
    
    /**
     * Gets the number of vertical degrees the image occupies.
     */
    public double vfov () {
        return vfov;
    }
    
    /**
     * Sets the number of degrees the image occupies. If the insert is
     * specified using <i>{topLeft,u,v}</i> then this parameter is 
     * ignored.
     *
     * @return this
     */
    public ImageInsert vfov (double vfov) {
        this.vfov = vfov;
        return this;
    }
    
    /**
     * Gets the <i>topLeft</i> vector, which is the bubble-space coordinate
     * of the top left corner of the image to be inserted.
     */
    public Point3D topLeft () {
        return topLeft;
    }
    
    /**
     * Sets the <i>topLeft</i> vector, which is the bubble-space coordinate
     * of the top left corner of the image to be inserted.
     *
     * @return this
     */
    public ImageInsert topLeft (Point3D topLeft) {
        this.topLeft = topLeft;
        return this;
    }
    
    /**
     * Gets the <i>u</i> vector, going from the top left corner to
     * the top right corner of the image.
     */
    public Point3D u () {
        return u;
    }
    
    /**
     * Sets the <i>u</i> vector, going from the top left corner to
     * the top right corner of the image.
     *
     * @return this
     */
    public ImageInsert u (Point3D u) {
        this.u = u;
        return this;
    }
    
    /**
     * Gets the <i>v</i> vector, going from the top left corner to
     * the bottom left corner of the image.
     */
    public Point3D v () {
        return v;
    }
    
    /**
     * Sets the <i>v</i> vector, going from the top left corner to
     * the bottom left corner of the image.
     *
     * @return this
     */
    public ImageInsert v (Point3D v) {
        this.v = v;
        return this;
    }
    
    /**
     * Helper method to parse a {@code double} from a string tokenizer.
     *
     * @return a new {@code double} parsed from the next token from the tokenizer
     */
    private static double nextDouble (StringTokenizer tok) {
        return Double.parseDouble (tok.nextToken ());
    }
    
    /**
     * Helper method to parse a {@link Point3D} from a string tokenizer.
     *
     * @return a new Point3D instance, created by the next three tokens from
     * the tokenizer parsed as {@code double}s.
     */
    private static Point3D nextPoint3D (StringTokenizer tok) {
        return new Point3D (nextDouble (tok), nextDouble (tok), nextDouble (tok));
    }
    
    /**
     * Parses a command-line parameter describing the image
     * insert. The parameter is a comma-separated list
     * consisting of ten elements: 
     * <i>topLeft.x,topLeft.y,topLeft.z,u.x,u.y,u.z,v.x,v.y,v.z,imageFile</i>.
     *
     * @return a new ImageInsert instance with the topLeft, u, and v and imageFile parameters set
     */
    public static ImageInsert parseVectors (String decl) {
        StringTokenizer tok = new StringTokenizer (decl, ",");
        return new ImageInsert ()
            .topLeft (nextPoint3D (tok))
            .u (nextPoint3D (tok))
            .v (nextPoint3D (tok))
            .imageFile (tok.nextToken ());
    }
    
    /**
     * Parses a command-line parameter describing the image
     * insert. The parameter is a comma-separated list
     * consisting of five elements: <i>yaw,pitch,roll,vfov,imageFile</i>.
     *
     * @return a new ImageInsert instance with the yaw, pitch, roll, vfov, and imageFile parameters set
     */
    public static ImageInsert parseAngles (String decl) {
        StringTokenizer tok = new StringTokenizer (decl, ",");
        return new ImageInsert ()
            .yaw (nextDouble (tok))
            .pitch (nextDouble (tok))
            .roll (nextDouble (tok))
            .vfov (nextDouble (tok))
            .imageFile (tok.nextToken ());
    }
    
    /**
     * Inserts the image into the panorama. If the insert position has been given as
     * a <i>{topLeft, u, v}</i>, the transform is unchanged. Otherwise, the 
     * transform's view and vfov are set and previous settings lost.
     */
    public void apply (AbstractCubicTransform xform) throws Exception {
        Image insertImg = image;
        if (insertImg == null) {
            insertImg = Image.read (imageFile, true);
        }
        if (topLeft == null) {
            xform.view (yaw (), pitch (), roll ());
            xform.vfov (vfov ());
            xform.insert (insertImg);
        } else {
            xform.insert (insertImg, topLeft, u, v);
        }
        
        // Dispose of the image when done.
        insertImg = null;
    }
}