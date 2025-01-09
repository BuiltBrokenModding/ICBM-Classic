package icbm.classic.content.blast.gas;

import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blast.Blast;
import icbm.classic.content.gas.ProtectiveArmorHandler;
import icbm.classic.lib.NBTConstants;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.*;

public abstract class BlastGasBase extends Blast implements IBlastTickable
{
    /** Delay between running calculations */
    private static final int TICKS_BETWEEN_RUNS = 5;

    /** Mutable block pos for pathing */
    private static final BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

    /** Duration of gas effect, also controls particles */
    @Setter @Accessors(chain = true)
    protected int duration;

    @Setter @Accessors(chain = true)
    private boolean playShortSoundFX;

    private int lastRadius = 0;

    /** Blocks pathed */
    private final HashSet<BlockPos> affectedBlocks = new HashSet();
    /** Queue of edge blocks to path */
    private final Queue<BlockPos> edgeBlocks = new LinkedList();

    /** Entities impacted by gas */
    private final HashMap<LivingEntity, Integer> impactedEntityMap = new HashMap();
    //TODO turn into entity capability to prevent damage stacking of several explosives
    //TODO use weak refs to not hold instances

    private double sizePercentageOverTime(int timePassed)
    {
        return Math.min(1, 2f * timePassed / duration + 0.1f);
    }

