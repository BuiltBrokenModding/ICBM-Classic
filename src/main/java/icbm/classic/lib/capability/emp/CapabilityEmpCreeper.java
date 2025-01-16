package icbm.classic.lib.capability.emp;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.config.ConfigEMP;
import icbm.classic.config.blast.ConfigBlast;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 */
public class CapabilityEmpCreeper implements IEMPReceiver, ICapabilityProvider
{
    public final CreeperEntity creeper;

    private final LazyOptional<CapabilityEmpCreeper> lazyOptional = LazyOptional.of(() -> this);

    public CapabilityEmpCreeper(CreeperEntity creeper)
    {
        this.creeper = creeper;
    }

    @Override
    public float applyEmpAction(World world, double x, double y, double z, IAction emp_blast, float power, boolean doAction)
    {
        if (ConfigBlast.emp.ALLOW_LIGHTING_CREEPER)
        {
            //Attack creeper with lighting TODO replace with data manager call
            creeper.onStruckByLightning(new LightningBoltEntity(world, creeper.posX, creeper.posY, creeper.posZ, true));
        }
        return power;
    }

    @Nullable
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        return capability == CapabilityEMP.EMP ? lazyOptional.cast() : LazyOptional.empty();
    }

}
