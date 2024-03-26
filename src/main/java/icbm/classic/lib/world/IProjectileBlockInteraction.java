package icbm.classic.lib.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface IProjectileBlockInteraction {

    /**
     * Called when a projectile interacts with a block normally through collision or impact.
     * <p>
     * Position of projectile may not match hit vector due to raytracing. So take care when
     * doing intersection checks or another validations.
     *
     * @param projectile hitting the block
     * @param level      containing the event
     * @param pos        of the hit
     * @param state      of the block
     * @param side       of the hit
     * @param hit        position exact, normally raytrace driven
     * @return desired result for interaction
     */
    EnumHitReactions apply(Level level, BlockPos pos, Vec3 hit, Direction side, BlockState state, Entity projectile);


    enum EnumHitReactions {
        /**
         * Continue collision interaction, but pass to next handler in list
         */
        PASS(false),
        /**
         * Continue collision interaction
         */
        CONTINUE(false),
        /**
         * Continue but don't trigger impact
         */
        CONTINUE_NO_IMPACT(false),
        /**
         * Stop collision interaction
         */
        STOP(true),
        /**
         * STOP interaction due to entity movement
         */
        MOVED(true),
        /**
         * STOP interaction due to entity teleportation (cross dimension)
         */
        TELEPORTED(true);

        public final boolean stop;

        private EnumHitReactions(boolean stop) {
            this.stop = stop;
        }
    }
}
