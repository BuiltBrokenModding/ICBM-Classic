package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IBlastIgnore;
import icbm.classic.api.explosion.IBlastMovable;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigBlast;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.entity.EntityFlyingBlock;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.lib.network.packet.PacketRedmatterSizeSync;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.lib.transform.vector.Pos;
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
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import java.util.List;

public class BlastRedmatter extends Blast implements IBlastTickable, IBlastMovable
{
    //Constants, do not change as they modify render and effect scales
    public static final float NORMAL_RADIUS = 70;
    public static final float ENTITY_DESTROY_RADIUS = 6;

    //Config settings
    public static int MAX_BLOCKS_EDITS_PER_TICK = 100;
    public static int DEFAULT_BLOCK_EDITS_PER_TICK = 20;
    public static int MAX_LIFESPAN = 36000; // 30 minutes
    public static float CHANCE_FOR_FLYING_BLOCK = 0.8f;
    public static boolean DO_DESPAWN = true;
    public static boolean doAudio = true;
    public static boolean doFlyingBlocks = true;

    //Blast Settings
    public int lifeSpan = MAX_LIFESPAN;
    public int blocksEditsPerTick = -1;

    public boolean coloredBeams = true;

    //client side value
    public float targetSize = 0.0F;

    public float getScaleFactor()
    {
        return size / NORMAL_RADIUS;
    }

    public int getBlocksPerTick()
    {
        if (blocksEditsPerTick == -1)
        {
            blocksEditsPerTick = (int) Math.min(MAX_BLOCKS_EDITS_PER_TICK, DEFAULT_BLOCK_EDITS_PER_TICK * getScaleFactor());
        }
        return blocksEditsPerTick;
    }

    //    @Override
    //    public boolean setupBlast()
    //    {
    //        if (!this.world().isRemote)
    //        {
    //            //this.oldWorld().createExplosion(this.exploder, position.x(), position.y(), position.z(), Math.max(2, 15.0F * getScaleFactor()), true);
    //        }
    //
    //        return true;
    //    }

    @Override
    protected void onBlastCompleted()
    {
        //Kill host TODO see if this is really needed
        AxisAlignedBB bounds = new AxisAlignedBB(this.x - this.size, this.y - this.size, this.z - this.size, this.x + this.size, this.y + this.size, this.z + this.size);
        List<?> list = this.world().getEntitiesWithinAABB(EntityExplosion.class, bounds);

        for (Object obj : list)
        {
            if (obj instanceof EntityExplosion)
            {
                EntityExplosion explosion = (EntityExplosion) obj;

                if (explosion.getBlast() == this)
                {
                    explosion.setDead();
                }
            }
        }
    }


    @Override
    public boolean onBlastTick(int ticksExisted)
    {
        if(world().isRemote)
        {
            //reach the target size which is the size that was last sent from the server
            if(targetSize < size)
                size -= Math.min(1,size-targetSize)/10f;
            else if(targetSize > size)
                size += Math.min(1,targetSize-size)/10f;
        }

        return super.onBlastTick(ticksExisted);
    }

    @Override
    public boolean doExplode(int callCount)
    {
        if (!this.world().isRemote)
        {
            //Decrease mass
            this.size-=0.1;

            if (this.size < 50) // evaporation speedup for small black holes
            {
                this.size -= (50-this.size)/100;
            }


            if (this.callCount % 10 == 0) //sync server size to clients every 10 ticks
            {
                ICBMClassic.packetHandler.sendToAllAround(new PacketRedmatterSizeSync(size, getPos()), new TargetPoint(world().provider.getDimension(), getPos().getX(), getPos().getY(), getPos().getZ(), 256));
            }

            //Limit life span of the blast
            if (DO_DESPAWN && callCount >= lifeSpan || this.size <= 0)
            {
                this.completeBlast(); //kill explosion
            }

            //Do actions
            doDestroyBlocks();
            doEntityMovement();

            //Play effects
            if (doAudio)
            {
                if (this.world().rand.nextInt(8) == 0)
                {
                    ICBMSounds.COLLAPSE.play(world, location.x() + (Math.random() - 0.5) * getBlastRadius(), location.y() + (Math.random() - 0.5) * getBlastRadius(), location.z() + (Math.random() - 0.5) * getBlastRadius(), 6.0F - this.world().rand.nextFloat(), 1.0F - this.world().rand.nextFloat() * 0.4F, true);
                }
                ICBMSounds.REDMATTER.play(world, location.x(), location.y(), location.z(), 3.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 1F, true);
            }
        }
        return false;
    }

