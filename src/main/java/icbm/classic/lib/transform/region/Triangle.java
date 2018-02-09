package icbm.classic.lib.transform.region;

import com.builtbroken.jlib.data.vector.IPos2D;
import icbm.classic.lib.transform.vector.Point;

/**
 * Created by robert on 1/12/2015.
 */
public class Triangle extends Shape2D
{
    IPos2D a, b, c;

    public Triangle(IPos2D a, IPos2D b, IPos2D c)
    {
        super(null);
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Triangle set(Triangle other)
    {
        this.a = other.a;
        this.b = other.b;
        this.c = other.c;
        return this;
    }

    @Override
    public boolean isWithin(double x, double y)
    {
        return isWithin(new Point(x, y));
    }


    public boolean isWithin(IPos2D p)
    {
        double ab = new Triangle(a, b, p).getArea();
        double bc = new Triangle(b, c, p).getArea();
        double ca = new Triangle(c, a, p).getArea();
        return (ab + bc + ca) <= getArea();
    }


    @Override
    public double getArea()
    {
        return Math.abs(a.x() * (b.y() - c.y()) + b.x() * (c.y() - a.y()) + c.x() * (a.y() - b.y())) / 2;
    }

    @Override
    public double getSizeX()
    {
        double lower = a.x();
        double upper = a.x();

        if (b.x() < lower)
            lower = b.x();
        if (c.x() < lower)
            lower = c.x();

        if (b.x() > upper)
            upper = b.x();
        if (c.x() > upper)
            upper = c.x();

        return upper - lower;
    }

    @Override
    public double getSizeY()
    {
        double lower = a.y();
        double upper = a.y();

        if (b.y() < lower)
            lower = b.y();
        if (c.y() < lower)
            lower = c.y();

        if (b.y() > upper)
            upper = b.y();
        if (c.y() > upper)
            upper = c.y();

        return upper - lower;
    }
}
