package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import icbm.classic.content.explosive.blast.BlastNuclear;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExNuclear extends Explosion
{
    public ExNuclear(String mingZi, BlockICBM.EnumTier tier)
    {
        super(mingZi, tier);
        if (this.getTier() == BlockICBM.EnumTier.THREE)
        {
            //this.missileModelPath = "missiles/tier3/missile_head_nuclear.obj";
        }
        else
        {
            //this.missileModelPath = "missiles/tier3/missile_head_conflag.obj";
        }
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        if (this.getTier() == BlockICBM.EnumTier.THREE)
        {
            new BlastNuclear(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 50, 80).setNuclear().explode();
        }
        else
        {
            new BlastNuclear(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 30, 45).explode();
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
