package icbm.classic.content.blast.redmatter.logic;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastIgnore;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.helpers.BlastBlockHelpers;
import icbm.classic.content.blast.redmatter.DamageSourceRedmatter;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.explosive.ExplosiveHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Handles logic of the redmatter tick
 */
public class RedmatterLogic
{
    //Host of the logic
    public final EntityRedmatter host;

    /** Blocks destroyed in the current size cycle... cycle can last few ticks to several minutes */
    protected int blockDestroyedThisCycle = 0;
    /** Blocks destroyed this tick, used to limit max world edits */
    protected int blockDestroyedThisTick = 0;
    /** Raytrace lines run to detect blocks this tick, used to limit CPU usage */
    protected int raytracesThisTick = 0;
    /** Cycles run since we removed blocks, used to track starve rate */
    protected int cyclesSinceLastBlockRemoved = -1;
    /** Current scan radius for blocks */
    protected int currentBlockDestroyRadius = -1;

    /** Queue of raytraces to run for searching blocks, this defaults to edge blocks only */
    protected final Queue<BlockPos> rayTraceTargets = new LinkedList();

    /**
     * @param host - redmatter entity running this logic
     */
    public RedmatterLogic(EntityRedmatter host)
    {
        this.host = host;
    }

    /**
     * Check how many blocks can be edited per tick
     *
     * @return blocks that can be removed each tick
     */
    public int getBlocksPerTick()
    {
        return ConfigBlast.REDMATTER.MAX_BLOCKS_EDITS_PER_TICK;
    }

    /** Invoked each tick by the controlling entity */
    public void tick()
    {
        preTick();
        doTick();
        postTick();
    }

    /** Prep cycle to cleanup from last tick */
    protected void preTick()
    {
        raytracesThisTick = 0;
        blockDestroyedThisTick = 0;

        //Init destroy radius based on host's saved value
        if (currentBlockDestroyRadius < 0)
        {
            currentBlockDestroyRadius = (int) Math.floor(host.getBlastSize());
        }
    }

    /** Actual work cycle */
    protected void doTick()
    {
        //Do actions
        detectAndDestroyBlocks();
        doEntityEffects();

        //Play effects
        if (ConfigBlast.REDMATTER.ENABLE_AUDIO)
        {
            //TODO collapse audio should play near blocks destroyed for better effect
            if (host.world.rand.nextInt(8) == 0)
            {
                final double playX = host.posX + CalculationHelpers.randFloatRange(host.world.rand, host.getBlastSize());
                final double playY = host.posY + CalculationHelpers.randFloatRange(host.world.rand, host.getBlastSize());
                final double playZ = host.posZ + CalculationHelpers.randFloatRange(host.world.rand, host.getBlastSize());
                final float volume = CalculationHelpers.randFloatRange(host.world.rand, 5, 6);
                final float pitch = CalculationHelpers.randFloatRange(host.world.rand, 0.6f, 1);
                ICBMSounds.COLLAPSE.play(host.world, playX, playY, playZ, volume, pitch, true);
            }
            //TODO check if this should play every tick
            ICBMSounds.REDMATTER.play(host.world, host.posX, host.posY, host.posZ, 3.0F, CalculationHelpers.randFloatRange(host.world.rand, -0.8f, 1.2f), true);
        }
    }

    /** Post work cycle */
    protected void postTick()
    {
        //Decrease block if we don't destroy anything
        if (blockDestroyedThisCycle <= 0)
        {
            decreaseScale();
        }
    }

    /** Handles decreasing the size of the redmatter */
    protected void decreaseScale()
    {
        final float size = host.getBlastSize();

        //We are not removing blocks so the redmatter is starving (has no blocks to remove), decrease size
        if (size <= cyclesSinceLastBlockRemoved)
        {
            //TODO make it optional to remove small redmatters. This way we can leave land marks were old redmatter exist
            //TODO if we leave small redmatters allow players to remove them and/or capture in jars
            if (size <= ConfigBlast.REDMATTER.MIN_SIZE)
            {
                host.setBlastSize(0);
                host.setDead();
                ICBMClassic.logger().info("Redmatter[{}] has starved to death at {} {} {} {}",
                        host.getEntityId(),
                        host.posX,
                        host.posY,
                        host.posZ,
                        host.world.provider.getDimension());
            }
            else
            //Decrease mass
            {
                final float newSize = size < 1 ? size * 0.9f : size * ConfigBlast.REDMATTER.STARVE_SCALE;
                host.setBlastSize(newSize);
            }
        }
    }

