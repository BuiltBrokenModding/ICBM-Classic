package icbm.classic.lib.capability.ex;

import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityExplosive implements IExplosive
{

    public CapabilityExplosive()
    {
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        return ICBMExplosives.CONDENSED;
    }

    @Override
    public void applyCustomizations(IBlast blast) {

    }

    @Override
    public void addCustomization(IExplosiveCustomization customization) {

    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        return null;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IExplosive.class, new Capability.IStorage<IExplosive>()
        {
            @Nullable
            @Override
            public INBT writeNBT(Capability<IExplosive> capability, IExplosive instance, Direction side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IExplosive> capability, IExplosive instance, Direction side, INBT nbt)
            {
            }
        },
            CapabilityExplosive::new);
    }
}
