package icbm.classic.content.blast.redmatter;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastIgnore;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.BlastHelpers;
import icbm.classic.content.blast.helpers.BlastBlockHelpers;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.lib.CalculationHelpers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * Handles logic of the redmatter tick
 */
public class RedmatterLogic
{
    public static float MASS_REDUCTION_SCALE = 0.98f; //TODO config
    public static float MINIMAL_SIZE = 0.25f; //TODO config

    //Lag tracking
    private int blocksDestroyedThisTick = 0;
    private int currentBlockDestroyRadius = 1;

    //Host of the logic
    private final EntityRedmatter host;

    public RedmatterLogic(EntityRedmatter host)
    {
        this.host = host;
    }

    public float getScaleFactor() //TODO move to field and calculate only when size changes
    {
        return host.getBlastSize() / ConfigBlast.REDMATTER.NORMAL_RADIUS;
    }

    /**
     * Check how many blocks can be edited per tick
     *
     * @return blocks that can be removed each tick
     */
    public int getBlocksPerTick() //TODO move to field and calculate only when size changes
    {
        return (int) Math.min(ConfigBlast.REDMATTER.MAX_BLOCKS_EDITS_PER_TICK, ConfigBlast.REDMATTER.DEFAULT_BLOCK_EDITS_PER_TICK * getScaleFactor());
    }

    public void tick()
    {
        preTick();
        doTick();
        postTick();
    }

    protected void preTick()
    {
        blocksDestroyedThisTick = 0;
    }

    protected void doTick()
    {
        //Do actions
        doDestroyBlocks();
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

    protected void postTick()
    {
        //Decrease block if we don't destroy anything
        if (blocksDestroyedThisTick <= 0)
        {
            decreaseScale();
        }
    }

    protected void decreaseScale()
    {
        if (host.getBlastSize() <= MINIMAL_SIZE)
        {
            host.setBlastSize(0);
            host.setDead();
        }
        else
        {
            //Decrease mass
            host.setBlastSize(host.getBlastSize() * MASS_REDUCTION_SCALE);

            if (host.getBlastSize() < 50) // evaporation speedup for small black holes
            {
                host.setBlastSize((50 - host.getBlastSize()) / 100);//TODO magic numbers & config
            }
        }

    }

    protected void doDestroyBlocks()
    {
        //Destroy blocks in radius
        BlastHelpers.forEachPosInRadiusUntil(currentBlockDestroyRadius,
                (x, y, z) -> {
                    processNextBlock(x, y, z);
                    return true;
                },
                this::shouldStopBreakingBlocks);

        //If we didn't destroy anything at this layer expand
        if (blocksDestroyedThisTick <= 0)
        {
            currentBlockDestroyRadius += 1;

            //Reset if we reach radius
            if (currentBlockDestroyRadius >= host.getBlastSize())
            {
                currentBlockDestroyRadius = 1;
            }
        }
    }

    /**
     * Checks to see if we should stop looping while breaking blocks
     *
     * @return true to stop
     */
    protected boolean shouldStopBreakingBlocks()
    {
        return blocksDestroyedThisTick > getBlocksPerTick()
                || host.isDead;
    }

    /**
     * Process the next block from looping
     *
     * @param rx - relative from center
     * @param ry - relative from center
     * @param rz - relative from center
     */
    protected void processNextBlock(int rx, int ry, int rz)
    {
        final BlockPos blockPos = new BlockPos(
                rx + Math.floor(host.posX),
                ry + Math.floor(host.posY),
                rz + Math.floor(host.posZ)
        ); //TODO use mutable pos for performance
        final double dist = host.getDistanceSq(blockPos);

        //We are looping in a shell orbit around the center
        if (dist < this.currentBlockDestroyRadius && dist > this.currentBlockDestroyRadius - 2)
        {
            final IBlockState blockState = host.world.getBlockState(blockPos);
            if (shouldRemoveBlock(blockPos, blockState)) //TODO calculate a pressure or pull force to destroy weaker blocks before stronger blocks
            {
                //TODO handle multi-blocks

                host.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), BlastBlockHelpers.isFluid(blockState) ? 2 : 3);
                //TODO: render fluid streams moving into hole

                //Convert a random amount of destroyed blocks into flying blocks for visuals
                if (canTurnIntoFlyingBlock(blockState) && host.world.rand.nextFloat() > ConfigBlast.REDMATTER.CHANCE_FOR_FLYING_BLOCK)
                {
                    spawnFlyingBlock(blockPos, blockState);
                }
                blocksDestroyedThisTick++;
            }
        }
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
        host.world.spawnEntity(entity);

