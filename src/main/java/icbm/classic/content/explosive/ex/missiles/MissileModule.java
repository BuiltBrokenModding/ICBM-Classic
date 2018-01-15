package icbm.classic.content.explosive.ex.missiles;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import resonant.api.explosion.IMissile;

public class MissileModule extends Missile
{
    public MissileModule()
    {
        super("missileModule", BlockICBM.EnumTier.ONE);
        this.hasBlock = false;
        //this.missileModelPath = "missiles/tier1/missile_head_conventional.obj";
    }

    @Override
    public void doCreateExplosion(World world, BlockPos pos, Entity entity)
    {
        if (entity instanceof IMissile)
        {
            ((IMissile) entity).dropMissileAsItem();
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
