package icbm.classic.content.explosive.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.IMissile;
import icbm.classic.client.models.ModelICBM;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.explosive.ExplosiveHandler;
import icbm.classic.content.explosive.thread.ThreadExplosion;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Prefab for any Explosion/Blast object created
 */
public abstract class Blast extends Explosion implements IBlast
{
    //Thread stuff
    private ThreadExplosion thread;
    private ConcurrentLinkedQueue<BlockPos> threadResults;
    private boolean threadComplete = false;

    //TODO remove position as we are double storing location data
    public Location position;

    /** Host of the blast */
    public EntityExplosion controller = null;

    /** Is the blast alive, if false the blast is dead */
    public boolean isAlive = true;

    /** The amount of times the explosion has been called */
    protected int callCount = 0;

    private boolean preExplode = false;

    public Blast(World world, Entity entity, double x, double y, double z, float size)
    {
        super(world, entity, x, y, z, size, false, true);
        this.position = new Location(world, x, y, z);
    }

    public Blast(Location pos, Entity entity, float size)
    {
        super(pos.world(), entity, pos.x(), pos.y(), pos.z(), size, false, true);
        this.position = pos;
    }

    public Blast(Entity entity, float size)
    {
        super(entity.world, entity, entity.posX, entity.posY, entity.posZ, size, false, true);
        this.position = new Location(entity);
    }

    /**
     * Internal call to run setup code for the blast
     */
    protected void doPreExplode()
    {
    }

    /**
     * Called to start the blast and run setup code
     */
    public final void preExplode()
    {
        debugEx(String.format("Blast#preExplode() -> Blast: %s, IsAlive: %s, HasBeenCalledBefore: %s", this, isAlive, preExplode));
        if (isAlive && !preExplode)
        {
            preExplode = true;
            ExplosiveHandler.add(this);
            this.doPreExplode();
        }
    }

    /**
     * Internal call to run the blast code
     */
    protected abstract void doExplode();

    /**
     * Called to trigger the main blast code
     */
    public final void onExplode()
    {
        if (!preExplode)
        {
            debugEx(String.format("Blast#onExplode() -> preExplode() was never called, Blast: %s, IsAlive: %s, CallCount: %s", this, isAlive, callCount));
            preExplode();
        }
        debugEx(String.format("Blast#onExplode() -> Blast: %s, IsAlive: %s, CallCount: %s", this, isAlive, callCount));
        if (isAlive)
        {
            this.doExplode();
            this.callCount++;
        }
    }

    /**
     * Internal call for running post blast code
     * Do not se the entity or blast dead. This is completed
     * in the {@link #postExplode()} method.
     */
    protected void doPostExplode()
    {
    }

    /**
     * Called to kill the blast and run last min code
     */
    public final void postExplode()
    {
        debugEx(String.format("Blast#postExplode() -> Blast: %s, IsAlive: %s", this, isAlive));
        if (isAlive)
        {
            //Mark as dead to prevent blast running
            isAlive = false;

            //Remove from tracker
            ExplosiveHandler.remove(this);

            //Run post code
            this.doPostExplode();
        }
    }

    /**
     * Called each tick the blast has moved
     *
     * @param posX
     * @param posY
     * @param posZ
     */
    public void onPositionUpdate(double posX, double posY, double posZ)
    {
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        position = new Location(world(), posX, posY, posZ);
        debugEx(String.format("Blast#onPositionUpdate(%s, %s, %s) -> position has been updated, Blast: %s", this, posX, posY, posZ));
    }

    /** Make the default functions useless. */
    @Override
    public void doExplosionA()
    {
        //Empty to cancel MC code
        ICBMClassic.logger().error("Blast#doExplosionA() -> Something called the vanilla explosion method. This is not a supported behavior for ICBM explosions. Blast: " + this, new RuntimeException());
    }

    @Override
    public void doExplosionB(boolean par1)
    {
        //Empty to cancel MC code
        ICBMClassic.logger().error("Blast#doExplosionB(" + par1 + ") -> Something called the vanilla explosion method. This is not a supported behavior for ICBM explosions. Blast: " + this, new RuntimeException());
    }

