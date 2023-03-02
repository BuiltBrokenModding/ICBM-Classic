package icbm.classic.api.missiles.cause;

import icbm.classic.api.missiles.parts.IMissilePart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Cause of a missile launch event. Stored as part of {@link IMissileSource}
 * used to track who or what fired the missile. Can be stored as a chain of
 * causes allowing detailed information to be tracked.
 *
 * Example: player -> remote -> screen -> silo -> cluster missile -> missile
 */
public interface IMissileCause extends IMissilePart {

    /**
     * First cause in the history
     *
     * @return first entry
     */
    default IMissileCause getRootCause() {
        final IMissileCause cause = getPreviousCause();
        if(cause != null) { //TODO add logic to prevent infinite loop
            return cause.getPreviousCause();
        }
        return null;
    }

    /**
     * Parent cause in the history
     *
     * @return cause
     */
    IMissileCause getPreviousCause();

    /**
     * Sets the missile cause
     * @param parent to use
     * @return self
     */
    IMissileCause setPreviousCause(IMissileCause parent);

    /**
     * Cause containing entity information
     */
    interface IEntityCause extends IMissileCause {
        Entity getEntity();
    }

    /**
     * Cause containing block information
     */
    interface IBlockCause extends IMissileCause {
        World getWorld();
        BlockPos getBlockPos();
        IBlockState getBlockState();
    }
}
