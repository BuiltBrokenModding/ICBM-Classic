package icbm.classic.lib.capability.holder;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.EntityCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by ICBM entities to expose capabilities the 1.12 way. {@link icbm.classic.content.reg.CapabilityReg}
 * registers a provider for every ICBM entity type that forwards to this method.
 * Named getEntityCapability because NeoForge makes {@code Entity#getCapability} final.
 */
public interface ICapabilityHolderEntity
{
    @Nullable
    <T> T getEntityCapability(EntityCapability<T, Void> capability);

    /** Optional sided capabilities, such as energy or inventory, null context is allowed */
    @Nullable
    default <T> T getSidedCapability(EntityCapability<T, Direction> capability, @Nullable Direction side)
    {
        return null;
    }
}