        this.handleEntities(entity); //TODO why? this should just be an apply velocity call
    }

    protected void doEntityEffects()
    {
        final float entityRadius = host.getBlastSize() * 1.5f; //TODO why 1.5?

        final AxisAlignedBB bounds = host.getEntityBoundingBox().expand(entityRadius, entityRadius, entityRadius);

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
    protected void handleEntities(Entity entity) //TODO why is radius and doExplosion not used
    {
        //Calculate different from center
        final double xDifference = entity.posX - host.posX;
        final double yDifference = entity.posY - host.posY;
        final double zDifference = entity.posZ - host.posZ;

        final double distance = host.getDistanceSq(entity); //TODO switch to Sq version for performance

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

        //Calculate velocity
        final double velX = -xDifference / distance / distance * 5; //TODO what is 5?
        final double velY = -yDifference / distance / distance * 5;
        final double velZ = -zDifference / distance / distance * 5;

        // Gravity Velocity
        entity.addVelocity(velX, velY, velZ);

        // if player send packet because motion is handled client side
        if (entity instanceof EntityPlayer)
        {
            entity.velocityChanged = true;
        }

        return true;
    }

    private void attackEntity(Entity entity, double distance)
    {
        //TODO move each section to capability or reg system
        //TODO make config driven, break section out into its own method

        //Handle eating logic
        if (distance < (ConfigBlast.REDMATTER.ENTITY_DESTROY_RADIUS * getScaleFactor()))
        {
            if (entity instanceof EntityRedmatter && !entity.isDead)
            {
                //https://www.wolframalpha.com/input/?i=(4%2F3)pi+*+r%5E3+%3D+(4%2F3)pi+*+a%5E3+%2B+(4%2F3)pi+*+b%5E3

                //We are going to merge both blasts together
                final double selfRad = Math.pow(host.getBlastSize(), 3);
                final double targetRad = Math.pow(((EntityExplosion) entity).getBlast().getBlastRadius(), 3);

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
                if (blast instanceof BlastAntimatter) //TODO move to capability... also check if this is even valid
                {
                    if (ConfigBlast.REDMATTER.ENABLE_AUDIO)
                    {
                        ICBMSounds.EXPLOSION.play(host.world, host.posX, host.posY, host.posZ, 7.0F, CalculationHelpers.randFloatRange(host.world.rand, -0.6F, 0.9F), true);
                    }
                    if (host.world.rand.nextFloat() > 0.85 && !host.world.isRemote) //TODO config for this float... why a chance?
                    {
                        //Destroy self
                        host.setDead();
                    }
                }

                //Kill the blast
                blast.clearBlast();
            }
            else if (entity instanceof EntityMissile) //TODO move to capability
            {
                ((EntityMissile) entity).doExplosion(); //TODO should trigger the explosive capability
            }
            else if (entity instanceof EntityExplosive) //TODO move to capability
            {
                ((EntityExplosive) entity).explode(); //TODO should trigger the explosive capability
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
                    if (host.getBlastSize() < 120)
                    {
                        host.setBlastSize(host.getBlastSize() + 0.05f); //TODO magic number and config
                    }
                }
            }
        }
    }
}
