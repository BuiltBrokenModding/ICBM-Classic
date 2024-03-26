package icbm.classic.api.caps;

import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used in capabilities to provide an explosive for usage
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public interface IExplosive {

    /**
     * Gets the explosive provided
     *
     * @return
     */
    @Nullable
    ExplosiveType getExplosiveData();

    /**
     * Gets the custom NBT data to encode into
     * any blast created from the explosive
     *
     * @return nbt
     */
    @Nonnull
    CompoundTag getCustomBlastData(); //TODO replace with interface instead of using NBT to directly apply changes

    /**
     * Gets the stack version of the explosive
     *
     * @return stack, or null if this has no stack
     */
    @Nullable
    ItemStack toStack();

    /**
     * Called when the explosive is defused
     */
    default void onDefuse() //TODO add args on who defused and how
    {

    }
}
