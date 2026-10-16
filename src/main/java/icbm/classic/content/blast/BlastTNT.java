package icbm.classic.content.blast;

import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.network.packet.PacketSpawnBlockExplosion;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Pos;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;

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
    @Setter @Accessors(chain = true)
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
    protected void clientRunBlast()
    {

    }

    @Override
    public boolean doExplode(int callCount)
    {
        calculateDamage(); //TODO add listener(s) to control block break and placement

        if(world instanceof ServerLevel) {
            final ServerLevel worldServer = (ServerLevel)world;
            if (this.size >= 2.0F)
            {
                worldServer.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1, 1.0D, 0.0D, 0.0D, 1);
            }
            else
            {
                worldServer.sendParticles(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1, 1.0D, 0.0D, 0.0D, 1);
            }
        }

        this.world().playSound(null, this.location.x(), this.location.y(), this.location.z(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, (1.0F + (this.world().random.nextFloat() - this.world().random.nextFloat()) * 0.2F) * 0.7F);

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
        fireDetonateEvent(new ArrayList<>());

        //Destroy blocks
        doDestroyBlocks(); //TODO add listener(s) to control block break and placement
        return true;
    }

    /**
     * Pre-calculates all blocks to be destroyed
     */
    protected void calculateDamage() //TODO thread
    {
        //TODO fix, alg seems to be making non-semetric shapes in harder blocks.
        //      TnT vanilla will do 3x3
        //      Condensed will do a 2x4 shaft with a few offshoots but doesn't scale correctly
        if (!this.world().isClientSide())
        {
            for (int xs = 0; xs < this.raysPerAxis; ++xs)
            {
                for (int ys = 0; ys < this.raysPerAxis; ++ys)
                {
                    for (int zs = 0; zs < this.raysPerAxis; ++zs)
                    {
                        //Only run on edges of the cube
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
                            float radialEnergy = this.getBlastRadius() * (0.7F + this.world().random.nextFloat() * 0.6F);

                            //Get starting point for ray
                            double x = this.location.x();
                            double y = this.location.y();
                            double z = this.location.z();

                            for (float step = 0.3F; radialEnergy > 0.0F; radialEnergy -= step * 0.75F)
                            {
                                //Convert position to int
                                int xi = Mth.floor(x);
                                int yi = Mth.floor(y);
                                int zi = Mth.floor(z);

                                //Get block
                                BlockPos blockPos = new BlockPos(xi, yi, zi);
                                BlockState blockState = world.getBlockState(blockPos);

                                //Only act on non-air blocks
                                if (!blockState.isAir())
                                {
                                    //Decrease energy based on resistance
                                    radialEnergy -= (getBlockResistance(blockPos, blockState) + 0.3F) * step;

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
        if (!this.world().isClientSide())
        {
            for (BlockPos blockDestroyedPos : getAffectedBlockPositions()) //TODO convert block positions to block edits to track prev and current blocks
            {
                //Get block
                final BlockState blockState = world().getBlockState(blockDestroyedPos);

                ///Generate effect TODO send a single packet with a list of block pos, this will do a 80% reduction in packet byte data
                PacketSpawnBlockExplosion.sendToAllClients(world(), x(), y(), z(), getBlastRadius(), blockDestroyedPos);

                //Only edit block if not air
                if (!blockState.isAir())
                {
                    try
                    {
                        //Do drops and break block
                        destroyBlockByBlast(blockDestroyedPos, blockState, true);
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
        List<Entity> entities = world.getEntitiesOfClass(Entity.class, region.getAABB());

        for (Entity entity : entities)
        {
            double distanceScale = Math.sqrt(entity.distanceToSqr(location.x(), location.y(), location.z())) / radius;

            if (distanceScale <= 1.0D)
            {
                //Get delta
                double xDifference = entity.getX() - location.x();
                double yDifference = entity.getY() - location.y();
                double zDifference = entity.getZ() - location.z();

                //Get magnitude
                double mag = Math.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);

                //Normalize difference
                xDifference /= mag;
                yDifference /= mag;
                zDifference /= mag;

                if (type == PushType.ATTRACT)
                {
                    double modifier = distanceScale * force * (entity instanceof Player ? 0.5 : 1);
                    entity.push(-xDifference * modifier, -yDifference * modifier, -zDifference * modifier);
                    markVelocityChanged(entity);
                }
                else if (type == PushType.REPEL)
                {
                    double modifier = (1.0D - distanceScale) * force * (entity instanceof Player ? 0.5 : 1);
                    entity.push(xDifference * modifier, yDifference * modifier, zDifference * modifier);
                    markVelocityChanged(entity);
                }
            }
        }
    }

    @Override
    public void load(CompoundTag nbt)
    {
        super.load(nbt);
        this.pushType = PushType.values()[nbt.getInt(NBTConstants.PUSH_TYPE)];
        this.destroyItem = nbt.getBoolean(NBTConstants.DESTROY_ITEM);
    }

    @Override
    public void save(CompoundTag nbt)
    {
        super.save(nbt);
        nbt.putInt(NBTConstants.PUSH_TYPE, this.pushType.ordinal());
        nbt.putBoolean(NBTConstants.DESTROY_ITEM, this.destroyItem);
    }

    public static enum PushType
    {
        NO_PUSH, ATTRACT, REPEL;
    }
}
// PORTED
