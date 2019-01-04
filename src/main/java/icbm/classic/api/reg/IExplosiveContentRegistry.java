package icbm.classic.api.reg;

import net.minecraft.util.ResourceLocation;

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
}
