package icbm.classic.lib.transform.region;

import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.lib.transform.vector.Point;
import net.minecraft.util.math.Vec3d;

/**
 * Created by robert on 1/12/2015.
 */
public abstract class Shape2D
{
    /** Rotation around the Y */
    double yaw = 0;
    Point center;

    public Shape2D(Point center)
    {
        this.center = center;
    }

    /** Distance the shape takes in the X axis */
    abstract double getSizeX();

    /** Distance the shape takes in the Y axis */
    abstract double getSizeY();

    /** Gets the area of the shape */
    abstract double getArea();

    //====================
    // Collision check methods
    //====================

    public abstract boolean isWithin(IPos2D p);

    /** Checks if the point is inside the shape */
    public boolean isWithin(double x, double y)
    {
        return isWithin(new Point(x, y));
    }

    /** Checks if the point is inside the shape */
    boolean isWithin(double x, double y, double z)
    {
        return isWithin(x, z);
    }

    /** Checks if the point is inside the shape */
    boolean isWithin(IPos3D vec)
    {
        return isWithin(vec.x(), vec.y(), vec.z());
    }

    /** Checks if the point is inside the shape */
    boolean isWithin(Vec3d vec)
    {
        return isWithin(vec.x, vec.y, vec.z);
    }


}
