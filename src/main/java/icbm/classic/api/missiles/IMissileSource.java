package icbm.classic.api.missiles;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Information about the source of the missile.
 *
 * This is for tracking purposes and should never be exposed to the player. Main purpose of this data
 * should always be for mod interaction and admin tools. Player are not meant to see this data directly as it could
 * easily provide them with the location of attackers. Instead, players should have to work for figuring out
 * attacker distance and direction.
 *
 * That said ICBM team will not stop anyone from using the data. As there are valid interaction uses. Such as showing
 * missiles on an interactive map or exposing the player's own missile launch position to themselves.
 */
public interface IMissileSource
{
    /**
     * World the missile was launched from
     *
     * @return world
     */
    World getWorld();

    /**
     * Get entity responsible for firing the current missile. This may not
     * be the original entity but could be a cluster missiles or other missile
     * creation source.
     *
     * @param actual entity who started the entire chain of entities
     *
     * @return entity
     */
    Entity getFiringEntity(boolean actual);

    /**
     * Type of source
     *
     * @return type
     */
    MissileSourceType getType();

    /**
     * Exact position source fired the missile
     *
     * If the source is a block this could be above the block.
     *
     * @retur position
     */
    Vec3d getFiredPosition();

    /**
     * Position of source block
     *
     * @return pos
     */
    BlockPos getBlockPos();

    /**
     * Save callback
     * @return save data, or null to save nothing
     */
    default NBTTagCompound save() {
        return null;
    }

    /**
     * Load callback
     * @param save data used
     */
    default void load(NBTTagCompound save) {

    }

    /**
     * Registry name to use with {@link icbm.classic.api.ICBMClassicAPI#MISSILE_SOURCE_REGISTRY}
     *
     * @return save id
     */
    ResourceLocation getRegistryName();

    @Deprecated //TODO replace with interfaces (IMissileSourceBlock, IMissileSourceEntity) that act as sub-types of the main sorta like events
    enum MissileSourceType {
        BLOCK,
        ENTITY
    }
}
