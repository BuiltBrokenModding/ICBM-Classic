package icbm.classic.content.blast;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BlastMutation extends Blast
{
    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.world().isClientSide())
        {
            final AABB bounds = new AABB(location.x() - this.getBlastRadius(), location.y() - this.getBlastRadius(), location.z() - this.getBlastRadius(), location.x() + this.getBlastRadius(), location.y() + this.getBlastRadius(), location.z() + this.getBlastRadius());
            final List<Mob> entitiesNearby = world().getEntitiesOfClass(Mob.class, bounds);

            for (Mob entity : entitiesNearby)
            {
                applyMutationEffect(entity);
            }
        }
        // PORT: one-shot blast, return true so completeBlast runs and the blast leaves WorkTickingActionHandler.activeBlasts
        return true;
    }

    public static boolean applyMutationEffect(final LivingEntity entity)
    {
        // PORT: 1.12 spawned a new entity by hand, 1.21 Mob#convertTo copies position, name and equipment then discards the old one
        if (entity instanceof Pig pig)
        {
            return pig.convertTo(EntityType.ZOMBIFIED_PIGLIN, false) != null;
        }
        else if (entity instanceof Villager villager)
        {
            final ZombieVillager newEntity = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
            if (newEntity != null)
            {
                newEntity.setVillagerData(villager.getVillagerData());
                return true;
            }
        }
        return false;
    }

    @Override //disable the sound for this explosive
    protected void playExplodeSound()
    {
    }
}
// PORTED
