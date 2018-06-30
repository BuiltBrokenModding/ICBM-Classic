package icbm.classic.content.explosive.blast;

import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom version of Minecraft TNT style blast for use
 * in basic explosives.
 */
public class BlastTNT extends Blast
{
    /** 0- No push, 1 - Attract, 2 - Repel */
    private int pushType = 0; //TODO change to enum
    /** Do destroy items */
    private boolean destroyItem = false;

    /** Amount of damage to do to an entity */
    public float damageToEntities = 10F;

    /** Number of rays (or steps) to use for blast per axis (x, y, z) */
    public int raysPerAxis = 16;

    /**
     * @param world  - location
     * @param entity - cause
     * @param x      - location
     * @param y      - location
     * @param z      - location
     * @param power  - Modifies power
     */
    public BlastTNT(World world, Entity entity, double x, double y, double z, float power)
    {
        super(world, entity, x, y, z, power);
    }

    /**
     * Sets push type, defaults to damage entity
     *
     * @param type
     * @return this
     */
    public BlastTNT setPushType(int type)
    {
        this.pushType = type;
        return this;
    }

    /**
     * Sets items to be destroyed
     *
     * @return this
     */
    public BlastTNT setDestroyItems()
    {
        this.destroyItem = true;
        return this;
    }

    @Override
    public void doExplode()
    {
        calculateDamage(); //TODO add listener(s) to control block break and placement

        //TODO move effect to Effect handler
        this.world().playSound(null, this.position.x(), this.position.y(), this.position.z(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F);

        //TODO collect entities before applying effects, this way event can override
        switch (this.pushType)
        {
            case 0:
                this.doDamageEntities(this.getBlastRadius(), damageToEntities, this.destroyItem);
                break;
            default:
                this.pushEntities(12, this.getBlastRadius() * 4, this.pushType);
                break;
        }

        //Fire event to allow blocking explosion damage
        MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Detonate(world, this, new ArrayList()));

        //Destroy blocks
        doDestroyBlocks(); //TODO add listener(s) to control block break and placement
    }

