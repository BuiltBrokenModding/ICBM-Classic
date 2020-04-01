package icbm.classic.lib.transform.region;

import com.builtbroken.jlib.data.vector.IPos2D;
import icbm.classic.lib.transform.vector.Point;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 2/9/2018.
 */
public class Rectangle extends Shape2D
{
    Point min;
    Point max;

    public Rectangle(Point min, Point max)
    {
        super(min.midpoint(max));
        this.min = min;
        this.max = max;
    }

    public Rectangle()
    {
        this(new Point(), new Point());
    }

    public Rectangle(Point vec, double expansion)
    {
        this(vec, vec.add(expansion));
    }

    public Rectangle(double minX, double minY, double maxX, double maxY)
    {
        this(new Point(minX, minY), new Point(maxX, maxY));
    }

    public Rectangle(Rectangle rect)
    {
        this(rect.min.clone(), rect.max.clone());
    }

    /** Checks if the point is inside the shape */
    public boolean isWithin(IPos2D p)
    {
        return p.y() >= this.min.y() && p.y() <= this.max.y() && p.x() >= this.min.x() && p.x() <= this.max.x();
    }

    public boolean isWithin_rotated(IPos2D p)
    {
        //Rect corners
        final Point cornerA = this.cornerA();
        final Point cornerB = this.cornerB();
        final Point cornerC = this.cornerC();
        final Point cornerD = this.cornerD();

        //Area of the triangles made from the corners and p
        double areaAB = new Triangle(cornerA, cornerB, p).getArea();
        double areaBC = new Triangle(cornerB, cornerC, p).getArea();
        double areaCD = new Triangle(cornerC, cornerD, p).getArea();
        double areaDA = new Triangle(cornerD, cornerA, p).getArea();

        //If the area of the combined points is less and equals to area
        return (areaAB + areaBC + areaCD + areaDA) <= getArea();
    }

    public Point cornerA()
    {
        return min;
    }

    public Point cornerB()
    {
        return new Point(min.x(), max.y());
    }

    public Point cornerC()
    {
        return max;
    }

    public Point cornerD()
    {
        return new Point(max.x(), min.y());
    }

    /**
     * Returns whether the given region intersects with this one.
     */
    public boolean intersects(Rectangle region)
    {
        if (region.max.x() > this.min.x() && region.min.x() < this.max.x())
        {
            if (region.max.y() > this.min.y() && region.min.y() < this.max.y())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public double getArea()
    {
        return getSizeX() * getSizeY();
    }

    @Override
    public double getSizeX()
    {
        return max.x() - min.x();
    }

    @Override
    public double getSizeY()
    {
        return max.y() - min.y();
    }

    @Override
    public String toString()
    {
        final int precision = 4;
        return "Rectangle[" + BigDecimal.valueOf(min.x()).setScale(precision, RoundingMode.HALF_UP) + ", " + BigDecimal.valueOf(min.y()).setScale(precision, RoundingMode.HALF_UP) + "] -> [" + BigDecimal.valueOf(max.x()).setScale(precision, RoundingMode.HALF_UP) + ", " + BigDecimal.valueOf(max.y()).setScale(precision, RoundingMode.HALF_UP) + "]";
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Rectangle)
        {
            return (min == ((Rectangle) o).min) && (max == ((Rectangle) o).max);
        }
        return false;
    }

    public Rectangle clone()
    {
        return new Rectangle(this);
    }
}
