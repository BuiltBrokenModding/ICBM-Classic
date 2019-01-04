package icbm.classic.content.explosive.handlers.missiles;

import icbm.classic.api.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import icbm.classic.api.explosion.IMissile;

public class MissileModule extends Missile
{
    public MissileModule()
    {
        super("missileModule", EnumTier.ONE);
        this.hasBlock = false;
        //this.missileModelPath = "missiles/tier1/missile_head_conventional.obj";
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        if (entity instanceof IMissile)
        {
            ((IMissile) entity).dropMissileAsItem();
        }
    }
}
