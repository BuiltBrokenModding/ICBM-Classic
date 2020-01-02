package icbm.classic.prefab.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper to store, save, and load capabilities on an ItemStack
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class ItemStackCapProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound>
{
    public final ItemStack host;
    public HashMap<Capability, Object> capTypeToCap = new HashMap();
    public HashMap<String, Object> keyToCap = new HashMap();

    public ItemStackCapProvider(ItemStack host)
    {
        this.host = host;
    }

    public <T> void add(String key, Capability<T> capability, T cap)
    {
        capTypeToCap.put(capability, cap);
        keyToCap.put(key, cap);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capTypeToCap.containsKey(capability);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capTypeToCap.containsKey(capability))
        {
            return (T) capTypeToCap.get(capability);
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        for (Map.Entry<String, Object> entry : keyToCap.entrySet())
        {
            if (entry.getValue() instanceof INBTSerializable)
            {
                tag.setTag(entry.getKey(), ((INBTSerializable) entry.getValue()).serializeNBT());
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        for (Map.Entry<String, Object> entry : keyToCap.entrySet())
        {
            if (entry.getValue() instanceof INBTSerializable)
            {
                ((INBTSerializable) entry.getValue()).deserializeNBT(nbt.getTag(entry.getKey()));
            }
        }
    }
}
