package icbm.classic.content.explosive.handlers;

import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExRepulsive extends Explosion
{
    public ExRepulsive(String name, EnumTier tier)
    {
        super(name, tier);
        this.setFuseTime(120);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        //TODO recode
        if (this == Explosives.ATTRACTIVE.handler)
        {
            new BlastTNT(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 2f * scale).setDestroyItems().setPushType(1).runBlast();
        }
        else
        {
            new BlastTNT(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 2f * scale).setDestroyItems().setPushType(2).runBlast();
        }
    }
}
