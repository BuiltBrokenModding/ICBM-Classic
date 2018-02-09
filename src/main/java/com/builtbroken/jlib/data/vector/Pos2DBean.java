package com.builtbroken.jlib.data.vector;

/** Most basic version of Pos that only contains the data. Useful for
 * just passing data into and out of storage.
 *
 *  The interface IPos2D is not implemented to avoid causing automatic casting issues
 * for classes that extend this object. This allows for the object to be extended to
 * create a higher level version of the location data. That may not want to be casted
 * to the interface. For example IPos3D would share the same data as IPos2D but would
 * cause issues if both shared the same interface. Especially with using scala wrappers
 * that use the interface for math operators. As the wrapper would treat IPos3D as the
 * same data as IPos2D.
 *
 * Created by robert on 1/11/2015.
 */
public class Pos2DBean implements Cloneable
{
    private final double x;
    private final double y;

    public Pos2DBean(double x, double y)
    {
        this.x = x;
        this.y = y;
    }


    public double x()
    {
        return x;
    }

    public float xf()
    {
        return (float)x;
    }

    public int xi()
    {
        return (int)Math.floor(x);
    }


    public double y()
    {
        return y;
    }

    public float yf()
    {
        return (float)y;
    }

    public int yi()
    {
        return (int)Math.floor(y);
    }

    @Override
    public Pos2DBean clone()
    {
        return new Pos2DBean(x(), y());
    }

    @Override
    public int hashCode()
    {
        long x = Double.doubleToLongBits(this.x);
        long y = Double.doubleToLongBits(this.y);
        return 31 * (int)(x ^ (x >>> 32)) + (int)(y ^ (y >>> 32));
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof IPos2D)
        {
            return this.x == ((IPos2D)o).x() && this.y == ((IPos2D)o).y();
        }
        return false;
    }

    public int compare(IPos2D pos)
    {
        if (x < pos.y() || y < pos.y())
            return -1;

        if (x > pos.y() || y > pos.y())
            return 1;

        return 0;
    }

    @Override
    public String toString()
    {
        return "Pos2D [" + this.x + "," + this.y + "]";
    }
}
