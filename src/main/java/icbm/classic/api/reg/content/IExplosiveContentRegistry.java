package icbm.classic.api.reg.content;

import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Set;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExplosiveContentRegistry
{

    /**
     * Set of all ids enabled for the content
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
     * Gets a list of all explosives by name
     * @return
     */
    List<ResourceLocation> getExplosiveNames();

    /**
     * Gets a list of all explosives
     * @return
     */
    List<IExplosiveData> getExplosives();

    /**
     * Called at the end of registry phase to
     * lock the registry, convert lists to immutable,
     * and prevent late registration of content.
     */
    void lockRegistry();
}
