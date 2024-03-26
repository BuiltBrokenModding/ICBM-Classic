package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Base class for any event fired by the EMP system
 * <p>
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public abstract class EmpEvent extends BlastEvent<IBlast> {
    public EmpEvent(IBlast blast) {
        super(blast);
    }

    /**
     * Called before an entity is hit with an EMP effect. Allows canceling the effect for any reason.
     * <p>
     * Canceling the effect will remove any side effects that are normally applied. This includes
     * mutating some entities, apply some effects, and adding EMP effects to items.
     */
    public static class EntityPre extends EmpEvent implements ICancellableEvent {
        public final Entity target;

        public EntityPre(IBlast emp, Entity target) {
            super(emp);
            this.target = target;
        }
    }

    /**
     * Called after EMP effects have been applied to the entity. This includes several different
     * effects and EMP effects on items.
     */
    public static class EntityPost extends EmpEvent {
        public final Entity target;

        public EntityPost(IBlast emp, Entity target) {
            super(emp);
            this.target = target;
        }
    }

    /**
     * Called before an entity is hit with an EMP effect. Allows canceling the effect for any reason.
     * <p>
     * Canceling the effect will remove any side effects that are normally applied. This includes
     * mutating some entities, apply some effects, and adding EMP effects to items.
     */
    public static class BlockPre extends EmpEvent implements ICancellableEvent {
        public final Level level;
        public final BlockPos blockPos;
        public final BlockState state;

        public BlockPre(IBlast emp, Level level, BlockPos pos, BlockState state) {
            super(emp);
            this.level = level;
            this.blockPos = pos;
            this.state = state;
        }
    }

    /**
     * Called after EMP effects have been applied to the entity. This includes several different
     * effects and EMP effects on items.
     */
    public static class BlockPost extends EmpEvent {
        public final Level level;
        public final BlockPos blockPos;
        public final BlockState state;

        public BlockPost(IBlast emp, Level level, BlockPos pos, BlockState state) {
            super(emp);
            this.level = level;
            this.blockPos = pos;
            this.state = state;
        }
    }
}
