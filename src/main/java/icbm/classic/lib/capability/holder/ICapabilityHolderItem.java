package icbm.classic.lib.capability.holder;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by ICBM items to expose capabilities, replaces 1.12 {@code Item#initCapabilities}.
 * <p>
 * Instances are created per lookup and not stored by the game. Any state must be read from and
 * written back to the stack (see {@link icbm.classic.lib.StackNbt}) on each call.
 */
public interface ICapabilityHolderItem
{
    @Nullable
    <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack);
}
