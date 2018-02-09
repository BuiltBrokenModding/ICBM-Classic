package com.builtbroken.jlib.data.vector;

/**
 * Useful interface to define that an object has a 2D location.
 *
 * @author DarkGuardsman
 */
public interface IPos2D
{
    double x();

    double y();

    default int xi()
    {
        return (int) Math.floor(x());
    }

    default int yi()
    {
        return (int) Math.floor(y());
    }

    default float xf()
    {
        return (float) x();
    }

    default float yf()
    {
        return (float) y();
    }
}
