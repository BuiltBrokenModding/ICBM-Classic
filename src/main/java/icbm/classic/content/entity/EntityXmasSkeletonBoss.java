package icbm.classic.content.entity;

import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 12/30/2018.
 */
public class EntityXmasSkeletonBoss extends EntityXmasSkeleton
{
    public EntityXmasSkeletonBoss(World worldIn)
    {
        super(worldIn);
        this.setSize(0.8F, 4F);
    }

    @Override
    public float getEyeHeight()
    {
        return 3.4f;
    }
}
