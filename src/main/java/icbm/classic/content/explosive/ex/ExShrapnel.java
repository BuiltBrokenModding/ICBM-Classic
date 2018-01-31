package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastShrapnel;
import icbm.classic.prefab.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExShrapnel extends Explosion
{
    public ExShrapnel(String name, EnumTier tier)
    {
        super(name, tier);
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        if (this.getTier() == EnumTier.TWO)
        {
            new BlastShrapnel(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 15, true, true, false).explode();
        }
        else if (this == Explosives.ANVIL.handler)
        {
            new BlastShrapnel(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 25, false, false, true).explode();
        }
        else
        {
            new BlastShrapnel(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 30, true, false, false).explode();
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
