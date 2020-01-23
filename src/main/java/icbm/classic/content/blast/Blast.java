package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.NBTConstants;
import icbm.classic.api.caps.IMissile;
import icbm.classic.api.events.BlastBuildEvent;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.IBlastRestore;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.ConfigBlast;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.thread.ThreadExplosion;
import icbm.classic.content.blast.threaded.BlastAntimatter;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.transform.vector.Location;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Prefab for any Explosion/Blast object created
 */
public abstract class Blast extends Explosion implements IBlastInit, IBlastRestore
{
    //Thread stuff
    private ThreadExplosion thread;
    private ConcurrentLinkedQueue<BlockPos> threadResults;
    private boolean threadComplete = false;

    //TODO remove position as we are double storing location data
    public Location location;

    /**
     * Host of the blast
     */
    public Entity controller = null;

    /**
     * Is the blast alive, if false the blast is dead
     */
    public boolean isAlive = true;

    /**
     * The amount of times the explosion has been called
     */
    protected int callCount = 0;

    private boolean hasSetupBlast = false;

    private boolean hasBuilt = false;

    private IExplosiveData explosiveData;

    /**
     * Only use the default if you plan to init required data
     */
    public Blast()
    {
        super(null, null, 0, 0, 0, 0, false, false);
    }

    @Override
    public IExplosiveData getExplosiveData()
    {
        return explosiveData;
    }

