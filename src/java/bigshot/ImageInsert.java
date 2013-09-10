package bigshot;

import java.io.File;
import java.util.StringTokenizer;

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
    
    public ImageInsert () {
    }
    
    public double yaw () {
        return yaw;
    }
    
    public ImageInsert yaw (double yaw) {
        this.yaw = yaw;
        return this;
    }
    
    public double pitch () {
        return pitch;
    }
    
    public ImageInsert pitch (double pitch) {
        this.pitch = pitch;
        return this;
    }
    
    public double roll () {
        return roll;
    }
    
    public ImageInsert roll (double roll) {
        this.roll = roll;
        return this;
    }
    
    public File imageFile () {
        return imageFile;
    }
    
    public ImageInsert imageFile (String imageFile) {
        this.imageFile = new File (imageFile);
        return this;
    }
    
    public ImageInsert imageFile (File imageFile) {
        this.imageFile = imageFile;
        return this;
    }
    
    public ImageInsert image (Image image) {
        this.image = image;
        return this;
    }
    
    public double vfov () {
        return vfov;
    }
    
    public ImageInsert vfov (double vfov) {
        this.vfov = vfov;
        return this;
    }
    
    public Point3D topLeft () {
        return topLeft;
    }
    
    public ImageInsert topLeft (Point3D topLeft) {
        this.topLeft = topLeft;
        return this;
    }
    
    public Point3D u () {
        return u;
    }
    
    public ImageInsert u (Point3D u) {
        this.u = u;
        return this;
    }
    
    public Point3D v () {
        return v;
    }
    
    public ImageInsert v (Point3D v) {
        this.v = v;
        return this;
    }
    
    private static double nextDouble (StringTokenizer tok) {
        return Double.parseDouble (tok.nextToken ());
    }
    
    private static Point3D nextPoint3D (StringTokenizer tok) {
        return new Point3D (nextDouble (tok), nextDouble (tok), nextDouble (tok));
    }
    
    public static ImageInsert parseVectors (String decl) {
        StringTokenizer tok = new StringTokenizer (decl, ",");
        return new ImageInsert ()
            .topLeft (nextPoint3D (tok))
            .u (nextPoint3D (tok))
            .v (nextPoint3D (tok))
            .imageFile (tok.nextToken ());
    }
    
    public static ImageInsert parseAngles (String decl) {
        StringTokenizer tok = new StringTokenizer (decl, ",");
        return new ImageInsert ()
            .yaw (nextDouble (tok))
            .pitch (nextDouble (tok))
            .roll (nextDouble (tok))
            .vfov (nextDouble (tok))
            .imageFile (tok.nextToken ());
    }
    
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
    }
}