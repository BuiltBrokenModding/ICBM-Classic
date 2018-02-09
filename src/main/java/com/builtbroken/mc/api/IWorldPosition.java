package com.builtbroken.mc.api;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.world.World;

/**
 * Useful interface to define that an object has a 3D location, and a defined world.
 *
 * @author DarkGuardsman
 */
public interface IWorldPosition extends IPos3D
{
    World world();

    /**
     * Converts the object to a location object.
     *
     * @return location object
     */
    default Location toLocation()
    {
        return this instanceof Location ? (Location) this : new Location(this);
    }

    /**
     * Converts the object to a position object.
     *
     * @return position object
     */
    default Pos toPos()
    {
        return new Pos(x(), y(), z());
    }

    default boolean isClient()
    {
        return world() != null && world().isRemote;
    }

    default boolean isServer()
    {
        return world() != null && !world().isRemote;
    }
}
