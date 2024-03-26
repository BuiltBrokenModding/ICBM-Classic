package icbm.classic.world.effect.gas;

import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.core.particles.IcbmParticleOptions;
import icbm.classic.lib.NBTConstants;
import icbm.classic.world.IcbmParticleTypes;
import icbm.classic.world.blast.Blast;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.*;

public abstract class BlastGasBase extends Blast implements IBlastTickable {
    /**
     * Delay between running calculations
     */
    private static final int TICKS_BETWEEN_RUNS = 5;

    /**
     * Mutable block pos for pathing
     */
    private static final BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

    /**
     * Duration of gas effect, also controls particles
     */
    protected int duration;

    private boolean playShortSoundFX;

    private int lastRadius = 0;

    /**
     * Blocks pathed
     */
    private final HashSet<BlockPos> affectedBlocks = new HashSet();
    /**
     * Queue of edge blocks to path
     */
    private final Queue<BlockPos> edgeBlocks = new LinkedList();

    /**
     * Entities impacted by gas
     */
    private final HashMap<EntityLivingBase, Integer> impactedEntityMap = new HashMap();
    //TODO turn into entity capability to prevent damage stacking of several explosives
    //TODO use weak refs to not hold instances

    public BlastGasBase(int duration, boolean playShortSoundFX) {
        this.duration = duration;
        this.playShortSoundFX = playShortSoundFX;
    }

    private double sizePercentageOverTime(int timePassed) {
        return Math.min(1, 2f * timePassed / duration + 0.1f);
    }

