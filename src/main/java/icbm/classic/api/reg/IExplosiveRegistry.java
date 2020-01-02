package icbm.classic.api.reg;

import icbm.classic.api.EnumTier;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.Set;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public interface IExplosiveRegistry
{

    /**
     * Registers a new blast factory and explosion data instance
     *
     * @param name         - key to register with
     * @param tier         - optional, defines the tier of the explosive
     * @param blastFactory - factory to use to make the blast
     * @return data created
     */
    IExplosiveData register(ResourceLocation name, EnumTier tier, IBlastFactory blastFactory);

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
     * All content registries that use the explosive registry.
     *
     * @return
     */
    Collection<IExplosiveContentRegistry> getContentRegistries();

    /**
     * Gets the content registry for the ID
     *
     * @param contentID
     * @return
     */
    IExplosiveContentRegistry getContentRegistry(ResourceLocation contentID);


    /**
     * Registers a new content type for explosives to exist as in game
     *
     * @param registry - handler for registering
     */
    default void registerContentRegistry(IExplosiveContentRegistry registry)
    {
        registerContentRegistry(registry.getRegistryName(), registry);
    }

    /**
     * Registers a new content type for explosives to exist as in game
     *
     * @param name     - unique ID
     * @param registry - handler for registering
     */
    void registerContentRegistry(ResourceLocation name, IExplosiveContentRegistry registry);

    /**
     * Gets all register explosives as an immutable set
     *
     * @return
     */
    Set<IExplosiveData> getExplosives();
}
