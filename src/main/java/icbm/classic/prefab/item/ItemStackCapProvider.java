package icbm.classic.prefab.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper to group several capabilities of an ItemStack
 * <p>
 * PORT: 1.21 item capabilities are created per lookup ({@link icbm.classic.lib.capability.holder.ICapabilityHolderItem})
 * and are never saved by the game, so this no longer serializes anything. Capabilities with state must read and
 * write the stack themselves. Kept as a small lookup helper.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 3/21/2018.
 */
public class ItemStackCapProvider
{
    public final ItemStack host;
    public final Map<ItemCapability<?, Void>, Object> capTypeToCap = new HashMap<>();
    public final Map<String, Object> keyToCap = new HashMap<>();

    public ItemStackCapProvider(ItemStack host)
    {
        this.host = host;
    }

    public <T> ItemStackCapProvider add(String key, ItemCapability<T, Void> capability, T cap)
    {
        capTypeToCap.put(capability, cap);
        keyToCap.put(key, cap);
        return this;
    }

    public boolean hasCapability(ItemCapability<?, Void> capability)
    {
        return capTypeToCap.containsKey(capability);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(ItemCapability<T, Void> capability)
    {
        return (T) capTypeToCap.get(capability);
    }
}
// PORTED
