package icbm.classic.content.blast.threaded;

import icbm.classic.ICBMClassic;
import icbm.classic.api.NBTConstants;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blast.BlastMutation;
import icbm.classic.content.blast.BlastRot;
import icbm.classic.lib.transform.vector.Location;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class BlastNuclear extends BlastThreaded
{
    private float energy;
    private boolean spawnMoreParticles = false;
    private boolean isRadioactive = false;

    public BlastNuclear(){}

    public BlastNuclear setEnergy(float energy)
    {
        this.energy = energy;
        return this;
    }

    @Override
    public BlastNuclear scaleBlast(double scale)
    {
        super.scaleBlast(scale);
        this.energy *= scale;
        return this;
    }


    public BlastNuclear setNuclear()
    {
        this.spawnMoreParticles = true;
        this.isRadioactive = true;
        return this;
    }

    @Override
    public boolean doRun(int loops, Consumer<BlockPos> edits)
    {
        //How many steps to go per rotation
        final int steps = (int) Math.ceil(Math.PI / Math.atan(1.0D / this.getBlastRadius()));

        double x;
        double y;
        double z;

        double dx;
        double dy;
        double dz;

        double power;

        double yaw;
        double pitch;

        for (int phi_n = 0; phi_n < 2 * steps; phi_n++)
        {
            for (int theta_n = 0; theta_n < steps; theta_n++)
            {
                //Calculate power
                power = this.energy - (this.energy * world.rand.nextFloat() / 2);

                //Get angles for rotation steps
                yaw = Math.PI * 2 / steps * phi_n;
                pitch = Math.PI / steps * theta_n;

                //Figure out vector to move for trace (cut in half to improve trace skipping blocks)
                dx = sin(pitch) * cos(yaw) * 0.5;
                dy = cos(pitch) * 0.5;
                dz = sin(pitch) * sin(yaw) * 0.5;

                //Reset position to current
                x = this.x();
                y = this.y();
                z = this.z();

                BlockPos prevPos = null;

                //Trace from start to end
                while (location.distance(x, y, z) <= this.getBlastRadius() && power > 0) //TODO replace distance check with SQ version
                {
                    //Consume power per loop
                    power -= 0.3F * 0.75F * 5; //TODO why the magic numbers?

                    //Convert double position to int position as block pos
                    final BlockPos blockPos = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));

                    //Only do action one time per block (not a perfect solution, but solves double hit on the same block in the same line)
                    if (prevPos != blockPos)
                    {
                        if(!world.isBlockLoaded(blockPos)) //TODO: find better fix for non main thread loading
                            continue;

                        //Get block state and block from position
                        final IBlockState state = world.getBlockState(blockPos);
                        final Block block = state.getBlock();

                        //Ignore air blocks && Only break block that can be broken
                        if (!block.isAir(state, world, blockPos) && state.getBlockHardness(world, blockPos) >= 0)
                        {
                            //Consume power based on block
                            power -= getResistance(blockPos, state);

                            //If we still have power, break the block
                            if (power > 0f)
                            {
                                edits.accept(blockPos);
                            }
                        }
                    }

                    //Note previous block
                    prevPos = blockPos;

                    //Move forward
                    x += dx;
                    y += dy;
                    z += dz;
                }
            }
        }
        return false;
    }

    public float getResistance(BlockPos pos, IBlockState state)
    {
        final Block block = state.getBlock();
        if (state.getMaterial().isLiquid())
        {
            return 0.25f;
        }
        else
        {
            return block.getExplosionResistance(world, pos, getExplosivePlacedBy(), this);
        }
    }


    @Override
    public void setupBlast()
    {
        super.setupBlast();
        if (this.world() != null)
        {
            if (this.spawnMoreParticles)
            {
                // Spawn nuclear cloud.
                for (int y = 0; y < 26; y++)
                {
                    int r = 4;

                    if (y < 8)
                    {
                        r = Math.max(Math.min((8 - y) * 2, 10), 4);
                    }
                    else if (y > 15)
                    {
                        r = Math.max(Math.min((y - 15) * 2, 15), 5);
                    }

                    for (int x = -r; x < r; x++)
                    {
                        for (int z = -r; z < r; z++)
                        {
                            double distance = MathHelper.sqrt(x * x + z * z);

                            if (r > distance && r - 3 < distance)
                            {
                                Location spawnPosition = location.add(new Pos(x * 2, (y - 2) * 2, z * 2));
                                float xDiff = (float) (spawnPosition.x() - location.x());
                                float zDiff = (float) (spawnPosition.z() - location.z());
                                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, spawnPosition.x(), spawnPosition.y(), spawnPosition.z(),
                                        xDiff * 0.3 * world().rand.nextFloat(), -world().rand.nextFloat(), zDiff * 0.3 * world().rand.nextFloat()); //(float) (distance / this.getRadius()) * oldWorld().rand.nextFloat(), 0, //0, 8F, 1.2F);
                            }
                        }
                    }
                }
            }

            this.doDamageEntities(this.getBlastRadius(), this.energy * 1000);

            ICBMSounds.EXPLOSION.play(world, this.location.x(), this.location.y(), this.location.z(), 7.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
        }
    }

    @Override
    public boolean doExplode(int callCount)
    {
        super.doExplode(callCount);
        int r = this.callCount;

        if (this.world().isRemote)
        {
            for (int x = -r; x < r; x++)
            {
                for (int z = -r; z < r; z++)
                {
                    double distance = MathHelper.sqrt(x * x + z * z);

                    if (distance < r && distance > r - 1)
                    {
                        Location targetPosition = this.location.add(new Pos(x, 0, z));

                        if (this.world().rand.nextFloat() < Math.max(0.001 * r, 0.05))
                        {
                            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, targetPosition.x(), targetPosition.y(), targetPosition.z(), 0, 0, 0); //5F, 1F);
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onBlastCompleted()
    {
        super.onBlastCompleted();
        if (world() != null && !world().isRemote)
        {
            try
            {
                //Attack entities
                this.doDamageEntities(this.getBlastRadius(), this.energy * 1000);
                //TODO hook into AS and apply radiation damage or fire a damage event to allow AS/proxy to hook

                //Place radio active blocks
                if (this.isRadioactive)
                {
                    new BlastRot()
                    .setBlastWorld(world())
                    .setBlastSource(this.exploder)
                    .setBlastPosition(location.x(), location.y(), location.z())
                    .setBlastSize(this.getBlastRadius())
                    .buildBlast().runBlast();  //TODO trigger from explosive handler

                    new BlastMutation()
                    .setBlastWorld(world())
                    .setBlastSource(this.exploder)
                    .setBlastPosition(location.x(), location.y(), location.z())
                    .setBlastSize(this.getBlastRadius())
                    .buildBlast().runBlast();  //TODO trigger from explosive handler

                    if (this.world().rand.nextInt(3) == 0)
                    {
                        world().rainingStrength = 1f;
                    }
                }

                //Play audio
                ICBMSounds.EXPLOSION.play(world, this.location.x(), this.location.y(), this.location.z(), 10.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);

            }
            catch (Exception e)
            {
                String msg = String.format("BlastNuclear#doPostExplode() ->  Unexpected error while running post detonation code " +
                        "\nWorld = %s " +
                        "\nThread = %s" +
                        "\nSize = %s" +
                        "\nPos = %s",
                        world, getThread(), size, location);
                ICBMClassic.logger().error(msg, e);
            }
        }
    }

    @Override
    public void load(NBTTagCompound nbt)
    {
        super.load(nbt);
        this.spawnMoreParticles = nbt.getBoolean(NBTConstants.SPAWN_MORE_PARTICLES);
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        super.save(nbt);
        nbt.setBoolean(NBTConstants.SPAWN_MORE_PARTICLES, this.spawnMoreParticles);
    }
}
