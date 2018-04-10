package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.blast.BlastNuclear;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExNuclear extends Explosion
{
    public ExNuclear(String mingZi, EnumTier tier)
    {
        super(mingZi, tier);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        if (this.getTier() == EnumTier.THREE)
        {
            new BlastNuclear(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 50 * scale, 80 * scale).setNuclear().explode();
        }
        else
        {
            new BlastNuclear(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 30 * scale, 45 * scale).explode();
        }
    }
}
