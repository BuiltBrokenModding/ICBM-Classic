package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import icbm.classic.content.explosive.blast.BlastChemical;
import icbm.classic.prefab.EnumTier;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExChemical extends Explosion
{
    public ExChemical(String mingZi, EnumTier tier)
    {
        super(mingZi, tier);
        //chemical
        if (this.getTier() == EnumTier.ONE)
        {
            //this.missileModelPath = "missiles/tier1/missile_head_chemical.obj";
        }//contagious
        else if (this.getTier() == EnumTier.TWO)
        {
            //this.missileModelPath = "missiles/tier2/missile_head_contagious.obj";
        }
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        if (this.getTier() == EnumTier.ONE)
        {
            new BlastChemical(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 20, 20 * 30, false).setPoison().setRGB(0.8f, 0.8f, 0).explode();
        }
        else if (this.getTier() == EnumTier.TWO)
        {
            new BlastChemical(world, entity, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, 20, 20 * 30, false).setContagious().setRGB(0.3f, 0.8f, 0).explode();
        }

    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
