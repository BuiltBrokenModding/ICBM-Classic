package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.NBTConstants;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.potion.CustomPotionEffect;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Array;
import java.util.*;

public class BlastGasBase extends Blast implements IBlastTickable
{
    private static final int TICKS_BETWEEN_RUNS = 5;

    private int duration;
    /**
     * Color of particles
     */
    private float red = 1, green = 1, blue = 1;
    private boolean playShortSoundFX;
    private boolean visible = true, applyConfusionEffect, applyPoisonEffect, applyContagiousEffect, mutateEntities;
    private List<BlockPos> affectedBlocks;
    private int lastRadius = 0;
    private HashMap<EntityLivingBase, Integer> damagedEntites;


    public BlastGasBase(int duration, boolean playShortSoundFX)
    {
        this.duration = duration;
        this.playShortSoundFX = playShortSoundFX;
        this.affectedBlocks = new ArrayList<>();
        this.damagedEntites = new HashMap<>();
    }

    private double sizePercentageOverTime(int timePassed)
    {
        return Math.min(1, 2f * timePassed / duration + 0.1f);
    }

    public BlastGasBase setRGB(float r, float g, float b)
    {
        this.red = r;
        this.green = g;
        this.blue = b;
        return this;
    }

    public BlastGasBase setInvisible()
    {
        this.visible = false;
        return this;
    }

    public BlastGasBase setConfuse()
    {
        this.applyConfusionEffect = true;
        return this;
    }

    public BlastGasBase setPoison()
    {
        this.applyPoisonEffect = true;
        return this;
    }

    public BlastGasBase setContagious()
    {
        this.applyContagiousEffect = true;
        this.mutateEntities = true;
        return this;
    }

    @Override
    public Blast setPosition(double posX, double posY, double posZ)
    {
        return super.setPosition(posX, posY, posZ);
    }

    @Override
    public void setupBlast()
    {
        super.setupBlast();
    }

    @Override
    public boolean doExplode(int callCount)
    {
        //Play start audio
        if (callCount == 0 && !this.playShortSoundFX)
        {
            ICBMSounds.DEBILITATION.play(world, this.location.x(), this.location.y(), this.location.z(), 4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }

        //Do gas effect
        if (callCount % TICKS_BETWEEN_RUNS == 0)
        {
            setEffectBoundsAndSpawnParticles(this.callCount); // recalculate the affected blocks (where particles spawn, poison is applied, etc.)

            //Trigger effects for user feedback
            //generateGraphicEffect();
            generateAudioEffect();


            //Only run potion effect application for the following types
            if (applyConfusionEffect || applyContagiousEffect || applyPoisonEffect || mutateEntities)
            {
                double radius = this.getBlastRadius();
                AxisAlignedBB bounds = new AxisAlignedBB(
                        location.x() - radius, location.y() - radius, location.z() - radius,
                        location.x() + radius, location.y() + radius, location.z() + radius);
                List<EntityLivingBase> bbents = world().getEntitiesWithinAABB(EntityLivingBase.class, bounds);

                //Get all living entities in range of the explosive effect
                List<EntityLivingBase> affectedEntities = new ArrayList<>();
                for (EntityLivingBase ent : bbents)
                {
                    for (BlockPos block : affectedBlocks)
                    {
                        if (block.equals(ent.getPosition()))
                        {
                            affectedEntities.add(ent);
                            break;
                        }
                    }
                }

                //Loop all entities
                for (EntityLivingBase entity : affectedEntities)
                {
                    if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).isCreative())
                    {
                        if (!damagedEntites.containsKey(entity))
                            damagedEntites.put(entity, 1);
                        else
                            damagedEntites.replace(entity, damagedEntites.get(entity) + 1);

                        int hitCount = damagedEntites.get(entity);
                        if (this.applyContagiousEffect)
                        {
                            ICBMClassic.contagios_potion.poisonEntity(location.toPos(), entity, 3);
                            if (hitCount > 10)
                            {
                                entity.attackEntityFrom(new DamageSource("icbm.contagious"), (hitCount - 10f) / 5);
                            }
                        }

                        if (this.applyPoisonEffect)
                        {
                            ICBMClassic.poisonous_potion.poisonEntity(location.toPos(), entity);
                            if (hitCount > 20)
                            {
                                entity.attackEntityFrom(new DamageSource("icbm.chemical"), (hitCount - 10f) / 10);
                            }
                        }

                        if (this.applyConfusionEffect)
                        {
                            entity.addPotionEffect(new CustomPotionEffect(MobEffects.POISON, 18 * 20, 0));
                            entity.addPotionEffect(new CustomPotionEffect(MobEffects.MINING_FATIGUE, 20 * 60, 0));
                            entity.addPotionEffect(new CustomPotionEffect(MobEffects.SLOWNESS, 20 * 60, 2));
                        }
                    }
                }
            }

            //Trigger secondary blast which mutates mobs similar to a lightning strike
            if (this.mutateEntities)
            {
                new BlastMutation()
                        .setBlastWorld(world())
                        .setBlastSource(this.exploder)
                        .setBlastPosition(location.x(), location.y(), location.z())
                        .setBlastSize(getBlastRadius())
                        .buildBlast().runBlast(); //TODO trigger from explosive handler
            }

            //End explosion when we hit life timer
            return this.callCount > this.duration;
        }

