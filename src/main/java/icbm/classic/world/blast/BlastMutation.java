package icbm.classic.world.blast;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.monster.EntityPigZombie;
import net.minecraft.world.entity.monster.EntityZombieVillager;
import net.minecraft.world.entity.passive.EntityPig;
import net.minecraft.world.entity.passive.EntityVillager;

import java.util.List;

public class BlastMutation extends Blast {
    @Override
    public boolean doExplode(int callCount) {
        if (!this.level().isClientSide()) {
            final AxisAlignedBB bounds = new AxisAlignedBB(location.x() - this.getBlastRadius(), location.y() - this.getBlastRadius(), location.z() - this.getBlastRadius(), location.x() + this.getBlastRadius(), location.y() + this.getBlastRadius(), location.z() + this.getBlastRadius());
            final List<EntityLiving> entitiesNearby = level().getEntitiesWithinAABB(EntityLiving.class, bounds);

            for (EntityLiving entity : entitiesNearby) {
                applyMutationEffect(entity);
            }
        }
        return false;
    }

    public static boolean applyMutationEffect(final EntityLivingBase entity) {
        if (entity instanceof EntityPig) {
            final EntityPigZombie newEntity = new EntityPigZombie(entity.world);
            newEntity.preventEntitySpawning = true;
            newEntity.setPosition(entity.getX(), entity.getY(), entity.getZ());
            entity.setDead();
            entity.world.spawnEntity(newEntity);
            return true;
        } else if (entity instanceof EntityVillager) {
            final EntityZombieVillager newEntity = new EntityZombieVillager(entity.world);
            newEntity.preventEntitySpawning = true;
            newEntity.setPosition(entity.getX(), entity.getY(), entity.getZ());
            newEntity.setForgeProfession(((EntityVillager) entity).getProfessionForge());
            entity.setDead();
            entity.world.spawnEntity(newEntity);
            return true;
        }
        return false;
    }

    @Override //disable the sound for this explosive
    protected void playExplodeSound() {
    }
}
