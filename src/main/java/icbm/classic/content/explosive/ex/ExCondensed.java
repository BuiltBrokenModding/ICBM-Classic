package icbm.classic.content.explosive.ex;

import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExCondensed extends Explosion
{
    public ExCondensed(String mingZi, EnumTier tier)
    {
        super(mingZi, tier);
        this.setFuseTime(1);
        //this.missileModelPath = "missiles/tier1/missile_head_concussion.obj";
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        new BlastTNT(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 6).explode();
    }
}
