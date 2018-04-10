package icbm.classic.content.explosive.handlers;

import icbm.classic.config.ConfigBlast;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.content.explosive.blast.BlastAntimatter;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExAntimatter extends Explosion
{
    public ExAntimatter()
    {
        super("antimatter", EnumTier.FOUR);
        this.setFuseTime(300);
    }

    /** Called when the explosive is on fuse and going to explode. Called only when the explosive is
     * in it's TNT form.
     *
     * @param fuseTicks - The amount of ticks this explosive is on fuse */
    @Override
    public void onYinZha(World worldObj, Pos position, int fuseTicks)
    {
        super.onYinZha(worldObj, position, fuseTicks);

        if (fuseTicks % 25 == 0)
        {
            //worldObj.playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "alarm", 4F, 1F);
        }
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale)
    {
        new BlastAntimatter(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, ConfigBlast.ANTIMATTER_SIZE * scale, ConfigBlast.ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS).explode();
    }
}
