package icbm.classic.api.missiles.cause;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Information about the source (starting point) of the missile.
 *
 * This is for tracking purposes and should never be exposed to the player. Main purpose of this data
 * should always be for mod interaction and admin tools. Player are not meant to see this data directly as it could
 * easily provide them with the location of attackers. Instead, players should have to work for figuring out
 * attacker distance and direction.
 *
 * That said ICBM team will not stop anyone from using the data. As there are valid interaction uses. Such as showing
 * missiles on an interactive map or exposing the player's own missile launch position to themselves.
 */
public interface IMissileSource extends INBTSerializable<NBTTagCompound>
{
    /**
     * World the missile was launched from
     *
     * @return world
     */
    World getWorld();

    /**
     * Get cause of the missile launch.
     *
     * @return cause of the launch
     */
    IMissileCause getCause();

    /**
     * Exact position source fired the missile
     *
     * If the source is a block this could be above the block.
     *
     * @retur position
     */
    Vec3d getPosition();
}
