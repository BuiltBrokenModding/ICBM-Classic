package icbm.classic.content.explosive.blast;

import com.builtbroken.mc.imp.transform.vector.Location;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.thread.ThreadLargeExplosion;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
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
        if (!this.oldWorld().isRemote)
        {
            this.thread = new ThreadLargeExplosion(this.position, (int) this.getRadius(), this.energy, this.exploder);

            this.thread.start();

        }
        else if (this.spawnMoreParticles && ICBMClassic.proxy.isGaoQing())
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
                        double distance = MathHelper.sqrt_double(x * x + z * z);

                        if (r > distance && r - 3 < distance)
                        {
                            Location spawnPosition = position.add(new Pos(x * 2, (y - 2) * 2, z * 2));
                            float xDiff = (float) (spawnPosition.x() - position.x());
                            float zDiff = (float) (spawnPosition.z() - position.z());
                            ICBMClassic.proxy.spawnParticle("smoke", oldWorld(), spawnPosition, xDiff * 0.3 * oldWorld().rand.nextFloat(), -oldWorld().rand.nextFloat(), zDiff * 0.3 * oldWorld().rand.nextFloat(), (float) (distance / this.getRadius()) * oldWorld().rand.nextFloat(), 0, 0, 8F, 1.2F);
                        }
                    }
                }
            }
        }

        this.doDamageEntities(this.getRadius(), this.energy * 1000);

        this.oldWorld().playSoundEffect(this.position.x(), this.position.y(), this.position.z(), ICBMClassic.PREFIX + "explosion", 7.0F, (1.0F + (this.oldWorld().rand.nextFloat() - this.oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
    }

    @Override
    public void doExplode()
    {
        int r = this.callCount;

        if (this.oldWorld().isRemote)
        {
            if (ICBMClassic.proxy.isGaoQing())
            {
                for (int x = -r; x < r; x++)
                {
                    for (int z = -r; z < r; z++)
                    {
                        double distance = MathHelper.sqrt_double(x * x + z * z);

                        if (distance < r && distance > r - 1)
                        {
                            Location targetPosition = this.position.add(new Pos(x, 0, z));

                            if (this.oldWorld().rand.nextFloat() < Math.max(0.001 * r, 0.05))
                            {
                                ICBMClassic.proxy.spawnParticle("smoke", this.oldWorld(), targetPosition, 5F, 1F);
                            }
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
            if (!this.oldWorld().isRemote && this.thread.isComplete)
            {
                for (Pos p : this.thread.results)
                {
                    Block block = this.oldWorld().getBlock(p.xi(), p.yi(), p.zi());
                    if (block != null)
                        block.onBlockExploded(this.oldWorld(), p.xi(), p.yi(), p.zi(), this);

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
            new BlastRot(oldWorld(), this.exploder, position.x(), position.y(), position.z(), this.getRadius(), this.energy).explode();
            new BlastMutation(oldWorld(), this.exploder, position.x(), position.y(), position.z(), this.getRadius()).explode();

            if (this.oldWorld().rand.nextInt(3) == 0)
            {
                oldWorld().rainingStrength = 1f;
            }
        }

        this.oldWorld().playSoundEffect(this.position.x(), this.position.y(), this.position.z(), ICBMClassic.PREFIX + "explosion", 10.0F, (1.0F + (this.oldWorld().rand.nextFloat() - this.oldWorld().rand.nextFloat()) * 0.2F) * 0.7F);
    }

    /** The interval in ticks before the next procedural call of this explosive
     *
     * return - Return -1 if this explosive does not need procedural calls */
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
