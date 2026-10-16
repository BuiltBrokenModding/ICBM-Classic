package icbm.classic.lib.capability.ex;

import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Used by any item that has an explosive capability
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class CapabilityExplosiveStatic implements IExplosive, INBTSerializable<CompoundTag>
{
    private final IExplosiveData data;
    private final Supplier<ItemStack> itemStackSupplier;
    private CompoundTag custom_ex_data;

    public CapabilityExplosiveStatic(IExplosiveData data, Supplier<ItemStack> itemStackSupplier) {
        this.data = data;
        this.itemStackSupplier = itemStackSupplier;
    }

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        return data;
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
        return itemStackSupplier.get();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider)
    {
        //TODO save customizations
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
    {

    }
}
// PORTED