    /** Handles looking for blocks and starting the destroy process */
    protected void detectAndDestroyBlocks()
    {
        //Match blast size it if changes
        if (currentBlockDestroyRadius > host.getBlastMaxSize())
        {
            setCurrentBlockDestroyRadius((int) Math.floor(host.getBlastSize()));
        }

        //Init stage
        if (rayTraceTargets.isEmpty())
        {
            startNextBlockDestroyCycle();
        }

        //Destroy blocks until we are told to stop
        cycleDestroyBlocks();
    }

    /** Detects blocks and removes them */
    protected void cycleDestroyBlocks()
    {
        //Loop targets and trace limit per tick
        final Vec3d center = host.getPositionVector();
        while (!shouldStopBreakingBlocks() && !rayTraceTargets.isEmpty())
        {
            raytracesThisTick++;
            rayTraceTowardsBlock(center, rayTraceTargets.poll());
        }
    }

    /** Triggers the next block cycle finding all raytrace paths */
    protected void startNextBlockDestroyCycle()
    {
        //Increase size
        if (cyclesSinceLastBlockRemoved > 0)
        {
            cyclesSinceLastBlockRemoved = 0;
            setCurrentBlockDestroyRadius(currentBlockDestroyRadius + 1);  //TODO change scale to number of blocks eaten
        }

        //Ensure we are empty to avoid memory overflow or duplicate data
        rayTraceTargets.clear();

        //Collect positions
        RedmatterBlockCollector.collectBlocksOnWallEdges(currentBlockDestroyRadius, (rx, ry, rz) -> {
            //TODO implement flywheel pattern
            rayTraceTargets.offer(new BlockPos(rx, ry, rz));
        });

        //Randomize ray order
        Collections.shuffle((List<?>) rayTraceTargets);

        //If we didn't destroy anything at this layer expand
        if (blockDestroyedThisCycle <= 0)
        {
            cyclesSinceLastBlockRemoved += 1;
        }
        blockDestroyedThisCycle = 0;
    }

