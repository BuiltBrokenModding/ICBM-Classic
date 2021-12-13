package icbm.classic.content.entity.missile;

import icbm.classic.prefab.entity.EntityProjectile;
import net.minecraft.world.World;

/**
 * Created by Robin Seifert on 12/12/2021.
 */
public class EntityMissile<E extends EntityMissile<E>> extends EntityProjectile<E>
{
    public EntityMissile(World world)
    {
        super(world);
    }
}
