package icbm.classic.content.potion;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PoisonContagion extends CustomPotion
{
    public static Potion INSTANCE;
    public static int TRIGGER_RATE = 20 * 2; //TODO config
    public static int SPREAD_RANGE = 13;
    public static float SPREAD_CHANCE = 0.8f;
    public static boolean ENABLED = true;
    public static List<Function<Entity, Boolean>> DISABLE_LIST = new ArrayList<>();

    static {
        DISABLE_LIST.add((e) -> e instanceof AbstractSkeleton);
        DISABLE_LIST.add((e) -> e instanceof EntitySkeletonHorse);
    }

    public PoisonContagion(boolean isBadEffect, int color, int id, String name)
    {
        super(isBadEffect, color, id, name);
        this.setIconIndex(6, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entityLiving, int amplifier)
    {
        final World world = entityLiving.world;

        // Allow removing the effect
        if(!ENABLED || DISABLE_LIST.stream().anyMatch(f -> f.apply(entityLiving))) {
            entityLiving.removePotionEffect(this);
            return;
        }

        // Undead can't be harmed by the illness
        if (!(entityLiving instanceof EntityZombie))
        {
            entityLiving.attackEntityFrom(DamageSource.MAGIC, 1); //TODO add custom damage source
        }

        if (entityLiving.world.rand.nextFloat() > SPREAD_CHANCE) //TODO spread on attack, and add cough sound effect
        {
            AxisAlignedBB entitySurroundings = new AxisAlignedBB(
                entityLiving.posX - SPREAD_RANGE, entityLiving.posY - SPREAD_RANGE, entityLiving.posZ - SPREAD_RANGE,
                entityLiving.posX + SPREAD_RANGE, entityLiving.posY + SPREAD_RANGE, entityLiving.posZ + SPREAD_RANGE
            );
            List<EntityLivingBase> entities = entityLiving.world.getEntitiesWithinAABB(EntityLivingBase.class, entitySurroundings);

            for (EntityLivingBase entity : entities)
            {
                if (entity != null && entity != entityLiving)
                {
                    // TODO add custom zombie
                    // TODO add zombie mobs like sheeps
                    // TODO add custom creeper that can explode to spread at greater distances
                    // TODO consider adding most of this via an addon and keeping this simple?
                    if (entity instanceof EntityPig)
                    {
                        entity = new EntityPigZombie(entity.world);
                        entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);

                        if (!world.isRemote)
                        {
                            world.spawnEntity(entity);
                        }
                        entity.setDead();
                    }
                    else if (entity instanceof EntityVillager)
                    {
                        if ((world.getDifficulty() == EnumDifficulty.NORMAL || world.getDifficulty() == EnumDifficulty.HARD))
                        {

                            final EntityVillager entityvillager = (EntityVillager) entity;

                            EntityZombieVillager entityzombievillager = new EntityZombieVillager(world);
                            entity = entityzombievillager;

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

                    ICBMClassic.contagiousPotion.poisonEntity(new Pos(entity), entity);
                }
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        return duration % TRIGGER_RATE == 0;
    }
}
