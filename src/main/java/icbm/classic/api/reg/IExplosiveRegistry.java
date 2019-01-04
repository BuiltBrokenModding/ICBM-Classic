package icbm.classic.api.reg;

import icbm.classic.api.explosion.IBlastFactory;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExplosiveRegistry
{

    /**
     * Registers a new blast factory and explosion data instance
     *
     * @param name         - key to register with
     * @param blastFactory - factory to use to make the blast
     * @return data created
     */
    IExplosiveData register(ResourceLocation name, IBlastFactory blastFactory);

    /**
     * Gets the explosive data for the registry name
     *
     * @param name - registry name
     * @return explosive data if registered
     */
    IExplosiveData getExplosiveData(ResourceLocation name);

    /**
     * Gets the explosive data for the registry name
     *
     * @param id - id of the explosive
     * @return explosive data if registered
     */
    IExplosiveData getExplosiveData(int id);

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
     * @param contentID   - ID of the content, see {@link icbm.classic.api.ICBMClassicAPI} for built in types
     * @param explosiveID - registry name of the explosion
     */
    void enableContent(ResourceLocation contentID, ResourceLocation explosiveID);
}
