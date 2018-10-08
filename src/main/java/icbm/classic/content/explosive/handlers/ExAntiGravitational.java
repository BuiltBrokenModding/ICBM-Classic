package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.blast.BlastAntiGravitational;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExAntiGravitational extends Explosion
{
    public ExAntiGravitational()
    {
        super("antiGravitational", EnumTier.THREE);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        new BlastAntiGravitational(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 30 * scale).runBlast();
    }
}
