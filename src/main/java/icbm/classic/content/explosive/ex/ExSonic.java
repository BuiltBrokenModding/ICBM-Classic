package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import icbm.classic.content.explosive.blast.BlastSonic;
import icbm.classic.prefab.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExSonic extends Explosion
{
    public ExSonic(String mingZi, EnumTier tier)
    {
        super(mingZi, tier);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        if (this.getTier() == EnumTier.THREE)
        {
            new BlastSonic(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 20, 35).setShockWave().explode();
        }
        else
        {
            new BlastSonic(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 15, 30).explode();
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