    @Override
    public boolean doExplode(int callCount)
    {
        //Play start audio
        if (callCount == 0 && !this.playShortSoundFX)
        {
            //TODO look into different sounds per type
            ICBMSounds.DEBILITATION.play(world,
                getPosition().x, getPosition().y, getPosition().z,
                4.0F, (1.0F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }

        //Do gas effect
        if (callCount % TICKS_BETWEEN_RUNS == 0)
        {
            setEffectBoundsAndSpawnParticles(this.callCount); // recalculate the affected blocks (where particles spawn, poison is applied, etc.)

            //Trigger effects for user feedback
            generateAudioEffect();

            //Only run potion effect application for the following types
            if (canEffectEntities())
            {
                final double radius = this.getBlastRadius();

                //Max bounds
                final AxisAlignedBB bounds = new AxisAlignedBB(
                    getPosition().x - radius, getPosition().y - radius, getPosition().z - radius,
                    getPosition().x + radius, getPosition().y + radius, getPosition().z + radius);

                final List<LivingEntity> entityList = getWorld()
                        .getEntitiesWithinAABB(LivingEntity.class, bounds, this::canGasEffect);

                //Loop all entities
                for (LivingEntity entity : entityList)
                {
                    final float protection = getProtectionRating(entity);
                    if(protection < minGasProtection() || protection < world.rand.nextFloat()) {
                        //Track entities
                        if (!impactedEntityMap.containsKey(entity)) {
                            impactedEntityMap.put(entity, 1);
                        } else {
                            impactedEntityMap.replace(entity, impactedEntityMap.get(entity) + 1);
                        }

                        //Scale damage with hit count
                        final int hitCount = impactedEntityMap.get(entity);

                        //Apply effects
                        applyEffect(entity, hitCount);
                    }
                }
            }

            //End explosion when we hit life timer
            return this.callCount > this.duration;
        }

        return false;
    }

    protected float minGasProtection() {
        return 0.5f;
    }

    protected float getProtectionRating(LivingEntity entityLivingBase) {
        return ProtectiveArmorHandler.getProtectionRating(entityLivingBase);
    }

    /**
     * Checks if this gas explosion wants to apply effects to entities directly
     *
     * @return true to run effects
     */
    protected abstract boolean canEffectEntities();

    /**
     * Called to apply effects to entities
     *
     * @param entity   to impact
     * @param hitCount level of impact
     */
    protected void applyEffect(final LivingEntity entity, final int hitCount)
    {

    }

    /**
     * Checking that the entity can be harmed or an effect can be applied
     *
     * @param entity
     * @return true to effect entity
     */
    protected boolean canGasEffect(LivingEntity entity)
    {
        //Ignore dead things
        if (entity.isAlive())
        {
            //Always ignore non-gameplay characters
            if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isCreative())
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
            ICBMSounds.GAS_LEAK.play(world, getPosition().x + 0.5D, getPosition().y + 0.5D, getPosition().z + 0.5D,
                    4.0F, (1.0F + (getWorld().rand.nextFloat() - getWorld().rand.nextFloat()) * 0.2F), true);
        }
    }

    private void setEffectBoundsAndSpawnParticles(int timePassed) //TODO move to pathfinder object for reuse
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
            affectedBlocks.add(new BlockPos(getPosition()));
            edgeBlocks.add(new BlockPos(getPosition()));
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
            for (Direction facing : Direction.values())
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
                        if (isValidPath(checkPos, facing))
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

    private boolean isValidPath(final BlockPos pos, final Direction direction)
    {
        final BlockState blockState = world.getBlockState(pos);
        final VoxelShape voxelShape = blockState.getCollisionShape(world, pos);
        if (voxelShape.isEmpty()) // if there is no bounding box, its pass through, so its a valid path
        {
            return true;
        }

        //TODO find a way to better detect using some outline calculation

        // whether the respective axes are completely stretched (they span form one side of the block to another)
        boolean xFull = voxelShape.getStart(Direction.Axis.X) <= 0.001 && voxelShape.getEnd(Direction.Axis.X) >= 0.999;
        boolean yFull = voxelShape.getStart(Direction.Axis.Y) <= 0.001 && voxelShape.getEnd(Direction.Axis.Y) >= 0.999;
        boolean zFull = voxelShape.getStart(Direction.Axis.Z) <= 0.001 && voxelShape.getEnd(Direction.Axis.Z) >= 0.999;

        boolean isImpassable = false;
        if (direction == Direction.UP || direction == Direction.DOWN)
        {
            isImpassable = xFull && zFull;
        }
        else if (direction == Direction.NORTH || direction == Direction.SOUTH)
        {
            isImpassable = xFull && yFull;
        }
        else if (direction == Direction.WEST || direction == Direction.EAST)
        {
            isImpassable = zFull && yFull;
        }

        return !isImpassable;
    }

    private boolean isInRange(final Vec3i pos, final int radiusSq)
    {
        return (int) Math.floor(pos.distanceSq(Math.floor(getPosition().x), Math.floor(getPosition().y), Math.floor(getPosition().z), true)) <= radiusSq;
    }

    protected void spawnGasParticles(final Vec3i pos)
    {
        /*ICBMClassic.proxy.spawnAirParticle(world,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                (Math.random() - 0.5) / 2,
                (Math.random() - 0.5) / 2 - 0.1,
                (Math.random() - 0.5) / 2,
                getParticleColorRed(pos),
                getParticleColorGreen(pos),
                getParticleColorBlue(pos),
                7.0F, duration);*/
    }

    protected float getParticleColorRed(final Vec3i pos)
    {
        return (float) Math.random();
    }

    protected float getParticleColorGreen(final Vec3i pos)
    {
        return (float) Math.random();
    }

    protected float getParticleColorBlue(final Vec3i pos)
    {
        return (float) Math.random();
    }

    @Override
    public void load(CompoundNBT nbt)
    {
        super.load(nbt);
        this.duration = nbt.getInt(NBTConstants.DURATION);
        this.playShortSoundFX = nbt.getBoolean(NBTConstants.PLAY_SHORT_SOUND_FX);
    }

    @Override
    public void save(CompoundNBT nbt)
    {
        super.save(nbt);
        nbt.putInt(NBTConstants.DURATION, this.duration);
        nbt.putBoolean(NBTConstants.PLAY_SHORT_SOUND_FX, this.playShortSoundFX);
    }
}
