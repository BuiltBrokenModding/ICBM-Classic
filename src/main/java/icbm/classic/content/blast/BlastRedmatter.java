package icbm.classic.content.blast;

import icbm.classic.api.events.BlastBlockModifyEvent;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastIgnore;
import icbm.classic.api.explosion.IBlastMovable;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.network.packet.PacketRedmatterSizeSync;
import icbm.classic.lib.transform.region.Cube;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.IFluidBlock;

public class BlastRedmatter extends Blast implements IBlastTickable, IBlastMovable
{
    //client side value
    public float clientSize = 0.0F;

    //Lag tracking
    private int blocksDestroyedThisTick = 0;
    private long tickStartTimeMs;
    private int currentBlockDestroyRadius = 1;

    public float getScaleFactor() //TODO move to field and calculate only when size changes
    {
        return size / ConfigBlast.REDMATTER.NORMAL_RADIUS;
    }

    public float getScaleFactorClient() //TODO move to field and calculate only when size changes
    {
        return clientSize / ConfigBlast.REDMATTER.NORMAL_RADIUS;
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

    /**
     * Triggered from the render to smoothly change size with each frame update
     *
     * @param deltaTick percentage of time passed (0.0 - 1.0f)
     */
    public void lerpSize(float deltaTick)
    {
        clientSize = clientSize + deltaTick * (size - clientSize);
    }

    @Override
    public boolean doExplode(int callCount)
    {
        preTick();
        doTick();
        postTick();
        return ConfigBlast.REDMATTER.DO_DESPAWN && callCount >= ConfigBlast.REDMATTER.MAX_LIFESPAN || this.size <= 0;
    }

    protected void preTick()
    {
        tickStartTimeMs = System.currentTimeMillis();
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
            if (this.world().rand.nextInt(8) == 0)
            {
                final double playX = location.x() + CalculationHelpers.randFloatRange(world().rand, getBlastRadius());
                final double playY = location.y() + CalculationHelpers.randFloatRange(world().rand, getBlastRadius());
                final double playZ = location.z() + CalculationHelpers.randFloatRange(world().rand, getBlastRadius());
                final float volume = CalculationHelpers.randFloatRange(world().rand, 5, 6);
                final float pitch = CalculationHelpers.randFloatRange(world().rand, 0.6f, 1);
                ICBMSounds.COLLAPSE.play(world, playX, playY, playZ, volume, pitch, true);
            }
            //TODO check if this should play every tick
            ICBMSounds.REDMATTER.play(world, location.x(), location.y(), location.z(), 3.0F, CalculationHelpers.randFloatRange(world().rand, -0.8f, 1.2f), true);
        }
    }

    protected void postTick()
    {
        //Decrease block if we don't destroy anything
        if (blocksDestroyedThisTick <= 0)
        {
            decreaseScale();
        }
        if (this.callCount % 10 == 0) //sync server size to clients every 10 ticks TODO this needs to sync more often or we will see rendering issues
        {
            PacketRedmatterSizeSync.sync(this); //TODO handle this in the controller, blasts shouldn't network sync
        }
    }

