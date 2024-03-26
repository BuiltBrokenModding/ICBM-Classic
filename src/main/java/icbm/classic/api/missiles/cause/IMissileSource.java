package icbm.classic.api.missiles.cause;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Information about the source (starting point) of the missile.
 * <p>
 * This is for tracking purposes and should never be exposed to the player. Main purpose of this data
 * should always be for mod interaction and admin tools. Player are not meant to see this data directly as it could
 * easily provide them with the location of attackers. Instead, players should have to work for figuring out
 * attacker distance and direction.
 * <p>
 * That said ICBM team will not stop anyone from using the data. As there are valid interaction uses. Such as showing
 * missiles on an interactive map or exposing the player's own missile launch position to themselves.
 */
public interface IMissileSource extends INBTSerializable<CompoundTag> {
    /**
     * Level the missile was launched from
     *
     * @return world
     */
    Level getLevel();

    /**
     * Get cause of the missile launch.
     *
     * @return cause of the launch
     */
    IMissileCause getCause();

    /**
     * Exact position source fired the missile
     * <p>
     * If the source is a block this could be above the block.
     *
     * @retur position
     */
    Vec3 getPosition();
}
