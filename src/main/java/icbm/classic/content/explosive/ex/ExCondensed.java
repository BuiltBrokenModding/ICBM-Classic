package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import icbm.classic.content.explosive.blast.BlastTNT;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ExCondensed extends Explosion
{
    public ExCondensed(String mingZi, BlockICBM.EnumTier tier)
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

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
