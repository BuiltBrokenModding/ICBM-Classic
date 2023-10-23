package icbm.classic.api.reg;

import icbm.classic.api.EnumTier;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Stores data about an explosive
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExplosiveData extends Comparable<IExplosiveData>
{
    static IBlastFactory EMPTY = () -> null;

    /**
     * Unique registry name of the explosive
     *
     * @return
     */
    ResourceLocation getRegistryName();

    /**
     * Assigned ID of the explosive. Is
     * saved to config file and automatically
     * assigned for new explosives.
     *
     * @return ID
     */
    int getRegistryID();

    /**
     * Blast factory used to create new blast instances
     *
     * @return
     */
    @Nullable
    IBlastFactory getBlastFactory();

    /**
     * Creates a new blast factory
     *
     * @return
     */
    default IBlastInit create()
    {
        return Optional.ofNullable(getBlastFactory()).orElseGet(() -> EMPTY).create();
    }

    /**
     * Tier of the explosive.
     *
     * @return tier of the explosive
     */
    @Deprecated
    @Nonnull
    default EnumTier getTier() {
        // Only for legacy explosives, moving forward tier is visual only and has no functionality
        return EnumTier.NONE;
    }

    /**
     * Checks if the explosive is enabled. Users
     * can disable explosives in the configs. As
     * well other mods can disable explosives
     * to allow items to still exist but functionality
     * to be switched to a new version.
     *
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Sets the enable status of
     *
     * @param b
     */
    void setEnabled(boolean b);

    /**
     * Called when this explosive is register to a content handler
     *
     * @param contentID - id of the registry
     * @param registry  - the registry itself
     * @return true to allow, false to block
     */
    boolean onEnableContent(ResourceLocation contentID, IExplosiveContentRegistry registry);
}
