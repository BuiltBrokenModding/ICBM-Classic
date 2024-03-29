package icbm.classic.content.blast;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class BlastMutation extends Blast
{
    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.world().isRemote)
        {
            final AxisAlignedBB bounds = new AxisAlignedBB(location.x() - this.getBlastRadius(), location.y() - this.getBlastRadius(), location.z() - this.getBlastRadius(), location.x() + this.getBlastRadius(), location.y() + this.getBlastRadius(), location.z() + this.getBlastRadius());
            final List<EntityLiving> entitiesNearby = world().getEntitiesWithinAABB(EntityLiving.class, bounds);

            for (EntityLiving entity : entitiesNearby)
            {
                applyMutationEffect(entity);
            }
        }
        return false;
    }

    public static boolean applyMutationEffect(final EntityLivingBase entity)
    {
        if (entity instanceof EntityPig)
        {
            final EntityPigZombie newEntity = new EntityPigZombie(entity.world);
            newEntity.preventEntitySpawning = true;
            newEntity.setPosition(entity.posX, entity.posY, entity.posZ);
            entity.setDead();
            entity.world.spawnEntity(newEntity);
            return true;
        }
        else if (entity instanceof EntityVillager)
        {
            final EntityZombieVillager newEntity = new EntityZombieVillager(entity.world);
            newEntity.preventEntitySpawning = true;
            newEntity.setPosition(entity.posX, entity.posY, entity.posZ);
            newEntity.setForgeProfession(((EntityVillager) entity).getProfessionForge());
            entity.setDead();
            entity.world.spawnEntity(newEntity);
            return true;
        }
        return false;
    }

    @Override //disable the sound for this explosive
    protected void playExplodeSound()
    {
    }
}