    @Override
    public boolean doExplode(int callCount) {
        //Play start audio
        if (callCount == 0 && !this.playShortSoundFX) {
            //TODO look into different sounds per type
            ICBMSounds.DEBILITATION.play(world, this.location.x(), this.location.y(), this.location.z(), 4.0F, (1.0F + (level().rand.nextFloat() - level().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }

        //Do gas effect
        if (callCount % TICKS_BETWEEN_RUNS == 0) {
            setEffectBoundsAndSpawnParticles(this.callCount); // recalculate the affected blocks (where particles spawn, poison is applied, etc.)

            //Trigger effects for user feedback
            generateAudioEffect();

            //Only run potion effect application for the following types
            if (canEffectEntities()) {
                final double radius = this.getBlastRadius();

                //Max bounds
                final AxisAlignedBB bounds = new AxisAlignedBB(
                    location.x() - radius, location.y() - radius, location.z() - radius,
                    location.x() + radius, location.y() + radius, location.z() + radius);

                final List<EntityLivingBase> entityList = level()
                    .getEntitiesWithinAABB(EntityLivingBase.class, bounds, this::canGasEffect);

                //Loop all entities
                for (LivingEntity entity : entityList) {
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

            //End explosion when we hit life timer
            return this.callCount > this.duration;
        }

        return false;
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
    protected void applyEffect(final EntityLivingBase entity, final int hitCount) {

    }

    /**
     * Checking that the entity can be harmed or an effect can be applied
     *
     * @param entity
     * @return true to effect entity
     */
    protected boolean canGasEffect(EntityLivingBase entity) {
        //Ignore dead things
        if (entity.isAlive()) {
            //Always ignore non-gameplay characters
            if (entity instanceof Player && ((Player) entity).isCreative()) {
                return false;
            }

            //Check that the entity is in range
            return affectedBlocks.contains(checkPos.setPos(entity.getX(), entity.getY(), entity.getZ()));
        }
        return false;
    }

    private void generateAudioEffect() {
        if (this.playShortSoundFX) {
            ICBMSounds.GAS_LEAK.play(world, location.x() + 0.5D, location.y() + 0.5D, location.z() + 0.5D,
                4.0F, (1.0F + (level().rand.nextFloat() - level().rand.nextFloat()) * 0.2F) * 1F, true);
        }
    }

    private void setEffectBoundsAndSpawnParticles(int timePassed) //TODO move to pathfinder object for reuse
    {
        final int maxSize = (int) Math.ceil(this.getBlastRadius());
        //Get and validate radius
        final int radius = (int) Math.floor(maxSize * sizePercentageOverTime(timePassed));
        if (lastRadius == radius) {
            return;
        }
        lastRadius = radius;

        //Get radius sq for distance checks
        final int currentDistanceSQ = radius * radius;

        //Init path data
        if (affectedBlocks.isEmpty()) {
            affectedBlocks.add(getBlockPos());
            edgeBlocks.add(getBlockPos());
        }

        if (edgeBlocks.isEmpty()) {
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
        while (edgeBlocks.peek() != null) {
            //Current edge block
            final BlockPos edge = edgeBlocks.poll();

            //Loop all 6 sides of the edge
            for (Direction facing : Direction.values()) {
                //Move our check pos to current target
                checkPos.setPos(edge);
                checkPos.move(facing);

                //Don't repath
                if (!hasPathed.contains(checkPos) && !affectedBlocks.contains(checkPos)) {
                    //Check that it is in range
                    if (isInRange(checkPos, currentDistanceSQ)) {
                        //Validate
                        if (isValidPath(checkPos, facing)) {
                            final BlockPos pos = checkPos.toImmutable();
                            affectedBlocks.add(pos);
                            nextSet.add(pos);

                            spawnGasParticles(pos);
                        }
                        //Ignore if invalid
                        else {
                            hasPathed.add(checkPos.toImmutable());
                        }
                    }
                }
            }
        }

        //Add next set to follow up queue
        edgeBlocks.addAll(nextSet);
    }

    private boolean isValidPath(final BlockPos pos, final Direction direction) {
        final BlockState blockState = world.getBlockState(pos);
        final AxisAlignedBB aabb = blockState.getCollisionBoundingBox(world, pos);
        if (aabb == null) // if there is no bounding box, its pass through, so its a valid path
        {
            return true;
        }

        // whether or not the respective axes are completely stretched (they span form one side of the block to another)
        boolean xFull = aabb.minX == 0 && aabb.maxX == 1;
        boolean yFull = aabb.minY == 0 && aabb.maxY == 1;
        boolean zFull = aabb.minZ == 0 && aabb.maxZ == 1;

        boolean isImpassable = false;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            isImpassable = xFull && zFull;
        } else if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            isImpassable = xFull && yFull;
        } else if (direction == Direction.WEST || direction == Direction.EAST) {
            isImpassable = zFull && yFull;
        }

        return !isImpassable;
    }

    private boolean isInRange(final Vec3i pos, final int radiusSq) {
        return (int) Math.floor(pos.distanceSq(xi(), yi(), zi())) <= radiusSq;
    }

    protected void spawnGasParticles(final Vec3i pos) {
        level().addParticle(
            new IcbmParticleOptions(
                IcbmParticleTypes.STALE_SMOKE.get(),
                new Vector3f(
                    getParticleColorRed(pos),
                    getParticleColorGreen(pos),
                    getParticleColorBlue(pos)
                ),
                7.0F,
                duration
            ),
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            (Math.random() - 0.5) / 2,
            (Math.random() - 0.5) / 2 - 0.1,
            (Math.random() - 0.5) / 2
        );
    }

    protected float getParticleColorRed(final Vec3i pos) {
        return (float) Math.random();
    }

    protected float getParticleColorGreen(final Vec3i pos) {
        return (float) Math.random();
    }

    protected float getParticleColorBlue(final Vec3i pos) {
        return (float) Math.random();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        this.duration = nbt.getInteger(NBTConstants.DURATION);
        this.playShortSoundFX = nbt.getBoolean(NBTConstants.PLAY_SHORT_SOUND_FX);
    }

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);
        nbt.setInteger(NBTConstants.DURATION, this.duration);
        nbt.setBoolean(NBTConstants.PLAY_SHORT_SOUND_FX, this.playShortSoundFX);
    }
}