    /**
     * Pre-calculates all blocks to be destroyed
     */
    protected void calculateDamage() //TODO thread
    {
        if (!this.world().isRemote)
        {
            for (int xs = 0; xs < this.raysPerAxis; ++xs)
            {
                for (int ys = 0; ys < this.raysPerAxis; ++ys)
                {
                    for (int zs = 0; zs < this.raysPerAxis; ++zs)
                    {
                        if (xs == 0 || xs == this.raysPerAxis - 1 || ys == 0 || ys == this.raysPerAxis - 1 || zs == 0 || zs == this.raysPerAxis - 1)
                        {
                            //Delta distance
                            double xStep = xs / (this.raysPerAxis - 1.0F) * 2.0F - 1.0F;
                            double yStep = ys / (this.raysPerAxis - 1.0F) * 2.0F - 1.0F;
                            double zStep = zs / (this.raysPerAxis - 1.0F) * 2.0F - 1.0F;

                            //Distance
                            final double diagonalDistance = Math.sqrt(xStep * xStep + yStep * yStep + zStep * zStep);

                            //normalize
                            xStep /= diagonalDistance;
                            yStep /= diagonalDistance;
                            zStep /= diagonalDistance;

                            //Get energy
                            float radialEnergy = this.getBlastRadius() * (0.7F + this.world().rand.nextFloat() * 0.6F);

                            //Get starting point for ray
                            double x = this.position.x();
                            double y = this.position.y();
                            double z = this.position.z();

                            for (float step = 0.3F; radialEnergy > 0.0F; radialEnergy -= step * 0.75F)
                            {
                                //Convert position to int
                                int xi = MathHelper.floor(x);
                                int yi = MathHelper.floor(y);
                                int zi = MathHelper.floor(z);

                                //Get block
                                BlockPos blockPos = new BlockPos(xi, yi, zi);
                                IBlockState blockState = world.getBlockState(blockPos);
                                Block block = blockState.getBlock();

                                //Only act on non-air blocks
                                if (block != Blocks.AIR)
                                {
                                    //Decrease energy based on resistance
                                    radialEnergy -= (block.getExplosionResistance(this.world(), blockPos, this.exploder, this) + 0.3F) * step;

                                    //Track blocks to destroy
                                    if (radialEnergy > 0.0F)
                                    {
                                        if (!getAffectedBlockPositions().contains(blockPos))
                                        {
                                            getAffectedBlockPositions().add(blockPos);
                                        }
                                    }
                                }

                                //Iterate location
                                x += xStep * step;
                                y += yStep * step;
                                z += zStep * step;
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * Removes the blocks from the world
     */
    protected void doDestroyBlocks() //TODO convert to change action
    {
        if (!this.world().isRemote)
        {
            for (BlockPos blownPosition : getAffectedBlockPositions()) //TODO convert block positions to block edits to track prev and current blocks
            {
                //Get position
                int xi = blownPosition.getX();
                int yi = blownPosition.getY();
                int zi = blownPosition.getZ();

                //Get block
                IBlockState blockState = world.getBlockState(blownPosition);
                Block block = blockState.getBlock();

                ///Generate effect TODO move to effect handler
                ///---------------------------------------------
                double var9 = (xi + this.world().rand.nextFloat());
                double var11 = (yi + this.world().rand.nextFloat());
                double var13 = (zi + this.world().rand.nextFloat());

                double var151 = var9 - this.position.y();
                double var171 = var11 - this.position.y();
                double var191 = var13 - this.position.z();

                double var211 = MathHelper.sqrt(var151 * var151 + var171 * var171 + var191 * var191);
                var151 /= var211;
                var171 /= var211;
                var191 /= var211;

                double var23 = 0.5D / (var211 / this.getBlastRadius() + 0.1D);
                var23 *= (this.world().rand.nextFloat() * this.world().rand.nextFloat() + 0.3F);
                var151 *= var23;
                var171 *= var23;
                var191 *= var23;

                this.world().spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, (var9 + this.position.x() * 1.0D) / 2.0D, (var11 + this.position.y() * 1.0D) / 2.0D, (var13 + this.position.z() * 1.0D) / 2.0D, var151, var171, var191);
                this.world().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, var9, var11, var13, var151, var171, var191);
                ///---------------------------------------------

                //Only edit block if not air TODO see if we need to check for modded air
                if (block != Blocks.AIR)
                {
                    try
                    {
                        //Do drops
                        if (block.canDropFromExplosion(null))
                        {
                            block.dropBlockAsItemWithChance(this.world(), blownPosition, blockState, 1F, 0);
                        }

                        //Break block
                        block.onBlockExploded(this.world(), blownPosition, this);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void pushEntities(float radius, float force, int type) //TODO convert to delay action
    {
        // Step 2: Damage all entities
        Pos minCoord = position.toPos();
        minCoord = minCoord.add(-radius - 1);
        Pos maxCoord = position.toPos();
        maxCoord = maxCoord.add(radius + 1);

        Cube region = new Cube(minCoord, maxCoord);
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, region.getAABB());

        for (Entity entity : entities)
        {
            double var13 = entity.getDistance(position.x(), position.y(), position.z()) / radius;

            if (var13 <= 1.0D)
            {
                //Get delta
                double xDifference = entity.posX - position.x();
                double yDifference = entity.posY - position.y();
                double zDifference = entity.posZ - position.z();

                //Get magnitude
                double mag = MathHelper.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);

                //Normalize difference
                xDifference /= mag;
                yDifference /= mag;
                zDifference /= mag;

                if (type == 1)
                {
                    double modifier = var13 * force * (entity instanceof EntityPlayer ? 0.5 : 1);
                    entity.addVelocity(-xDifference * modifier, -yDifference * modifier, -zDifference * modifier);
                }
                else if (type == 2)
                {
                    double modifier = (1.0D - var13) * force * (entity instanceof EntityPlayer ? 0.5 : 1);
                    entity.addVelocity(xDifference * modifier, yDifference * modifier, zDifference * modifier);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.pushType = nbt.getInteger("pushType");
        this.destroyItem = nbt.getBoolean("destroyItem");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("pushType", this.pushType);
        nbt.setBoolean("destroyItem", this.destroyItem);
    }
}
