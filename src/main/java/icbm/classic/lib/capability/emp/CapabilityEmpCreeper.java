package icbm.classic.lib.capability.emp;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.config.ConfigEMP;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.effect.EntityLightningBolt;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public class CapabilityEmpCreeper implements IEMPReceiver, ICapabilityProvider {
    public final EntityCreeper creeper;

    public CapabilityEmpCreeper(EntityCreeper creeper) {
        this.creeper = creeper;
    }

    @Override
    public float applyEmpAction(Level level, double x, double y, double z, IBlast emp_blast, float power, boolean doAction) {
        if (ConfigEMP.ALLOW_LIGHTING_CREEPER) {
            //Attack creeper with lighting TODO replace with data manager call
            creeper.onStruckByLightning(new EntityLightningBolt(world, creeper.getX(), creeper.getY(), creeper.getZ(), true));
        }
        return power;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capability == CapabilityEMP.EMP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return capability == CapabilityEMP.EMP ? (T) this : null;
    }

}
