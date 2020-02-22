package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.potion.CustomPotionEffect;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BlastGasBase extends Blast implements IBlastTickable
{
    //Cache damage
    public static final DamageSource CONTAGIUS_DAMAGE = new DamageSource("icbm.contagious");
    public static final DamageSource CHEMICAL_DAMAGE = new DamageSource("icbm.chemical");

    //Settings
    private static final int TICKS_BETWEEN_RUNS = 5;

    //Cache usage
    private static final BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();


    private int duration;

    // Color of particles
    private float red = 1, green = 1, blue = 1;
    private boolean playShortSoundFX;
    private boolean applyConfusionEffect, applyPoisonEffect, applyContagiousEffect, mutateEntities;

    private int lastRadius = 0;

    //AOE
    private HashSet<BlockPos> affectedBlocks = new HashSet();
    private Queue<BlockPos> edgeBlocks = new LinkedList();

    //Damage tracker for entities effected
    private HashMap<EntityLivingBase, Integer> impactedEntityMap = new HashMap();
    //TODO turn into entity capability to prevent damage stacking of several explosives
    //TODO use weak refs to not hold instances

    public BlastGasBase(int duration, boolean playShortSoundFX)
    {
        this.duration = duration;
        this.playShortSoundFX = playShortSoundFX;
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

            //Spawn particles
            //affectedBlocks.forEach(pos -> spawnGasParticles(pos));

            //Only run potion effect application for the following types
            if (applyConfusionEffect || applyContagiousEffect || applyPoisonEffect || mutateEntities)
            {
                final double radius = this.getBlastRadius();

                //Max bounds
                final AxisAlignedBB bounds = new AxisAlignedBB(
                        location.x() - radius, location.y() - radius, location.z() - radius,
                        location.x() + radius, location.y() + radius, location.z() + radius);

                final List<EntityLivingBase> entityList = world()
                        .getEntitiesWithinAABB(EntityLivingBase.class, bounds, this::canGasEffect);

                //Loop all entities
                for (EntityLivingBase entity : entityList)
                {
                    //Track entities
                    if (!impactedEntityMap.containsKey(entity))
                    {
                        impactedEntityMap.put(entity, 1);
                    }
                    else
                    {
                        impactedEntityMap.replace(entity, impactedEntityMap.get(entity) + 1);
                    }

                    //Scale damage with hit count
                    final int hitCount = impactedEntityMap.get(entity);

                    if (this.applyContagiousEffect)
                    {
                        ICBMClassic.contagios_potion.poisonEntity(location.toPos(), entity, 3);
                        if (hitCount > 10)
                        {
                            entity.attackEntityFrom(CONTAGIUS_DAMAGE, (hitCount - 10f) / 5);
                        }
                    }

                    if (this.applyPoisonEffect)
                    {
                        ICBMClassic.poisonous_potion.poisonEntity(location.toPos(), entity);
                        if (hitCount > 20)
                        {
                            entity.attackEntityFrom(CHEMICAL_DAMAGE, (hitCount - 10f) / 10);
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

            //Trigger secondary blast which mutates mobs similar to a lightning strike
            if (this.mutateEntities)
            {
                new BlastMutation()
                .setBlastWorld(world())
                .setBlastSource(this.exploder)
                .setBlastPosition(location.x(), location.y(), location.z())
                .setBlastSize(getBlastRadius())
                .setExplosiveData(ICBMExplosives.MUTATION)
                .buildBlast().runBlast(); //TODO trigger from explosive handler
            }

            //End explosion when we hit life timer
            return this.callCount > this.duration;
        }

        return false;
    }

    /**
     * Checking that the entity can be harmed or an effect can be applied
     *
     * @param entity
     * @return
     */
    private boolean canGasEffect(EntityLivingBase entity)
    {
        //Ignore dead things
        if (entity.isEntityAlive())
        {
            //Always ignore non-gameplay characters
            if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative())
            {
                return false;
            }

            //Check that we can harm the entity
            else if (applyContagiousEffect && entity.isEntityInvulnerable(CONTAGIUS_DAMAGE)) //TODO add ignore list for zombies and skeletons
            {
                return false;
            }
            else if (applyPoisonEffect && entity.isEntityInvulnerable(CHEMICAL_DAMAGE))
            {
                return false;
            }

            //Check that the entity is in range
            return affectedBlocks.contains(checkPos.setPos(entity.posX, entity.posY, entity.posZ));
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
        final int maxSize = (int) Math.ceil(this.getBlastRadius());
        //Get and validate radius
        final int radius = (int) Math.floor(maxSize * sizePercentageOverTime(timePassed));
        if (lastRadius == radius)
        {
            return;
        }
        lastRadius = radius;

        //Get radius sq for distance checks
        final int currentDistanceSQ = radius * radius;

        //Init path data
        if (affectedBlocks.isEmpty())
        {
            affectedBlocks.add(getPos());
            edgeBlocks.add(getPos());
        }

        if (edgeBlocks.isEmpty())
        {
            affectedBlocks
            .stream()
            .filter((pos) -> Math.random() > 0.5)
            .forEach(pos -> edgeBlocks.add(pos));
        }

        //Track blocks we pathed but didn't need
        final HashSet<BlockPos> hasPathed = new HashSet();
        //Track blocks we need to path next tick
        final Queue<BlockPos> nextSet = new LinkedList();


        //Loop edges from last tick
        while (edgeBlocks.peek() != null)
        {
            //Current edge block
            final BlockPos edge = edgeBlocks.poll();

            //Loop all 6 sides of the edge
            for (EnumFacing facing : EnumFacing.values())
            {
                //Move our check pos to current target
                checkPos.setPos(edge);
                checkPos.move(facing);

                //Don't repath
                if (!hasPathed.contains(checkPos) && !affectedBlocks.contains(checkPos))
                {
                    //Check that it is in range
                    if (isInRange(checkPos, currentDistanceSQ))
                    {
                        //Validate
                        if (isValidPath(checkPos))
                        {
                            final BlockPos pos = checkPos.toImmutable();
                            affectedBlocks.add(pos);
                            nextSet.add(pos);

                            spawnGasParticles(pos);
                        }
                        //Ignore if invalid
                        else
                        {
                            hasPathed.add(checkPos.toImmutable());
                        }
                    }
                }
            }
        }

        //Add next set to follow up queue
        edgeBlocks.addAll(nextSet);
    }

    private boolean isValidPath(BlockPos pos)
    {
        IBlockState blockState = world.getBlockState(pos);
        return !blockState.isFullBlock();
    }

    private boolean isInRange(BlockPos pos, int radiusSq)
    {
        return (int) Math.floor(pos.distanceSq(xi(), yi(), zi())) <= radiusSq;
    }

    private void spawnGasParticles(BlockPos pos)
    {
        for (int j = 0; j < 1; j++) //TODO send packet to client to spawn all 5 at once
        {
            ICBMClassic.proxy.spawnAirParticle(world, new Pos(pos),
                    (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2 - 0.1, (Math.random() - 0.5) / 2,
                    this.red, this.green, this.blue,
                    7.0F, duration);
        }
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
        nbt.setFloat(NBTConstants.RED, this.red);
        nbt.setFloat(NBTConstants.GREEN, this.green);
        nbt.setFloat(NBTConstants.BLUE, this.blue);
        nbt.setBoolean(NBTConstants.PLAY_SHORT_SOUND_FX, this.playShortSoundFX);
    }
}
