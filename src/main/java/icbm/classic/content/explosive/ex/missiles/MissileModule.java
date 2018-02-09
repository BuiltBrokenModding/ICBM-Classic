package icbm.classic.content.explosive.ex.missiles;

import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import resonant.api.explosion.IMissile;

public class MissileModule extends Missile
{
    public MissileModule()
    {
        super("missileModule", EnumTier.ONE);
        this.hasBlock = false;
        //this.missileModelPath = "missiles/tier1/missile_head_conventional.obj";
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        if (entity instanceof IMissile)
        {
            ((IMissile) entity).dropMissileAsItem();
        }
    }
}