    protected void doDestroyBlocks()
    {
        long time = System.currentTimeMillis();
        // Try to find and grab some blocks to orbit
        int blocksDestroyed = 0;

        for (int currentRadius = 1; currentRadius < getBlastRadius(); currentRadius++) //TODO recode as it can stall the main thread
        {
            for (int xr = -currentRadius; xr < currentRadius; xr++)
            {
                for (int yr = -currentRadius; yr < currentRadius; yr++)
                {
                    for (int zr = -currentRadius; zr < currentRadius; zr++)
                    {
                        final BlockPos blockPos = new BlockPos(location.xi() + xr, location.yi() + yr, location.zi() + zr);
                        final double dist = location.distance(blockPos);

                        //We are looping in a shell orbit around the center
                        if (dist < currentRadius && dist > currentRadius - 2)
                        {
                            final IBlockState blockState = world.getBlockState(blockPos);
                            final Block block = blockState.getBlock();
                            //Null if call was made on an unloaded chunk
                            if (block != null)
                            {
                                final boolean isFluid = block instanceof BlockLiquid || block instanceof IFluidBlock;
                                //Ignore air blocks and unbreakable blocks
                                if (!block.isAir(blockState, world(), blockPos) && (((isFluid && blockState.getValue(BlockLiquid.LEVEL) < 7) || (!isFluid && blockState.getBlockHardness(world, blockPos) >= 0))))
                                {
                                    //TODO handle multi-blocks

                                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), isFluid ? 2 : 3);
                                    //TODO: render fluid streams moving into hole
                                    if (!isFluid && doFlyingBlocks)
                                    {
                                        //Convert a random amount of destroyed blocks into flying blocks for visuals
                                        if (this.world().rand.nextFloat() > CHANCE_FOR_FLYING_BLOCK)
                                        {
                                            EntityFlyingBlock entity = new EntityFlyingBlock(this.world(), blockPos, blockState);
                                            entity.yawChange = 50 * this.world().rand.nextFloat();
                                            entity.pitchChange = 50 * this.world().rand.nextFloat();
                                            this.world().spawnEntity(entity);
                                            this.affectEntity(this.getBlastRadius() * 2, entity, false);
                                        }
                                    }
                                    //Keep track of blocks removed to keep from lagging the game
                                    blocksDestroyed++;
                                }
                            }
                        }

                        //Exit conditions, make sure stays at bottom of loop
                        if (blocksDestroyed > getBlocksPerTick() || !isAlive || System.currentTimeMillis() - time > 30) //30ms to prevent stall
                        {
                            return;
                        }
                    }
                }
            }
        }
    }

    protected void doEntityMovement()
    {
        float entityRadius = this.getBlastRadius() * 2;
        Cube cube = new Cube(location.add(0.5).sub(entityRadius), location.add(0.5).add(entityRadius));
        AxisAlignedBB bounds = cube.getAABB();
        List<Entity> allEntities = this.world().getEntitiesWithinAABB(Entity.class, bounds);
        boolean doExplosion = true;
        for (Entity entity : allEntities)
        {
            doExplosion = !this.affectEntity(entityRadius, entity, doExplosion);
        }
    }

    /**
     * Makes an entity get affected by Red Matter.
     *
     * @Return True if explosion happened
     */
    public boolean affectEntity(float radius, Entity entity, boolean doExplosion)
    {
        //Ignore players that are in creative mode or can't be harmed
        if (entity instanceof EntityPlayer && (((EntityPlayer) entity).capabilities.isCreativeMode || ((EntityPlayer) entity).capabilities.disableDamage))
        {
            return false;
        }

        //Ignore self
        if (entity == this.controller)
        {
            return false;
        }

        //Ignore entities that mark themselves are ignorable
        if (entity instanceof IBlastIgnore)
        {
            if (((IBlastIgnore) entity).canIgnore(this)) //TODO pass in distance and deltas
            {
                return false;
            }
        }

        //Calculate different from center
        double xDifference = entity.posX - location.xi() + 0.5;
        double yDifference = entity.posY - location.yi() + 0.5;
        double zDifference = entity.posZ - location.zi() + 0.5;

        /** The percentage of the closeness of the entity. */
        double xPercentage = 1 - (xDifference / radius);
        double yPercentage = 1 - (yDifference / radius);
        double zPercentage = 1 - (zDifference / radius);
        double distancePercentage = this.location.distance(entity) / radius;

        Pos entityPosition = new Pos(entity);
        Pos centeredPosition = entityPosition.subtract(this.location);
        centeredPosition = (Pos) centeredPosition.transform(new EulerAngle(1.5 * distancePercentage * Math.random(), 1.5 * distancePercentage * Math.random(), 1.5 * distancePercentage * Math.random()));

        Location newPosition = this.location.add(centeredPosition);
        // Orbit Velocity
        entity.addVelocity(newPosition.x() - entityPosition.x(), 0, newPosition.z() - entityPosition.z());
        // Gravity Velocity (0.015 is barely enough to overcome y gravity so do not lower)
        entity.addVelocity(-xDifference * 0.02 * xPercentage, -yDifference * 0.02 * yPercentage, -zDifference * 0.02 * zPercentage);

        if (entity instanceof EntityPlayer) // if player send packet because motion is handled client side
        {
            entity.velocityChanged = true;
        }

        if (entity instanceof EntityExplosion)
        {
            final IBlast blast = ((EntityExplosion) entity).getBlast();
            if (blast instanceof  BlastRedmatter)
            {
                final BlastRedmatter rmBlast = (BlastRedmatter)blast;

                final int otherSize = (int)Math.pow(this.getBlastRadius(),3);
                final int thisSize = (int)Math.pow(blast.getBlastRadius(),3);
                final double totalSize = otherSize + thisSize;

                final double thisSizePct = thisSize / totalSize;

                final Vec3d totalDelta = rmBlast.getPosition().subtract(this.getPosition());
                final Vec3d thisDelta = totalDelta.scale(thisSizePct);

                if(exploder != null)
                    this.exploder.addVelocity(thisDelta.x,thisDelta.y,thisDelta.z);
            }
        }

        boolean explosionCreated = false;

        if (new Pos(entity.posX, entity.posY, entity.posZ).distance(location) < (ENTITY_DESTROY_RADIUS * getScaleFactor()))
        {
            if (entity instanceof EntityExplosion)
            {
                final IBlast blast = ((EntityExplosion) entity).getBlast();
                if (blast instanceof BlastAntimatter)
                {
                    if (doAudio)
                    {
                        ICBMSounds.EXPLOSION.play(world, location.x(), location.y(), location.z(), 7.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
                    }
                    if (this.world().rand.nextFloat() > 0.85 && !this.world().isRemote)
                    {
                        entity.setDead();
                        return explosionCreated;
                    }
                }
                else if (blast instanceof BlastRedmatter && ((BlastRedmatter) blast).isAlive && this.isAlive)
                {
                    //https://www.wolframalpha.com/input/?i=(4%2F3)pi+*+r%5E3+%3D+(4%2F3)pi+*+a%5E3+%2B+(4%2F3)pi+*+b%5E3

                    //We are going to merge both blasts together
                    double sizeA = this.getBlastRadius();
                    sizeA = sizeA * sizeA * sizeA;
                    double sizeB = ((EntityExplosion) entity).getBlast().getBlastRadius();
                    sizeB = sizeB * sizeB * sizeB;
                    float radiusNew = (float) Math.cbrt(sizeA + sizeB);

                    //Average out timer
                    this.callCount = (callCount + ((BlastRedmatter)blast).callCount) / 2;

                    this.size = radiusNew;

                    this.controller.setVelocity(0,0,0);

                    //Kill explosion entity
                    ((BlastRedmatter)blast).isAlive = false;
                    ((BlastRedmatter)blast).controller.setDead();
                }
                //Kill entity in the center of the ball
                entity.setDead();
            }
            else if (entity instanceof EntityMissile)
            {
                ((EntityMissile) entity).doExplosion();
            }
            else if (entity instanceof EntityExplosive)
            {
                ((EntityExplosive) entity).explode();
            }
            else if (entity instanceof EntityLiving || entity instanceof EntityPlayer)
            {
                entity.attackEntityFrom(new DamageSourceRedmatter(this), 2000);
            }
            else
            {
                //Kill entity in the center of the ball
                entity.setDead();
                if(entity instanceof EntityFlyingBlock)
                {
                    if(this.size<120)
                        this.size+=0.1;
                }
            }
        }

        return explosionCreated;
    }

    public static class DamageSourceRedmatter extends DamageSource
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
        return ConfigBlast.REDMATTER_MOVEMENT;
    }

}
