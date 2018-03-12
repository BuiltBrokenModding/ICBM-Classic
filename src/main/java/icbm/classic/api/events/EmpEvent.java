package icbm.classic.api.events;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base class for any event fired by the EMP system
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/12/2018.
 */
public abstract class EmpEvent extends Event
{
    public final World source_world;
    public final double source_x;
    public final double source_y;
    public final double source_z;

    public final Entity source_entity;

    public EmpEvent(World source_world, double source_x, double source_y, double source_z, Entity source_entity)
    {
        this.source_world = source_world;
        this.source_x = source_x;
        this.source_y = source_y;
        this.source_z = source_z;
        this.source_entity = source_entity;
    }

    public static class EntityPre extends EmpEvent
    {
        public final Entity target;

        public EntityPre(World source_world, double source_x, double source_y, double source_z, Entity source_entity, Entity target)
        {
            super(source_world, source_x, source_y, source_z, source_entity);
            this.target = target;
        }
    }
}
