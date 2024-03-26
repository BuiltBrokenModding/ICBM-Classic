package icbm.classic.prefab.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper to store, save, and load capabilities on an ItemStack
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class ItemStackCapProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public final ItemStack host;
    public HashMap<Capability, Object> capTypeToCap = new HashMap();
    public HashMap<String, Object> keyToCap = new HashMap();

    public ItemStackCapProvider(ItemStack host) {
        this.host = host;
    }

    public <T> void add(String key, Capability<T> capability, T cap) {
        capTypeToCap.put(capability, cap);
        keyToCap.put(key, cap);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable Direction facing) {
        return capTypeToCap.containsKey(capability);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capTypeToCap.containsKey(capability)) {
            return (T) capTypeToCap.get(capability);
        }
        return null;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Object> entry : keyToCap.entrySet()) {
            if (entry.getValue() instanceof INBTSerializable) {
                tag.put(entry.getKey(), ((INBTSerializable) entry.getValue()).serializeNBT());
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (Map.Entry<String, Object> entry : keyToCap.entrySet()) {
            if (entry.getValue() instanceof INBTSerializable) {
                ((INBTSerializable) entry.getValue()).deserializeNBT(nbt.getTag(entry.getKey()));
            }
        }
    }
}
