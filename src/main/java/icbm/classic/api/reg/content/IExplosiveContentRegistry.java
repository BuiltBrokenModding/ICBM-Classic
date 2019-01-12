package icbm.classic.api.reg.content;

import icbm.classic.api.reg.IExplosiveData;
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
