package com.builtbroken.mc.imp.transform.region;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

/**
 * Created by robert on 1/12/2015.
 */
public abstract class Shape3D
{
    //TODO replace with rotation object
    double yaw = 0;
    double pitch = 0;
    double roll = 0;

    Pos center;

    public Shape3D(Pos center)
    {
        this.center = center;
    }

    public Shape3D(NBTTagCompound nbt)
    {
        this(new Pos(nbt.getCompoundTag("center")));
        yaw = nbt.getDouble("yaw");
        pitch = nbt.getDouble("pitch");
        roll = nbt.getDouble("roll");
    }

    /**
     * Is the vector(x, y, z) inside the shape
     */
    abstract boolean isWithin(double x, double y, double z);

    /**
     * Volume of the 3D shape
     */
    abstract double getVolume();

    /**
     * Surface area of the shape
     */
    abstract double getArea();

    /**
     * Distance the shape takes in the Xaxis
     */
    abstract double getSizeX();

    /**
     * Distance the shape takes in the Y axis
     */
    abstract double getSizeY();

    /**
     * Distance the shape takes in the Z axis
     */
    abstract double getSizeZ();

    /**
     * gets the max distance a corner of the shape will reach from the center
     */
    double getSize()
    {
        double r = getSizeX();
        if (getSizeY() > r)
            r = getSizeY();
        if (getSizeZ() > r)
            r = getSizeZ();
        return r;
    }

    /**
     * Center of the 3D shape
     */
    public IPos3D getCenter()
    {
        return center;
    }

    public double distance(IPos3D pos)
    {
        return center.distance(pos);
    }

    /**
     * Is the vector(x, y, z) inside the shape
     */
    public boolean isWithin(IPos3D vec)
    {
        return isWithin(vec.x(), vec.y(), vec.z());
    }

    public boolean isWithin(BlockPos vec)
    {
        return isWithin(vec.getX(), vec.getY(), vec.getZ());
    }
}
