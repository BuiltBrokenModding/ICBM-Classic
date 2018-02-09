package icbm.classic.content.explosive.ex;

import icbm.classic.content.explosive.blast.BlastEMP;
import icbm.classic.prefab.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExEMP extends Explosion
{
    public ExEMP()
    {
        super("emp", EnumTier.THREE);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastEMP(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 50).setEffectBlocks().setEffectEntities().explode();
    }
}