    @Override
    public BlastState runBlast()
    {
        try
        {
            if (!this.world().isRemote)
            {
                //Forge event, allows for interaction and canceling the explosion
                if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, this))
                {
                    return BlastState.FORGE_EVENT_CANCEL;
                }

                //Play audio to confirm explosion triggered
                playExplodeSound();

                //Start explosion
                if (this instanceof IBlastTickable)
                {
                    if (!this.world().spawnEntity(new EntityExplosion(this)))
                    {
                        ICBMClassic.logger().error(this + " Failed to spawn explosion entity to control blast.");
                        isAlive = false;
                        return BlastState.ERROR; //TODO be more specific about error
                    }
                    return BlastState.TRIGGERED;
                }
                else
                {
                    //Do setup tasks
                    if (!this.doFirstSetup())
                    {
                        return BlastState.FORGE_EVENT_CANCEL;
                    }

                    //Call explosive, only complete if true
                    if (this.doExplode(-1))
                    {
                        this.completeBlast();
                    }
                }
            }
            else
            {
                clientRunBlast();
            }
            return BlastState.TRIGGERED;
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error(this + ": Unexpected error running blast", e);
            return BlastState.ERROR;
        }
    }

    //Handles client only stuff
    protected void clientRunBlast()
    {

    }

    /**
     * Called by ticking explosives
     *
     * @param ticksExisted
     * @return
     */
    public boolean onBlastTick(int ticksExisted)
    {
        if (!world.isRemote)
        {
            //Do setup work
            if (!doFirstSetup())
            {
                return false;
            }

            //Do ticks
            if (isAlive)
            {
                if (this.isCompleted() || this.doExplode(callCount++))
                {
                    completeBlast();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called to start the blast and run setup code
     *
     * @return true if the blast should continue to run, false otherwhise
     */
    public final boolean doFirstSetup()
    {
        if (isAlive && !hasSetupBlast)
        {
            hasSetupBlast = true;
            ExplosiveHandler.add(this);
            return this.setupBlast();
        }

        return true;
    }

    /**
     * @return true if the blast should continue to run, false otherwhise
     */
    protected boolean setupBlast()
    {
        return true;
    }

    /**
     * Called each tick of the blast
     *
     * @param callCount - call count or -1 to indicate this is not a ticking explosive
     * @return true to finish, false to continue/indicate the blast is doing something external like a thread
     */
    protected boolean doExplode(int callCount)
    {
        return false;
    }

    /**
     * Called to kill the blast and run last min code
     */
    public final void completeBlast()
    {
        if (isAlive)
        {
            //Mark as dead to prevent blast running
            isAlive = false;

            //Remove from tracker
            ExplosiveHandler.remove(this);

            //Run post code
            this.onBlastCompleted();
        }
    }

    /**
     * Internal call for running post blast code
     * Do not se the entity or blast dead. This is completed
     * in the {@link #completeBlast()} method.
     */
    protected void onBlastCompleted()
    {
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
        setPosition(posX, posY, posZ);
    }

    /**
     * Called to set the position of the blast. Only call this for initialization, anything
     * that happens during world tick should call {@link #onPositionUpdate(double, double, double)}
     *
     * @param posX
     * @param posY
     * @param posZ
     */
    public Blast setPosition(double posX, double posY, double posZ)
    {
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        location = new Location(world, posX, posY, posZ);
        //TODO super contains a vec3 also called position, we need to set that value instead of overriding the return
        return this;
    }

    @Override
    public Vec3d getPosition()
    {
        return this.location.toVec3d();
    }

    /**
     * Make the default functions useless.
     */
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

    protected boolean doDamageEntities(float radius, float power)
    {
        return this.doDamageEntities(radius, power, true);
    }

    /**
     * @return true if the method ran successfully, false if it was interrupted
     */
    protected boolean doDamageEntities(float radius, float power, boolean destroyItem)
    {
        // Step 2: Damage all entities
        radius *= 2.0F;
        Location minCoord = location.add(-radius - 1);
        Location maxCoord = location.add(radius + 1);
        List<Entity> allEntities = world().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(minCoord.xi(), minCoord.yi(), minCoord.zi(), maxCoord.xi(), maxCoord.yi(), maxCoord.zi()));
        Vec3d var31 = new Vec3d(location.x(), location.y(), location.z());

        if (!ConfigBlast.ANTIMATTER_BLOCK_AND_ENT_DAMAGE_ON_REDMATTER && this instanceof BlastAntimatter)
        {
            allEntities.sort((e1, e2) -> {
                if (e2 instanceof EntityExplosion && ((EntityExplosion) e2).getBlast() instanceof BlastRedmatter)
                {
                    return 1; //put red matter at the front
                }
                else
                {
                    return -1;
                }
            });

            if (onDamageEntity(allEntities.get(0))) //remove red matter blast and stop doing anything else
            {
                return false;
            }
        }

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

            double distance = entity.getDistance(location.x(), location.y(), location.z()) / radius;

            if (distance <= 1.0D)
            {
                double xDifference = entity.posX - location.x();
                double yDifference = entity.posY - location.y();
                double zDifference = entity.posZ - location.z();
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

        return true;
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

    @Override
    public void load(NBTTagCompound nbt)
    {
        this.callCount = nbt.getInteger(NBTConstants.CALL_COUNT);
        this.size = nbt.getFloat(NBTConstants.EXPLOSION_SIZE);

        if (world instanceof WorldServer)
        {
            exploder = ((WorldServer) world).getEntityFromUuid(nbt.getUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID));
        }
    }

    @Override
    public void save(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.CALL_COUNT, this.callCount);
        nbt.setFloat(NBTConstants.EXPLOSION_SIZE, this.size);

        if (world instanceof WorldServer)
        {
            nbt.setUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID, this.exploder.getUniqueID());
        }
    }

    public boolean isMovable()
    {
        return false;
    }

    @Override
    public World world()
    {
        return world;
    }

    @Override
    public double x()
    {
        return this.location.x();
    }

    @Override
    public double y()
    {
        return this.location.y();
    }

    @Override
    public double z()
    {
        return this.location.z();
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

    //================================================
    //=====             Properties              ======
    //================================================

    @Override
    public IBlastInit setBlastSource(Entity entity)
    {
        checkBuilt();
        this.exploder = entity;
        return this;
    }

    @Override
    public Blast setBlastSize(double power)
    {
        checkBuilt();
        this.size = (float) power;
        return this;
    }

    @Override
    public Blast scaleBlast(double scale)
    {
        checkBuilt();
        this.size *= scale;
        return this;
    }

    @Override
    public Blast setBlastWorld(World world)
    {
        checkBuilt();
        this.world = world;
        return this;
    }

    @Override
    public Blast setBlastPosition(double posX, double posY, double posZ)
    {
        checkBuilt();
        setPosition(posX, posY, posZ);
        return this;
    }

    @Override
    public IBlastInit setExplosiveData(IExplosiveData data)
    {
        checkBuilt();
        this.explosiveData = data;
        return this;
    }

    @Override
    public IBlastInit setCustomData(@Nonnull NBTTagCompound customData)
    {
        return this;
    }

    @Override
    public boolean isCompleted()
    {
        return !isAlive;
    }

    @Override
    public IBlastInit setEntityController(Entity entityController)
    {
        this.controller = entityController;
        return this;
    }

    @Nullable
    @Override
    public Entity getController()
    {
        return controller;
    }

    private final void checkBuilt()
    {
        if (hasBuilt)
        {
            throw new RuntimeException("Can not init properties of a blast after it has built");
        }
    }

    @Override
    public Blast buildBlast()
    {
        if (hasBuilt)
        {
            throw new RuntimeException("Blast has already been built");
        }
        MinecraftForge.EVENT_BUS.post(new BlastBuildEvent(this));
        hasBuilt = true;
        return this;
    }

    @Override
    public void clearBlast()
    {
        if (getThread() != null)
        {
            getThread().kill();
        }
        isAlive = false;
    }
}
