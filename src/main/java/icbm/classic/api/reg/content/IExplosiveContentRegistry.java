package icbm.classic.api.reg.content;

import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Set;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExplosiveContentRegistry
{

    /**
     * Set of all ids enabled for the content
     *
     * @param contentID - content ID
     * @return set of enabled explosives
     */
    Set<Integer> getExplosivesEnabledForContent(ResourceLocation contentID);

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
