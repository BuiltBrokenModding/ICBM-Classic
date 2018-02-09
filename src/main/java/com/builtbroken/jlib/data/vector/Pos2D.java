package com.builtbroken.jlib.data.vector;

/** Basic implementation of a 2D position with math operators
 *
 * Created by robert on 1/11/2015.
 */
public abstract class Pos2D<R extends Pos2D> extends Pos2DBean
{
    public Pos2D(double x, double y)
    {
        super(x, y);
    }

    public Pos2D()
    {
        this(0, 0);
    }

    public R add(double x, double y)
    {
        return newPos(x + x(), y + y());
    }

    public R add(IPos2D other)
    {
        return add(other.x(), other.y());
    }

    public R add(double a)
    {
        return add(a, a);
    }

    public R sub(double x, double y)
    {
        return add(-x, -y);
    }

    public R sub(IPos2D other)
    {
        return sub(other.x(), other.y());
    }

    public R sub(double a)
    {
        return sub(a, a);
    }


    public R multiply(IPos2D pos)
    {
        return newPos(pos.x() * x(), pos.y() * y());
    }

    public R multiply(double x, double y)
    {
        return newPos(x * x(), y * y());
    }

    public R multiply(double a)
    {
        return multiply(a, a);
    }


    public R divide(IPos2D pos)
    {
        return newPos( x() / pos.x(), y() / pos.y());
    }

    public R divide(double x, double y)
    {
        return newPos(x() / x, y() / y);
    }

    public R divide(double a)
    {
        return divide(a, a);
    }

    public R rotate(double angle)
    {
        return newPos(x() * Math.cos(angle) - y() * Math.sin(angle), x() * Math.sin(angle) + y() * Math.cos(angle));
    }

    public double dotProduct(IPos2D other)
    {
        return x() * other.x() + y() * other.y();
    }

    public double magnitudeSquared()
    {
        return x() * x() + y() * y();
    }

    public double magnitude()
    {
        return Math.sqrt(magnitudeSquared());
    }

    public R normalize() { return this.divide(magnitude()); }

    public double distance(IPos2D other)
    {
        return (this.sub(other)).magnitude();
    }

    public R midpoint(IPos2D other)
    {
        return (R)add(other).divide(2);
    }

    public boolean isZero()
    {
        return x() == 0 && y() == 0;
    }

    /**
     * Gets the slow or ratio of change between
     * two points
     *
     * @param other - point to use
     * @return slope
     */
    public double slope(IPos2D other)
    {
        return (y() - other.y()) / (x() - other.x());
    }

    /**
     * Rounds down
     *
     * @return new Pos
     */
    public R round()
    {
        return newPos(Math.round(x()), Math.round(y()));
    }

    /**
     * Rounds up
     *
     * @return new Pos
     */
    public R ceil()
    {
        return newPos(Math.ceil(x()), Math.ceil(y()));
    }

    /**
     * Rounds down
     *
     * @return new Pos
     */
    public R floor()
    {
        return newPos(Math.floor(x()), Math.floor(y()));
    }

    /**
     * Creates a new point that represents the max between the two points
     *
     * @return new Pos
     */
    public R max(IPos2D other)
    {
        return newPos(Math.max(x(), other.x()), Math.max(y(), other.y()));
    }

    /**
     * Creates a new point that represents the min between the two points
     *
     * @return new Pos
     */
    public R min(IPos2D other)
    {
        return newPos(Math.min(x(), other.x()), Math.min(y(), other.y()));
    }

    /**
     * Gets the reciprocal or the value of 1 over (x, y)
     *
     * @return new Pos
     */
    public R reciprocal()
    {
        return newPos(1 / x(), 1 / y());
    }

    @Override
    public R clone()
    {
        return newPos(this.x(), this.y());
    }

    public abstract R newPos(double x, double y);
}
