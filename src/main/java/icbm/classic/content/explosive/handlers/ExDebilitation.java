package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.blast.BlastChemical;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExDebilitation extends Explosion
{
    public ExDebilitation(String mingZi, EnumTier tier)
    {
        super(mingZi, tier);
        //this.missileModelPath = "missiles/tier1/missile_head_dibilitation.obj";
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        new BlastChemical(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 20 * scale, 20 * 30, false).setConfuse().explode();
    }
}
