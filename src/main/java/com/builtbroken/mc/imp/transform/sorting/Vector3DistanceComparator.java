package com.builtbroken.mc.imp.transform.sorting;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.imp.transform.vector.Pos;

import java.util.Comparator;

public class Vector3DistanceComparator implements Comparator<IPos3D>
{
    final IPos3D center;
    final boolean closest;

    public Vector3DistanceComparator(IPos3D center)
    {
       this(center, true);
    }

    public Vector3DistanceComparator(IPos3D center, boolean closest)
    {
        this.center = center;
        this.closest = closest;
    }

    @Override
    public int compare(IPos3D o1, IPos3D o2)
    {
        double d = new Pos(o1).distance(center);
        double d2 = new Pos(o2).distance(center);
        return d > d2 ? 1 : d == d2 ? 0 : -1;
    }
}