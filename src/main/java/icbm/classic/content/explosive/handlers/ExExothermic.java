package icbm.classic.content.explosive.handlers;

import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.content.explosive.blast.BlastExothermic;
import icbm.classic.api.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExExothermic extends Explosion
{
    public ExExothermic()
    {
        super("exothermic", EnumTier.THREE);
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
        new BlastExothermic(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 50 * scale).runBlast();
    }
}
