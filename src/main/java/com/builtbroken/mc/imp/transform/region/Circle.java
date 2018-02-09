package com.builtbroken.mc.imp.transform.region;

import com.builtbroken.jlib.data.vector.IPos2D;
import com.builtbroken.mc.imp.transform.vector.Point;

/**
 * Created by robert on 1/12/2015.
 */
public class Circle extends Shape2D
{
    public double r;

    public Circle(Point center, double radius)
    {
        super(center);
        this.r = radius;
    }

    public Circle(Point center)
    {
        this(center, 1);
    }

    public double getArea()
    {
        return Math.PI * (r * r);
    }

    @Override
    public boolean isWithin(IPos2D p)
    {
        return center.distance(p) <= r;
    }

    public Circle set(Circle other)
    {
        this.center = other.center;
        this.r = other.r;
        return this;
    }

    @Override
    public double getSizeX() { return r; }

    @Override
    public double getSizeY() { return r; }
}
