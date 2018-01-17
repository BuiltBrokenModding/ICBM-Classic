package icbm.classic.content.potion;

import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.List;

public class PoisonContagion extends CustomPotion
{
    public static Potion INSTANCE;

    public PoisonContagion(boolean isBadEffect, int color, int id, String name)
    {
        super(isBadEffect, color, id, name);
        this.setIconIndex(6, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entityLiving, int amplifier)
    {
        World world = entityLiving.world;
        if (!(entityLiving instanceof EntityZombie) && !(entityLiving instanceof EntityPigZombie))
        {
            entityLiving.attackEntityFrom(DamageSource.MAGIC, 1);
        }

        if (entityLiving.world.rand.nextFloat() > 0.8)
        {
            int r = 13;
            AxisAlignedBB entitySurroundings = new AxisAlignedBB(entityLiving.posX - r, entityLiving.posY - r, entityLiving.posZ - r, entityLiving.posX + r, entityLiving.posY + r, entityLiving.posZ + r);
            List<EntityLivingBase> entities = entityLiving.world.getEntitiesWithinAABB(EntityLivingBase.class, entitySurroundings);

            for (EntityLivingBase entity : entities)
            {
                if (entity != null && entity != entityLiving)
                {
                    if (entity instanceof EntityPig)
                    {
                        EntityPigZombie newEntity = new EntityPigZombie(entity.world);
                        newEntity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                        if (!entity.world.isRemote)
                        {
                            entity.world.spawnEntity(newEntity);
                        }
                        entity.setDead();
                    }
                    else if (entity instanceof EntityVillager)
                    {
                        if ((world.getDifficulty() == EnumDifficulty.NORMAL || world.getDifficulty() == EnumDifficulty.HARD))
                        {

                            EntityVillager entityvillager = (EntityVillager) entity;
                            EntityZombieVillager entityzombievillager = new EntityZombieVillager(world);
                            entityzombievillager.copyLocationAndAnglesFrom(entityvillager);
                            world.removeEntity(entityvillager);
                            entityzombievillager.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityzombievillager)), null);
                            entityzombievillager.setProfession(entityvillager.getProfession());
                            entityzombievillager.setChild(entityvillager.isChild());
                            entityzombievillager.setNoAI(entityvillager.isAIDisabled());

                            if (entityvillager.hasCustomName())
                            {
                                entityzombievillager.setCustomNameTag(entityvillager.getCustomNameTag());
                                entityzombievillager.setAlwaysRenderNameTag(entityvillager.getAlwaysRenderNameTag());
                            }

                            world.spawnEntity(entityzombievillager);
                            world.playEvent((EntityPlayer) null, 1026, new BlockPos(entity), 0);
                        }
                        entity.setDead();
                    }

                    ICBMClassic.contagios_potion.poisonEntity(new Pos(entity), entity);
                }
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        if (duration % (20 * 2) == 0)
        {
            return true;
        }

        return false;
    }
}