        return false;
    }

    private void generateAudioEffect()
    {
        if (this.playShortSoundFX)
        {
            ICBMSounds.GAS_LEAK.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D,
                    4.0F, (1.0F + (world().rand.nextFloat() - world().rand.nextFloat()) * 0.2F) * 1F, true);
        }
    }

    private void setEffectBoundsAndSpawnParticles(int timePassed)
    {
        final int radius = (int) (this.getBlastRadius() * sizePercentageOverTime(timePassed));
        if (lastRadius == radius)
            return;

        lastRadius = radius;

        final double maxDstSquarde = radius * radius;
        List<BlockPos> affected = new ArrayList<>();
        affected.add(this.getPos());
        List<BlockPos> lastGrown = new ArrayList<>();
        lastGrown.add(this.getPos());
        for (int i = 0; i < radius; i++) // grow once per radius size
        {
            List<BlockPos> currentGrown = new ArrayList<>();
            for (BlockPos bp : lastGrown)
            {
                List<BlockPos> dirs = new ArrayList<>();
                dirs.add(bp.up());
                dirs.add(bp.down());
                dirs.add(bp.north());
                dirs.add(bp.east());
                dirs.add(bp.south());
                dirs.add(bp.west());

                for (BlockPos dir : dirs)
                {
                    if (!affected.contains(dir) && !currentGrown.contains(dir) && dir.distanceSq(getPos()) < maxDstSquarde && !world.getBlockState(dir).isFullBlock())
                    {
                        if (visible && !affectedBlocks.contains(dir))
                            for (int j = 0; j < 5; j++)
                                ICBMClassic.proxy.spawnAirParticle(world, new Pos(dir),
                                        (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2 - 0.1, (Math.random() - 0.5) / 2,
                                        this.red, this.green, this.blue, 7.0F, (int) (world.rand.nextFloat() * 40 + duration));

                        currentGrown.add(dir);
                    }
                }
            }
            affected.addAll(currentGrown);
            lastGrown = new ArrayList<>(currentGrown);
        }
        affectedBlocks = affected;
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        this.duration = nbt.getInteger(NBTConstants.DURATION);
        this.applyContagiousEffect = nbt.getBoolean(NBTConstants.IS_CONTAGIOUS);
        this.applyPoisonEffect = nbt.getBoolean(NBTConstants.IS_POISONOUS);
        this.applyConfusionEffect = nbt.getBoolean(NBTConstants.IS_CONFUSE);
        this.mutateEntities = nbt.getBoolean(NBTConstants.IS_MUTATE);
        this.visible = nbt.getBoolean(NBTConstants.IS_VISIBLE);
        this.red = nbt.getFloat(NBTConstants.RED);
        this.green = nbt.getFloat(NBTConstants.GREEN);
        this.blue = nbt.getFloat(NBTConstants.BLUE);
        this.playShortSoundFX = nbt.getBoolean(NBTConstants.PLAY_SHORT_SOUND_FX);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setInteger(NBTConstants.DURATION, this.duration);
        nbt.setBoolean(NBTConstants.IS_CONTAGIOUS, this.applyContagiousEffect);
        nbt.setBoolean(NBTConstants.IS_POISONOUS, this.applyPoisonEffect);
        nbt.setBoolean(NBTConstants.IS_CONFUSE, this.applyConfusionEffect);
        nbt.setBoolean(NBTConstants.IS_MUTATE, this.mutateEntities);
        nbt.setBoolean(NBTConstants.IS_VISIBLE, this.visible);
        nbt.setFloat(NBTConstants.RED, this.red);
        nbt.setFloat(NBTConstants.GREEN, this.green);
        nbt.setFloat(NBTConstants.BLUE, this.blue);
        nbt.setBoolean(NBTConstants.PLAY_SHORT_SOUND_FX, this.playShortSoundFX);
    }
}
