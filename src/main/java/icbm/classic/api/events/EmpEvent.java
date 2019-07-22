package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * Base class for any event fired by the EMP system
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public abstract class EmpEvent extends BlastEvent
{

    public EmpEvent(IBlast blast)
    {
        super(blast);
    }

    /**
     * Called before an entity is hit with an EMP effect. Allows canceling the effect for any reason.
     * <p>
     * Canceling the effect will remove any side effects that are normally applied. This includes
     * mutating some entities, apply some effects, and adding EMP effects to items.
     */
    @Cancelable
    public static class EntityPre extends EmpEvent
    {
        public final Entity target;

        public EntityPre(IBlast emp, Entity target)
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

        public EntityPost(IBlast emp, Entity target)
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
    @Cancelable
    public static class BlockPre extends EmpEvent
    {
        public final World world;
        public final BlockPos blockPos;
        public final IBlockState state;

        public BlockPre(IBlast emp, World world, BlockPos pos, IBlockState state)
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
        public final World world;
        public final BlockPos blockPos;
        public final IBlockState state;

        public BlockPost(IBlast emp, World world, BlockPos pos, IBlockState state)
        {
            super(emp);
            this.world = world;
            this.blockPos = pos;
            this.state = state;
        }
    }
}
