package icbm.classic.content.explosive.ex;

import icbm.classic.content.explosive.blast.BlastRedmatter;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExRedMatter extends Explosion
{
    public ExRedMatter()
    {
        super("redMatter", EnumTier.FOUR);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastRedmatter(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, BlastRedmatter.NORMAL_RADIUS).explode();
    }
}
