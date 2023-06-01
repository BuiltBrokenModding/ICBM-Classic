package icbm.classic.api.caps;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Used in capabilities to provide an explosive for usage
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public interface IExplosive
{

    /**
     * Gets the explosive provided
     *
     * @return explosive data
     */
    @Nullable
    IExplosiveData getExplosiveData();

    /**
     * Called to apply customizations.
     *
     * This is meant to be used in combination with {@link #addCustomization(IExplosiveCustomization)}
     * and any other customizations applyed by this explosive instance. It is recommended to use
     * customization objects to handle save/load.
     *
     * @param blast to customize
     */
    void applyCustomizations(IBlast blast);

    /**
     * Adds a layer of blast customization
     *
     * @param customization to apply during blast spawning
     */
    void addCustomization(IExplosiveCustomization customization);

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
