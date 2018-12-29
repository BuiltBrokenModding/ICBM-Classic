package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.blast.BlastXmas;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExXMAS extends Explosion
{
    public ExXMAS()
    {
        super("xmas", EnumTier.TWO);
        this.setFuseTime(200);
        this.hasGrenade = true;
    }

    @Override
    public void onFuseTick(World worldObj, Pos position, int fuseTicks)
    {

    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        new BlastXmas(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 5 * scale).runBlast();
    }
}
