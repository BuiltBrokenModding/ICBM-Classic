package com.builtbroken.jlib.data.vector;

/**
 * Most basic version of Pos that only contains the data. Useful for
 * just passing data into and out of storage.
 *
 * The interface IPos3D is not implemented to avoid causing automatic casting issues
 * for classes that extend this object. This allows for the object to be extended to
 * create a higher level version of the location data. That may not want to be casted
 * to the interface
 *
 * Created by robert on 1/11/2015.
 */
public class Pos3DBean extends Pos2DBean
{
    private final double z;

    public Pos3DBean(double x, double y, double z)
    {
        super(x, y);
        this.z = z;
    }

    public double z()
    {
        return z;
    }

    public double zf()
    {
        return (float) z;
    }

    public double zi()
    {
        return (int) z;
    }

    @Override
    public Pos3DBean clone()
    {
        return new Pos3DBean(x(), y(), z());
    }

    @Override
    public int hashCode()
    {
        long x = Double.doubleToLongBits(this.x());
        long y = Double.doubleToLongBits(this.y());
        long z = Double.doubleToLongBits(this.z());
        long hash = (x ^ (x >>> 32));
        hash = 31 * hash + y ^ (y >>> 32);
        hash = 31 * hash + z ^ (z >>> 32);
        return (int)hash;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof IPos3D)
        {
            return ((IPos3D)o).x() == x() && ((IPos3D)o).y() == y() && ((IPos3D)o).z() == z();
        }
        return false;
    }

    public int compare(IPos3D that)
    {
        if (x() < that.x() || y() < that.y() || z < that.z())
            return -1;

        if (x() > that.x() || y() > that.y() || z > that.z())
            return 1;

        return 0;
    }
}
