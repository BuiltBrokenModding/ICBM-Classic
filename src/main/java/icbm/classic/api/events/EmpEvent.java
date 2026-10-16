package icbm.classic.api.events;

import icbm.classic.api.actions.IAction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Base class for any event fired by the EMP system
 *
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/12/2018.
 *
 * @deprecated will be replaced by an event providing {@link icbm.classic.api.actions.IAction}
 */
@Deprecated
public abstract class EmpEvent extends BlastEvent
{
    public EmpEvent(IAction blast)
    {
        super(blast);
    }

    /**
     * Called before an entity is hit with an EMP effect. Allows canceling the effect for any reason.
     * <p>
     * Canceling the effect will remove any side effects that are normally applied. This includes
     * mutating some entities, apply some effects, and adding EMP effects to items.
     */
    public static class EntityPre extends EmpEvent implements ICancellableEvent
    {
        public final Entity target;

        public EntityPre(IAction emp, Entity target)
        {
            super(emp);
            this.target = target;
        }
    }

    /**
     * Called after EMP effects have been applied to the entity. This includes several different
     * effects and EMP effects on items.
     */
    public static class EntityPost extends EmpEvent
    {
        public final Entity target;

        public EntityPost(IAction emp, Entity target)
        {
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
    public static class BlockPre extends EmpEvent implements ICancellableEvent
    {
        public final Level world;
        public final BlockPos blockPos;
        public final BlockState state;

        public BlockPre(IAction emp, Level world, BlockPos pos, BlockState state)
        {
            super(emp);
            this.world = world;
            this.blockPos = pos;
            this.state = state;
        }
    }

    /**
     * Called after EMP effects have been applied to the entity. This includes several different
     * effects and EMP effects on items.
     */
    public static class BlockPost extends EmpEvent
    {
        public final Level world;
        public final BlockPos blockPos;
        public final BlockState state;

        public BlockPost(IAction emp, Level world, BlockPos pos, BlockState state)
        {
            super(emp);
            this.world = world;
            this.blockPos = pos;
            this.state = state;
        }
    }
}
// PORTED
