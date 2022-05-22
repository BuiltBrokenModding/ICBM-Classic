package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.network.packet.PacketSpawnBlockExplosion;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
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
    private PushType pushType = PushType.NO_PUSH;
    /** Do destroy items */
    private boolean destroyItem = false;

    /** Amount of damage to do to an entity */
    public float damageToEntities = 10F;

    /** Number of rays (or steps) to use for blast per axis (x, y, z) */
    public int raysPerAxis = 16;

    public BlastTNT()
    {
        super();
    }

    /**
     * Sets push type, defaults to damage entity
     *
     * @param type
     * @return this
     */
    public BlastTNT setPushType(PushType type)
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
    public boolean doExplode(int callCount)
    {
        calculateDamage(); //TODO add listener(s) to control block break and placement

        this.world().playSound(null, this.location.x(), this.location.y(), this.location.z(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F);

        //TODO collect entities before applying effects, this way event can override
        if (this.pushType == PushType.NO_PUSH)
        {
            this.doDamageEntities(this.getBlastRadius(), damageToEntities, this.destroyItem);
        }
        else
        {
            this.pushEntities(12, this.getBlastRadius() * 4, this.pushType);
        }

        //Fire event to allow blocking explosion damage
        MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Detonate(world, this, new ArrayList()));

        //Destroy blocks
        doDestroyBlocks(); //TODO add listener(s) to control block break and placement
        return true;
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
                            double x = this.location.x();
                            double y = this.location.y();
                            double z = this.location.z();

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
                                if (blockState.getMaterial() != Material.AIR)
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
            for (BlockPos blockDestroyedPos : getAffectedBlockPositions()) //TODO convert block positions to block edits to track prev and current blocks
            {
                //Get block
                final IBlockState blockState = world().getBlockState(blockDestroyedPos);

                ///Generate effect
                PacketSpawnBlockExplosion.sendToAllClients(world(), x(), y(), z(), getBlastRadius(), blockDestroyedPos);

                //Only edit block if not air
                if (blockState.getMaterial() != Material.AIR)
                {
                    try
                    {
                        //Do drops
                        if (blockState.getBlock().canDropFromExplosion(this))
                        {
                            blockState.getBlock().dropBlockAsItemWithChance(this.world(), blockDestroyedPos, blockState, 1F, 0);
                        }

                        //Break block
                        blockState.getBlock().onBlockExploded(this.world(), blockDestroyedPos, this);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void pushEntities(float radius, float force, PushType type) //TODO convert to delay action
    {
        // Step 2: Damage all entities
        Pos minCoord = location.toPos();
        minCoord = minCoord.add(-radius - 1);
        Pos maxCoord = location.toPos();
        maxCoord = maxCoord.add(radius + 1);

        Cube region = new Cube(minCoord, maxCoord);
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, region.getAABB());

        for (Entity entity : entities)
        {
            double distanceScale = entity.getDistance(location.x(), location.y(), location.z()) / radius;

            if (distanceScale <= 1.0D)
            {
                //Get delta
                double xDifference = entity.posX - location.x();
                double yDifference = entity.posY - location.y();
                double zDifference = entity.posZ - location.z();

                //Get magnitude
                double mag = MathHelper.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);

                //Normalize difference
                xDifference /= mag;
                yDifference /= mag;
                zDifference /= mag;

                if (type == PushType.ATTRACT)
                {
                    double modifier = distanceScale * force * (entity instanceof EntityPlayer ? 0.5 : 1);
                    entity.addVelocity(-xDifference * modifier, -yDifference * modifier, -zDifference * modifier);
                }
                else if (type == PushType.REPEL)
                {
                    double modifier = (1.0D - distanceScale) * force * (entity instanceof EntityPlayer ? 0.5 : 1);
                    entity.addVelocity(xDifference * modifier, yDifference * modifier, zDifference * modifier);
                }
            }
        }
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        this.pushType = PushType.values()[nbt.getInteger(NBTConstants.PUSH_TYPE)];
        this.destroyItem = nbt.getBoolean(NBTConstants.DESTROY_ITEM);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setInteger(NBTConstants.PUSH_TYPE, this.pushType.ordinal());
        nbt.setBoolean(NBTConstants.DESTROY_ITEM, this.destroyItem);
    }

    public static enum PushType
    {
        NO_PUSH, ATTRACT, REPEL;
    }
}