    protected void decreaseScale()
    {
        //Decrease mass
        this.size -= 0.1; //TODO magic numbers & config

        if (this.size < 50) // evaporation speedup for small black holes
        {
            this.size -= (50 - this.size) / 100; //TODO magic numbers & config
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
            if (currentBlockDestroyRadius >= getBlastRadius())
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
                || !isAlive;
                //|| (System.currentTimeMillis() - tickStartTimeMs) >= ConfigBlast.REDMATTER.MAX_RUNTIME_MS;
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
        final BlockPos blockPos = new BlockPos(rx + xi(), ry + yi(), rz + zi()); //TODO use mutable pos for performance
        final double dist = location.distance(blockPos);

        //We are looping in a shell orbit around the center
        if (dist < this.currentBlockDestroyRadius && dist > this.currentBlockDestroyRadius - 2)
        {
            final IBlockState blockState = world.getBlockState(blockPos);
            if (shouldRemoveBlock(blockPos, blockState)) //TODO calculate a pressure or pull force to destroy weaker blocks before stronger blocks
            {
                //TODO handle multi-blocks

                MinecraftForge.EVENT_BUS.post(new BlastBlockModifyEvent(world, blockPos, () -> {
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), isFluid(blockState) ? 2 : 3);
                    //TODO: render fluid streams moving into hole

                    //Convert a random amount of destroyed blocks into flying blocks for visuals
                    if (canTurnIntoFlyingBlock(blockState) && this.world().rand.nextFloat() > ConfigBlast.REDMATTER.CHANCE_FOR_FLYING_BLOCK)
                    {
                        spawnFlyingBlock(blockPos, blockState);
                    }
                }));
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
        final boolean isFluid = isFluid(blockState);
        //Ignore air blocks and unbreakable blocks
        return !block.isAir(blockState, world(), blockPos)
                && (isFlowingWater(blockState) || !isFluid && blockState.getBlockHardness(world, blockPos) >= 0);

    }

    protected boolean isFluid(IBlockState blockState)
    {
        return blockState.getBlock() instanceof BlockLiquid || blockState.getBlock() instanceof IFluidBlock;
    }

    protected boolean isFlowingWater(IBlockState blockState)
    {
        return blockState.getBlock() instanceof BlockLiquid && blockState.getValue(BlockLiquid.LEVEL) < 7;
    }

    protected boolean canTurnIntoFlyingBlock(IBlockState blockState)
    {
        return ConfigBlast.REDMATTER.SPAWN_FLYING_BLOCKS && !isFluid(blockState);
    }

    protected void spawnFlyingBlock(BlockPos blockPos, IBlockState blockState)
    {
        final EntityFlyingBlock entity = new EntityFlyingBlock(this.world(), blockPos, blockState);
        entity.yawChange = 50 * this.world().rand.nextFloat();
        entity.pitchChange = 50 * this.world().rand.nextFloat();
        this.world().spawnEntity(entity);

        this.handleEntities(entity); //TODO why? this should just be an apply velocity call
    }

    protected void doEntityEffects()
    {
        final float entityRadius = this.getBlastRadius() * 1.5f;

        final Cube cube = new Cube(location.add(0.5).sub(entityRadius), location.add(0.5).add(entityRadius)); //TODO Cache, recalc on movement only
        final AxisAlignedBB bounds = cube.getAABB();

        //Get all entities in the cube area
        this.world().getEntitiesWithinAABB(Entity.class, bounds)
                //Filter down
                .stream().filter(this::shouldHandleEntity)
                //Apply to each
                .forEach(this::handleEntities);
    }

    private boolean shouldHandleEntity(Entity entity)
    {
        //Ignore self
        if (entity == this.controller)
        {
            return false;
        }

        //Ignore players that are in creative mode or can't be harmed TODO may need to check for spectator?
        if (entity instanceof EntityPlayer && (((EntityPlayer) entity).capabilities.isCreativeMode || ((EntityPlayer) entity).capabilities.disableDamage))
        {
            return false;
        }

        //Ignore entities that mark themselves are ignorable
        if (entity instanceof IBlastIgnore && ((IBlastIgnore) entity).canIgnore(this)) //TODO remove
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
        final double xDifference = entity.posX - location.xi() + 0.5; //TODO why 0.5? blast might actually have decimal position
        final double yDifference = entity.posY - location.yi() + 0.5;
        final double zDifference = entity.posZ - location.zi() + 0.5;

        final double distance = this.location.distance(entity); //TODO switch to Sq version for performance

        moveEntity(entity, xDifference, yDifference, zDifference, distance);
        attackEntity(entity, distance);
    }

    private void moveEntity(Entity entity, double xDifference, double yDifference, double zDifference, double distance)
    {
        //Calculate velocity
        final double velX = -xDifference / distance / distance * 5;
        final double velY = -yDifference / distance / distance * 5;
        final double velZ = -zDifference / distance / distance * 5;

        // Gravity Velocity
        entity.addVelocity(velX, velY, velZ); //TODO add API to allow modifying this value

        // if player send packet because motion is handled client side
        if (entity instanceof EntityPlayer)
        {
            entity.velocityChanged = true;
        }
        else if (entity instanceof EntityExplosion)
        {
            final IBlast blast = ((EntityExplosion) entity).getBlast();
            if (blast instanceof BlastRedmatter) //TODO move to capability logic
            {
                final BlastRedmatter rmBlast = (BlastRedmatter) blast;

                final int otherSize = (int) Math.pow(this.getBlastRadius(), 3);
                final int thisSize = (int) Math.pow(blast.getBlastRadius(), 3);
                final double totalSize = otherSize + thisSize;

                final double thisSizePct = thisSize / totalSize;

                final Vec3d totalDelta = rmBlast.getPosition().subtract(this.getPosition());
                final Vec3d thisDelta = totalDelta.scale(thisSizePct);

                if (exploder != null)
                {
                    this.exploder.addVelocity(thisDelta.x, thisDelta.y, thisDelta.z); //TODO we are adding velocity twice
                }
            }
        }
    }

    private void attackEntity(Entity entity, double distance)
    {
        //Handle eating logic
        if (distance < (ConfigBlast.REDMATTER.ENTITY_DESTROY_RADIUS * getScaleFactor())) //TODO make config driven, break section out into its own method
        {
            if (entity instanceof EntityExplosion)
            {
                final IBlast blast = ((EntityExplosion) entity).getBlast();
                if (blast instanceof BlastAntimatter) //TODO move to capability
                {
                    if (ConfigBlast.REDMATTER.ENABLE_AUDIO)
                    {
                        ICBMSounds.EXPLOSION.play(world, location.x(), location.y(), location.z(), 7.0F, CalculationHelpers.randFloatRange(world().rand, -0.6F, 0.9F), true);
                    }
                    if (this.world().rand.nextFloat() > 0.85 && !this.world().isRemote)
                    {
                        //Destroy self
                        clearBlast();
                    }
                }
                else if (blast instanceof BlastRedmatter && ((BlastRedmatter) blast).isAlive && this.isAlive) //TODO move to capability, also why isAlive checks?
                {
                    //https://www.wolframalpha.com/input/?i=(4%2F3)pi+*+r%5E3+%3D+(4%2F3)pi+*+a%5E3+%2B+(4%2F3)pi+*+b%5E3

                    //We are going to merge both blasts together
                    final double selfRad = Math.pow(this.getBlastRadius(), 3);
                    final double targetRad = Math.pow(((EntityExplosion) entity).getBlast().getBlastRadius(), 3);

                    final float newRad = (float) Math.cbrt(selfRad + targetRad); //TODO why cube?

                    //Average out timer
                    this.callCount = (callCount + ((BlastRedmatter) blast).callCount) / 2;

                    this.size = newRad;

                    this.controller.setVelocity(0, 0, 0); //TODO combine the vectors

                    //TODO fire an event when combined (non-cancelable to allow acting on combined result)
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
                    if (this.size < 120)
                    {
                        this.size += 0.05;
                    }
                }
            }
        }
    }

    public static class DamageSourceRedmatter extends DamageSource //TODO move to its own class with proper handling
    {
        public final BlastRedmatter blastRedmatter;

        public DamageSourceRedmatter(BlastRedmatter blastRedmatter)
        {
            super("icbm.redmatter");
            this.blastRedmatter = blastRedmatter;
        }
    }

    @Override
    public boolean isMovable()
    {
        return ConfigBlast.REDMATTER.REDMATTER_MOVEMENT;
    }
}
