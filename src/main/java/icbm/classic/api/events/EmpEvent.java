package icbm.classic.api.events;

import icbm.classic.api.explosion.IBlast;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base class for any event fired by the EMP system
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public abstract class EmpEvent extends Event
{
    /** EMP blast that triggered the event */
    public final IBlast emp_blast;

    public EmpEvent(IBlast emp_blast)
    {
        this.emp_blast = emp_blast;
    }

    /**
     * Source of the blast.
     */
    public World world()
    {
        return emp_blast.world();
    }

    /**
     * Source of the blast.
     */
    public double x()
    {
        return emp_blast.x();
    }

    /**
     * Source of the blast.
     */
    public double y()
    {
        return emp_blast.y();
    }

    /**
     * Source of the blast.
     */
    public double z()
    {
        return emp_blast.z();
    }

    /**
     * Source of the blast.
     * <p>
     * Normally a Missile, Grenade, or Minecraft
     *
     * @return entity, can be null in some cases
     */
    public Entity getSourceEntity()
    {
        return emp_blast.getBlastSource();
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
