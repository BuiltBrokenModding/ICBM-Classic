package com.builtbroken.mc.framework.radar.data;


import com.builtbroken.mc.imp.transform.vector.Pos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/5/2016.
 */
public class RadarTile extends RadarObject<TileEntity>
{
    public TileEntity tile;

    public RadarTile(TileEntity referent)
    {
        this.tile = referent;
    }

    @Override
    public boolean isValid()
    {
        return tile != null && tile.hasWorld() && !tile.isInvalid();
    }

    @Override
    public World world()
    {
        return tile != null ? tile.getWorld() : null;
    }

    @Override
    public double x()
    {
        return tile != null ? tile.getPos().getX() + 0.5 : 0;
    }

    @Override
    public double y()
    {
        return tile != null ? tile.getPos().getY() + 0.5 : 0;
    }

    @Override
    public double z()
    {
        return tile != null ? tile.getPos().getZ() + 0.5 : 0;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof RadarTile)
        {
            //No need to compare world as radar contacts should only be stored per world
            return ((RadarTile) object).tile == tile || new Pos(((RadarTile) object).tile).equals(new Pos(tile));
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        if (tile != null)
        {
            return tile.hashCode();
        }
        return super.hashCode();
    }
}