    /**
     * Raytraces towards a target position looking for blocks
     * <p>
     * This will not remove all blocks in the path. Instead it kills the first
     * block it finds allowing for a repeat cycle of block removal in the path
     * over several ticks.
     *
     * @param center - redmatter center, passed in to avoid recreating
     * @param target - block target to trace towards
     */
    protected void rayTraceTowardsBlock(final Vec3d center, final BlockPos target)
    {
        //Build target position
        final double targetX = target.getX() + center.x;
        final double targetY = target.getY() + center.y;
        final double targetZ = target.getZ() + center.z;
        final Vec3d pos = new Vec3d(targetX, targetY, targetZ);

        //Raytrace towards block
        final RayTraceResult rayTrace = host.world.rayTraceBlocks(center, pos, true, false, false);

        if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK)
        {
            processNextBlock(rayTrace.getBlockPos());
        }
    }

    /**
     * Checks to see if we should stop looping while breaking blocks
     *
     * @return true to stop
     */
    protected boolean shouldStopBreakingBlocks()
    {
        return raytracesThisTick > ConfigBlast.REDMATTER.DEFAULT_BLOCK_RAYTRACE_PER_TICK
                || blockDestroyedThisTick > getBlocksPerTick()
                || host.isDead;
    }

    /**
     * Process the next block from looping
     *
     * @param blockPos - blockToEdit
     */
    protected void processNextBlock(BlockPos blockPos)
    {
        final double dist = MathHelper.sqrt(host.getDistanceSqToCenter(blockPos));

        //We are looping in a shell orbit around the center
        if (dist < (this.currentBlockDestroyRadius + 1))
        {
            final IBlockState blockState = host.world.getBlockState(blockPos);
            if (shouldRemoveBlock(blockPos, blockState)) //TODO calculate a pressure or pull force to destroy weaker blocks before stronger blocks
            {
                //TODO handle multi-blocks
                //TODO: render fluid streams moving into hole

                if (host.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3))
                {
                    //Freeze fluid blocks to improve pull rate
                    if (blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.FLOWING_WATER)
                    {
                        freezeWaterAround(blockPos);
                    }

                    //Convert a random amount of destroyed blocks into flying blocks for visuals
                    if (canTurnIntoFlyingBlock(blockState) && host.world.rand.nextFloat() > ConfigBlast.REDMATTER.CHANCE_FOR_FLYING_BLOCK)
                    {
                        spawnFlyingBlock(blockPos, blockState);
                    }
                    markBlockRemoved();
                }
            }
        }
    }

    private void freezeWaterAround(BlockPos pos) //TODO convert to map<Block, Action> to allow introducing more effects
    {
        for (EnumFacing side : EnumFacing.values())
        {
            final BlockPos blockPos = pos.add(side.getDirectionVec());
            final IBlockState blockState = host.world.getBlockState(blockPos);
            if (blockState.getBlock() == Blocks.WATER || blockState.getBlock() == Blocks.FLOWING_WATER)
            {
                host.world.setBlockState(blockPos, Blocks.ICE.getDefaultState(), 3); //TODO turn into fake ice that melts randomly
            }
        }
    }

    private void markBlockRemoved()
    {
        blockDestroyedThisCycle++; //Tracks blocks removed over several ticks
        blockDestroyedThisTick++; //Tracks blocks removed in a single tick
        cyclesSinceLastBlockRemoved = 0;
    }

    /**
     * Checks to see if we should remove the block
     *
     * @param blockPos   - position of the block in the world
     * @param blockState - state of the block
     * @return true to remove
     */
    protected boolean shouldRemoveBlock(BlockPos blockPos, IBlockState blockState)
    {
        final Block block = blockState.getBlock();
        final boolean isFluid = BlastBlockHelpers.isFluid(blockState);
        //Ignore air blocks and unbreakable blocks
        return !block.isAir(blockState, host.world, blockPos)
                && (BlastBlockHelpers.isFlowingWater(blockState) || !isFluid && blockState.getBlockHardness(host.world, blockPos) >= 0);

    }

    protected boolean canTurnIntoFlyingBlock(IBlockState blockState)
    {
        return ConfigBlast.REDMATTER.SPAWN_FLYING_BLOCKS && !BlastBlockHelpers.isFluid(blockState);
    }

    protected void spawnFlyingBlock(BlockPos blockPos, IBlockState blockState)
    {
        final EntityFlyingBlock entity = new EntityFlyingBlock(host.world, blockPos, blockState);
        entity.yawChange = 50 * host.world.rand.nextFloat();
        entity.pitchChange = 50 * host.world.rand.nextFloat();
        entity.noClip = true;
        host.world.spawnEntity(entity);

        this.handleEntities(entity); //TODO why? this should just be an apply velocity call
    }

    private float getEntityImpactRange()
    {
        return host.getBlastSize() * ConfigBlast.REDMATTER.GRAVITY_SCALE;
    }

    protected void doEntityEffects()
    {
        final float entityRadius = getEntityImpactRange();

        final AxisAlignedBB bounds = new AxisAlignedBB(
                host.posX - entityRadius,
                host.posY - entityRadius,
                host.posZ - entityRadius,
                host.posX + entityRadius,
                host.posY + entityRadius,
                host.posZ + entityRadius);

        //Get all entities in the cube area
        host.world.getEntitiesWithinAABB(Entity.class, bounds)
                //Filter down
                .stream().filter(this::shouldHandleEntity)
                //Apply to each
                .forEach(this::handleEntities);
    }

    private boolean shouldHandleEntity(Entity entity)
    {
        //Ignore self
        if (entity == host)
        {
            return false;
        }

        //Ignore players that are in creative mode or can't be harmed TODO may need to check for spectator?
        if (entity instanceof EntityPlayer && (((EntityPlayer) entity).capabilities.isCreativeMode || ((EntityPlayer) entity).capabilities.disableDamage))
        {
            return false;
        }

        //Ignore entities that mark themselves are ignorable
        if (entity instanceof IBlastIgnore && ((IBlastIgnore) entity).canIgnore(host.blastData)) //TODO remove
        {
            return false;
        }

        return true;
    }

    /**
     * Makes an entity get affected by Red Matter.
     */
    protected void handleEntities(Entity entity)
    {
        //Calculate different from center
        final double xDifference = host.posX - entity.posX;
        final double yDifference = host.posY - entity.posY;
        final double zDifference = host.posZ - entity.posZ;

        final double distance = host.getDistance(entity);

        moveEntity(entity, xDifference, yDifference, zDifference, distance);
        attackEntity(entity, distance);
    }

    private boolean moveEntity(Entity entity, double xDifference, double yDifference, double zDifference, double distance)
    {
        //Allow overriding default pull logic
        final IBlastVelocity cap = entity.getCapability(ICBMClassicAPI.BLAST_VELOCITY_CAPABILITY, null);
        if (cap != null && cap.onBlastApplyMotion(host, host.blastData, xDifference, yDifference, zDifference, distance))
        {
            return true;
        }

        final double distanceScale = Math.max(0, 1 - (distance / getEntityImpactRange())); //0.0 to 1.0
        final double pullPower = Math.min(1, distanceScale * host.getBlastSize() * 0.01);

        //Calculate velocity (delta / mag) * power
        final double velX = (xDifference / distance) * pullPower;
        final double velY = (yDifference / distance) * pullPower;
        final double velZ = (zDifference / distance) * pullPower;

        // Gravity Velocity
        entity.addVelocity(velX, velY, velZ); //Negative pulls it towards the redmatter
        entity.velocityChanged = true;

        return true;
    }

    private void attackEntity(Entity entity, double distance)
    {
        //TODO move each section to capability or reg system
        //TODO make config driven, break section out into its own method

        //Handle eating logic
        final double attackRange = Math.max(1, ConfigBlast.REDMATTER.KILL_SCALE * host.getBlastSize());
        if (distance < attackRange)
        {
            if (entity instanceof EntityRedmatter && !entity.isDead)
            {
                //https://www.wolframalpha.com/input/?i=(4%2F3)pi+*+r%5E3+%3D+(4%2F3)pi+*+a%5E3+%2B+(4%2F3)pi+*+b%5E3

                //We are going to merge both blasts together
                final double selfRad = Math.pow(host.getBlastSize(), 3);
                final double targetRad = Math.pow(((EntityRedmatter) entity).getBlastSize(), 3);

                final float newRad = (float) Math.cbrt(selfRad + targetRad); //TODO why cube?

                host.setBlastSize(newRad);

                //TODO combine the vectors instead of turning into zero
                host.motionX = 0;
                host.motionY = 0;
                host.motionZ = 0;

                //TODO fire an event when combined (non-cancelable to allow acting on combined result)
            }
            else if (entity instanceof EntityExplosion)
            {
                final IBlast blast = ((EntityExplosion) entity).getBlast();

                //Kill the blast
                blast.clearBlast();
            }
            else if (entity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null))
            {
                final IExplosive explosive = entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
                ExplosiveHandler.createExplosion(host, entity.world, entity.posX, entity.posY, entity.posZ, explosive);
            }
            else if (entity instanceof EntityLiving || entity instanceof EntityPlayer)
            {
                entity.attackEntityFrom(new DamageSourceRedmatter(this), 2000);
            }
            else
            {
                //Kill entity in the center of the ball
                entity.setDead();
                if (entity instanceof EntityFlyingBlock)
                {
                    if (host.getBlastSize() < host.getBlastMaxSize())
                    {
                        host.setBlastSize(host.getBlastSize() + 0.05f); //TODO magic number and config
                    }
                }
            }
        }
    }

    public void setCurrentBlockDestroyRadius(int size)
    {
        this.currentBlockDestroyRadius = (int) Math.max(1, Math.min(size, host.getBlastMaxSize()));
        this.host.setBlastSize(size);
    }
}
