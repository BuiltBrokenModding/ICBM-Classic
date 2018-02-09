package com.builtbroken.jlib.data.vector;

/**
 * Useful interface to define that an object has a 3D location.
 *
 * @author DarkGuardsman
 */
public interface IPos3D extends IPos2D
{
    double z();

    default float zf()
    {
        return (float) z();
    }

    default int zi()
    {
        return (int) Math.floor(z());
    }
}
