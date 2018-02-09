package icbm.classic.content.explosive.blast;

import icbm.classic.lib.transform.vector.Location;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.explosive.thread.ThreadLargeExplosion;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlastNuclear extends Blast
{
    private ThreadLargeExplosion thread;
    private float energy;
    private boolean spawnMoreParticles = false;
    private boolean isRadioactive = false;

    public BlastNuclear(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size);
    }

    public BlastNuclear(World world, Entity entity, double x, double y, double z, float size, float energy)
    {
        this(world, entity, x, y, z, size);
        this.energy = energy;
    }

    public BlastNuclear setNuclear()
    {
        this.spawnMoreParticles = true;
        this.isRadioactive = true;
        return this;
    }

    @Override
    public void doPreExplode()
    {
        if (!this.world().isRemote)
        {
            this.thread = new ThreadLargeExplosion(this, (int) this.getRadius(), this.energy, this.exploder);

            this.thread.start();

        }
        else if (this.spawnMoreParticles)
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
                            Location spawnPosition = position.add(new Pos(x * 2, (y - 2) * 2, z * 2));
                            float xDiff = (float) (spawnPosition.x() - position.x());
                            float zDiff = (float) (spawnPosition.z() - position.z());
                            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, spawnPosition.x(), spawnPosition.y(), spawnPosition.z(),
                                    xDiff * 0.3 * world().rand.nextFloat(), -world().rand.nextFloat(), zDiff * 0.3 * world().rand.nextFloat()); //(float) (distance / this.getRadius()) * oldWorld().rand.nextFloat(), 0, //0, 8F, 1.2F);
                        }
                    }
                }
            }
        }

        this.doDamageEntities(this.getRadius(), this.energy * 1000);

        ICBMSounds.EXPLOSION.play(world, this.position.x(), this.position.y(), this.position.z(),  7.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
    }

    @Override
    public void doExplode()
    {
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
                        Location targetPosition = this.position.add(new Pos(x, 0, z));

                        if (this.world().rand.nextFloat() < Math.max(0.001 * r, 0.05))
                        {
                            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, targetPosition.x(), targetPosition.y(), targetPosition.z(), 0, 0, 0); //5F, 1F);
                        }
                    }
                }
            }

        }
        else
        {
            if (this.thread != null)
            {
                if (this.thread.isComplete)
                {
                    this.controller.endExplosion();
                }
            }
            else
            {
                this.controller.endExplosion();
                ICBMClassic.INSTANCE.logger().error("Something went wrong with multi-threading while detonating the nuclear explosive.");
            }
        }
    }

    @Override
    public void doPostExplode()
    {
        try
        {
            if (!world().isRemote && this.thread.isComplete)
            {
                for (BlockPos p : this.thread.results)
                {
                    IBlockState state = this.world().getBlockState(p);
                    if (!state.getBlock().isAir(state, world(), p))
                    {
                        state.getBlock().onBlockExploded(this.world(), p, this);
                    }
                }
            }
        }
        catch (Exception e)
        {
            ICBMClassic.INSTANCE.logger().error("Nuclear-type detonation Failed!", e);
        }

        this.doDamageEntities(this.getRadius(), this.energy * 1000);

        if (this.isRadioactive)
        {
            new BlastRot(world(), this.exploder, position.x(), position.y(), position.z(), this.getRadius(), this.energy).explode();
            new BlastMutation(world(), this.exploder, position.x(), position.y(), position.z(), this.getRadius()).explode();

            if (this.world().rand.nextInt(3) == 0)
            {
                world().rainingStrength = 1f;
            }
        }

        ICBMSounds.EXPLOSION.play(world, this.position.x(), this.position.y(), this.position.z(), 10.0F, (1.0F + (this.world().rand.nextFloat() - this.world().rand.nextFloat()) * 0.2F) * 0.7F, true);
    }

    /**
     * The interval in ticks before the next procedural call of this explosive
     * <p>
     * return - Return -1 if this explosive does not need procedural calls
     */
    @Override
    public int proceduralInterval()
    {
        return 1;
    }

    @Override
    public long getEnergy()
    {
        return (long) (41840000 * this.energy);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.spawnMoreParticles = nbt.getBoolean("spawnMoreParticles");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setBoolean("spawnMoreParticles", this.spawnMoreParticles);

    }
}
