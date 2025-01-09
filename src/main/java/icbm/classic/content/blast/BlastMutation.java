package icbm.classic.content.blast;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombiePigmanEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BlastMutation extends Blast
{
    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.getWorld().isRemote)
        {
            final AxisAlignedBB bounds = new AxisAlignedBB(
                getPosition().x - this.getBlastRadius(), getPosition().y - this.getBlastRadius(), getPosition().z - this.getBlastRadius(),
                getPosition().x + this.getBlastRadius(), getPosition().y + this.getBlastRadius(), getPosition().z + this.getBlastRadius());
            final List<MobEntity> entitiesNearby = getWorld().getEntitiesWithinAABB(MobEntity.class, bounds);

            for (MobEntity entity : entitiesNearby)
            {
                applyMutationEffect(entity);
            }
        }
        return false;
    }

    public static boolean applyMutationEffect(final LivingEntity entity)
    {
        if (entity instanceof PigEntity)
        {
            final ZombiePigmanEntity newEntity = EntityType.ZOMBIE_PIGMAN.create(entity.world);
            newEntity.preventEntitySpawning = true;
            newEntity.setPosition(entity.posX, entity.posY, entity.posZ);
            entity.remove();
            entity.world.addEntity(newEntity);
            return true;
        }
        else if (entity instanceof VillagerEntity)
        {
            VillagerEntity villagerentity = (VillagerEntity)entity;
            ZombieVillagerEntity zombievillagerentity = EntityType.ZOMBIE_VILLAGER.create(entity.world);
            zombievillagerentity.copyLocationAndAnglesFrom(villagerentity);
            villagerentity.remove();
            zombievillagerentity.onInitialSpawn(entity.world, entity.world.getDifficultyForLocation(new BlockPos(zombievillagerentity)), SpawnReason.CONVERSION, null, null);
            zombievillagerentity.func_213792_a(villagerentity.getVillagerData());
            zombievillagerentity.func_223727_a(villagerentity.func_223722_es().func_220914_a(NBTDynamicOps.INSTANCE).getValue());
            zombievillagerentity.func_213790_g(villagerentity.getOffers().func_222199_a());
            zombievillagerentity.func_213789_a(villagerentity.getXp());
            zombievillagerentity.setChild(villagerentity.isChild());
            zombievillagerentity.setNoAI(villagerentity.isAIDisabled());
            if (villagerentity.hasCustomName()) {
                zombievillagerentity.setCustomName(villagerentity.getCustomName());
                zombievillagerentity.setCustomNameVisible(villagerentity.isCustomNameVisible());
            }

            entity.world.addEntity(zombievillagerentity);
            entity.world.playEvent(null, 1026, entity.getPosition(), 0);
            return true;
        }
        return false;
    }

    @Override //disable the sound for this explosive
    protected void playExplodeSound()
    {
    }
}
