package icbm.classic.content.missile.source;

import icbm.classic.api.missiles.IMissileSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MissileSourceEntity implements IMissileSource
{
    //TODO implement

    @Override
    public World getWorld()
    {
        return null;
    }

    @Override
    public Entity getFiringEntity()
    {
        return null;
    }

    @Override
    public MissileSourceType getType()
    {
        return null;
    }

    @Override
    public Vec3d getFiredPosition()
    {
        return null;
    }

    @Override
    public BlockPos getBlockPos()
    {
        return null;
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return null;
    }
}
