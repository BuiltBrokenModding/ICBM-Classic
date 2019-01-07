package icbm.classic.content.blast.handlers;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import icbm.classic.api.explosion.IMissile;

public class MissileModule //extends Missile
{
    public MissileModule()
    {
        //super("missileModule", EnumTier.ONE);
        //this.hasBlock = false;
    }

    //@Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        if (entity instanceof IMissile)
        {
            ((IMissile) entity).dropMissileAsItem();
        }
    }
}
