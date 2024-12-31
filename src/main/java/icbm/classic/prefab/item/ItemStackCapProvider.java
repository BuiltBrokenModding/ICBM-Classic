package icbm.classic.prefab.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper to store, save, and load capabilities on an ItemStack
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/21/2018.
 */
public class ItemStackCapProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT>
{
    public final ItemStack host;
    public HashMap<Capability, LazyOptional<Object>> capTypeToCap = new HashMap();

    public ItemStackCapProvider(ItemStack host)
    {
        this.host = host;
    }

    public <T> ItemStackCapProvider with(Capability<T> capability, NonNullSupplier<T> cap)
    {
        capTypeToCap.put(capability, LazyOptional.of(cap).cast());
        return this;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull final Capability<T> capability, final @Nullable Direction side)
    {
        if (capTypeToCap.containsKey(capability))
        {
            return capTypeToCap.get(capability).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();
        for (Map.Entry<Capability, LazyOptional<Object>> entry : capTypeToCap.entrySet())
        {
            entry.getValue().ifPresent((value) -> {
                final INBT nbt = ((INBTSerializable) value).serializeNBT();
                if(!(nbt instanceof CompoundNBT) || !((CompoundNBT) nbt).isEmpty()) {
                    tag.put(entry.getKey().getName(), nbt);
                }
            });
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt)
    {
        for (Map.Entry<Capability, LazyOptional<Object>> entry : capTypeToCap.entrySet())
        {
            entry.getValue().ifPresent((value) -> {
                if(nbt.contains(entry.getKey().getName())) {
                    final INBT save = nbt.get(entry.getKey().getName());
                    if(!(save instanceof CompoundNBT) || !((CompoundNBT) save).isEmpty()) {
                        ((INBTSerializable) entry.getValue()).deserializeNBT(save);
                    }
                }
            });
        }
    }
}
