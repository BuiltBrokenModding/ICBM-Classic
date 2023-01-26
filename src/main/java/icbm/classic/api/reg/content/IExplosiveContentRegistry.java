package icbm.classic.api.reg.content;

import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExplosiveContentRegistry
{
    /**
     * Unique name of the content registry
     *
     * @return
     */
    ResourceLocation getRegistryName();

    /**
     * Set of all ids enabled for the content
     * <p>
     * Set should be immutable after {@link #lockRegistry()} is called
     *
     * @return set of enabled explosives
     */
    Set<Integer> getExplosivesIDs();

    /**
     * Enables content type for the explosion
     *
     * @param explosiveID - registry name of the explosion
     */
    void enableContent(ResourceLocation explosiveID);

    /**
     * Gets a list of all explosives by name.
     * <p>
     * List should be immutable after {@link #lockRegistry()} is called
     *
     * @return
     */
    Set<ResourceLocation> getExplosiveNames();

    /**
     * Gets a list of all explosives
     * <p>
     * List should be immutable after {@link #lockRegistry()} is called
     *
     * @return
     */
    Set<IExplosiveData> getExplosives();

    /**
     * Gets a supported explosive by name
     *
     * @param regName - registry name of the explosive
     * @return
     */
    IExplosiveData getExplosive(ResourceLocation regName);

    /**
     * Creates a new explosive device represented by
     * this registry.
     * <p>
     * Ex: Missile stack
     * <p>
     * Note each registry should only have 1 item mapped to
     * it as a time. If another item is desired then a
     * registry should be created to mirror the previous
     * registry.
     *
     * @param data for explosive instance
     * @return new device stack
     */
    default ItemStack getDeviceStack(IExplosiveData data) {
        return getDeviceStack(data.getRegistryName());
    }

    /**
     * Creates a new explosive device represented by
     * this registry.
     * <p>
     * Ex: Missile stack
     * <p>
     * Note each registry should only have 1 item mapped to
     * it as a time. If another item is desired then a
     * registry should be created to mirror the previous
     * registry.
     *
     * @param regName - registry name of the explosive
     * @return new device stack
     */
    ItemStack getDeviceStack(ResourceLocation regName);

    /**
     * Called at the end of registry phase to
     * lock the registry, convert lists to immutable,
     * and prevent late registration of content.
     */
    void lockRegistry();

    /**
     * Is the content enabled for the explosive
     *
     * @param explosiveData - explosive data
     * @return true if enabled, can also be false if the registry is not setup yet
     */
    default boolean isEnabled(IExplosiveData explosiveData)
    {
        return getExplosivesIDs() != null && getExplosivesIDs().contains(explosiveData.getRegistryID());
    }

    /**
     * Is the content enabled for the explosive
     *
     * @param exName - explosive data name
     * @return true if enabled, can also be false if the registry is not setup yet
     */
    default boolean isEnabled(ResourceLocation exName)
    {
        return getExplosiveNames() != null && getExplosiveNames().contains(exName);
    }

    /**
     * Is the content enabled for the explosive
     *
     * @param exID - explosive data ID
     * @return true if enabled, can also be false if the registry is not setup yet
     */
    default boolean isEnabled(int exID)
    {
        return getExplosivesIDs() != null && getExplosivesIDs().contains(exID);
    }

}
