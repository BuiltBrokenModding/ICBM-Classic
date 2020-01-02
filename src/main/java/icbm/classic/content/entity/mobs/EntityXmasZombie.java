package icbm.classic.content.entity.mobs;

import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 12/31/2018.
 */
public class EntityXmasZombie extends EntityXmasMob
{
    public EntityXmasZombie(World worldIn)
    {
        super(worldIn);
        this.setSize(0.6F, 1.6F);
    }

    @Override
    protected double getArmOffset()
    {
        return -0.2;
    }

    @Override
    protected double getForwardOffset()
    {
        return  0.5;
    }

    @Override
    public boolean isIceFaction()
    {
        return false;
    }

    @Override
    protected void initEntityAI()
    {
        super.initEntityAI();
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntitySkeleton.class, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityXmasSkeleton.class, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityXmasSnowman.class, true));
    }

    @Override
    public float getEyeHeight()
    {
        return 1.24F;
    }
}
