package icbm.classic.lib.capability.emp;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.config.ConfigEMP;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpCreeper implements IEMPReceiver, ICapabilityProvider
{
    public final EntityCreeper creeper;

    public CapabilityEmpCreeper(EntityCreeper creeper)
    {
        this.creeper = creeper;
    }

    @Override
    public float applyEmpAction(World world, double x, double y, double z, IBlast emp_blast, float power, boolean doAction)
    {
        if (ConfigEMP.ALLOW_LIGHTING_CREEPER)
        {
            //Attack creeper with lighting TODO replace with data manager call
            creeper.onStruckByLightning(new EntityLightningBolt(world, creeper.posX, creeper.posY, creeper.posZ, true));
        }
        return power;
    }
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP ? (T) this : null;
    }

}
