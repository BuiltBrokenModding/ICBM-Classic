package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.blast.BlastEndothermic;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExEndothermic extends Explosion
{
    public ExEndothermic()
    {
        super("endothermic", EnumTier.THREE);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastEndothermic(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 50).explode();
    }
}
