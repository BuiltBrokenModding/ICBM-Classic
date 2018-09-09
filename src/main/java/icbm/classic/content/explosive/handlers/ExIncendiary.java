package icbm.classic.content.explosive.handlers;

import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.content.explosive.blast.BlastFire;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExIncendiary extends Explosion
{
    public ExIncendiary(String mingZi, EnumTier tier)
    {
        super(mingZi, tier);
        //this.missileModelPath = "missiles/tier1/missile_head_incen.obj";
    }

    @Override
    public void onFuseTick(World worldObj, Pos position, int fuseTicks)
    {
        super.onFuseTick(worldObj, position, fuseTicks);
        worldObj.spawnParticle(EnumParticleTypes.LAVA, position.x(), position.y() + 0.5D, position.z(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        new BlastFire(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 14 * scale).explode();
    }
}