    /**
     * Called to trigger the explosion
     */
    public void runBlast()
    {
        //Forge event, allows for interaction and canceling the explosion
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, this))
        {
            return;
        }

        //Play audio to confirm explosion triggered
        playExplodeSound();

        //Start explosion
        if (this.proceduralInterval() > 0)
        {
            debugEx("Blast#explode() -> Triggering interval based explosion, Blast: " + this);
            if (!this.world().isRemote)
            {
                if (!this.world().spawnEntity(new EntityExplosion(this)))
                {
                    ICBMClassic.logger().error("Blast#explode() -> Failed to spawn explosion entity to control blast, Blast: " + this);
                    isAlive = false;
                }
            }
        }
        else
        {
            debugEx("Blast#explode() -> Triggering full explosion, Blast: " + this);
            doRunBlast();
        }
    }

    protected void doRunBlast()
    {
        this.preExplode();
        this.doExplode();
        this.postExplode();
    }

    protected void playExplodeSound()
    {
        this.world.playSound((EntityPlayer) null,
                this.x, this.y, this.z,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.BLOCKS,
                4.0F,
                (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);
    }

    /**
     * Radius of the blast
     *
     * @return
     */
    public float getBlastRadius()
    {
        return Math.max(3, this.size);
    }

    /**
     * The interval in ticks before the next procedural call of this explosive
     *
     * @return - Return -1 if this explosive does not need procedural calls
     */
    public int proceduralInterval()
    {
        return -1;
    }

    protected void doDamageEntities(float radius, float power)
    {
        this.doDamageEntities(radius, power, true);
    }

    protected void doDamageEntities(float radius, float power, boolean destroyItem)
    {
        // Step 2: Damage all entities
        radius *= 2.0F;
        Location minCoord = position.add(-radius - 1);
        Location maxCoord = position.add(radius + 1);
        List<Entity> allEntities = world().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(minCoord.xi(), minCoord.yi(), minCoord.zi(), maxCoord.xi(), maxCoord.yi(), maxCoord.zi()));
        Vec3d var31 = new Vec3d(position.x(), position.y(), position.z());

        for (int i = 0; i < allEntities.size(); ++i)
        {
            Entity entity = allEntities.get(i);

            if (this.onDamageEntity(entity))
            {
                continue;
            }

            if (entity instanceof IMissile)
            {
                ((IMissile) entity).destroyMissile(true);
                continue;
            }

            if (entity instanceof EntityItem && !destroyItem)
            {
                continue;
            }

            double distance = entity.getDistance(position.x(), position.y(), position.z()) / radius;

            if (distance <= 1.0D)
            {
                double xDifference = entity.posX - position.x();
                double yDifference = entity.posY - position.y();
                double zDifference = entity.posZ - position.z();
                double var35 = MathHelper.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference);
                xDifference /= var35;
                yDifference /= var35;
                zDifference /= var35;
                double var34 = world().getBlockDensity(var31, entity.getEntityBoundingBox());
                double var36 = (1.0D - distance) * var34;
                int damage = 0;

                damage = (int) ((var36 * var36 + var36) / 2.0D * 8.0D * power + 1.0D);

                entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), damage);

                entity.motionX += xDifference * var36;
                entity.motionY += yDifference * var36;
                entity.motionZ += zDifference * var36;
            }
        }
    }

    /**
     * Called by doDamageEntity on each entity being damaged. This function should be inherited if
     * something special is to happen to a specific entity.
     *
     * @return True if something special happens to this specific entity.
     */
    protected boolean onDamageEntity(Entity entity)
    {
        return false;
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        this.callCount = nbt.getInteger("callCount");
        this.size = nbt.getFloat("explosionSize");
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("callCount", this.callCount);
        nbt.setFloat("explosionSize", this.size);
    }

    public boolean isMovable()
    {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public ModelICBM getRenderModel()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getRenderResource()
    {
        return null;
    }

    @Override
    public World world()
    {
        return this.position.world();
    }

    @Override
    public double x()
    {
        return this.position.x();
    }

    @Override
    public double y()
    {
        return this.position.y();
    }

    @Override
    public double z()
    {
        return this.position.z();
    }

    @Override
    public Entity getBlastSource()
    {
        return getBlastSource();
    }

    protected void createAndStartThread(ThreadExplosion thread)
    {
        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            ICBMClassic.logger().info("Blast#createAndStartThread(" + thread + ") -> Thread set");
        }

        if (this.thread != null && !this.thread.isComplete)
        {
            ICBMClassic.logger().info("Blast#createAndStartThread(" + thread + ") -> Error new thread was set before last finished\nLast: " + thread);
        }

        //Store thread instance
        this.thread = thread;

        //Reset thread state
        this.threadComplete = false;

        //Start thread
        this.thread.start();

        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            ICBMClassic.logger().info("Blast#createAndStartThread(" + thread + ") -> Thread started: " + thread.isAlive());
        }
    }

    protected boolean isThreadCompleted()
    {
        return threadComplete || thread != null && thread.isComplete;
    }

    /**
     * Called from the explosive thread to mark as completed
     *
     * @param exThread
     */
    public synchronized void markThreadCompleted(ThreadExplosion exThread) //This method is a work around for thread instance going null
    {
        //Debug
        if (ConfigDebug.DEBUG_THREADS)
        {
            ICBMClassic.logger().info("Blast#markThreadCompleted(" + exThread + ") -> Thread responded that is has completed, Blast: " + this);
        }
        if (thread == null || thread == exThread)
        {
            threadComplete = true;
        }
        else
        {
            ICBMClassic.logger().info("Blast#markThreadCompleted(" + exThread + ") -> Error thread attempted to mark for complete but did not match current thread \nCurrent: " + thread + "\nBlast: " + this);
        }
    }

    public void addThreadResult(BlockPos pos)
    {
        getThreadResults().add(pos);
    }

    protected ConcurrentLinkedQueue getThreadResults()
    {
        if (threadResults == null)
        {
            threadResults = new ConcurrentLinkedQueue();
        }
        return threadResults;
    }

    public ThreadExplosion getThread()
    {
        return thread;
    }

    protected final void debugEx(String msg)
    {
        if (ConfigDebug.DEBUG_EXPLOSIVES)
        {
            ICBMClassic.logger().info(msg);
        }
    }
}
