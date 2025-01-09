package icbm.classic.content.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.events.BlastBuildEvent;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.IBlastRestore;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.ConfigDebug;
import icbm.classic.content.blast.thread.ThreadExplosion;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.actions.WorkTickingActionHandler;
import icbm.classic.lib.actions.status.ActionResponses;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Prefab for any Explosion/Blast object created
 */
@Deprecated
public abstract class Blast extends Explosion implements IBlastInit, IBlastRestore
{
    //Thread stuff
    private ThreadExplosion thread;
    protected ConcurrentLinkedQueue<BlockPos> threadResults;
    private boolean threadComplete = false;

    //TODO remove position as we are double storing location data
    public BlockPos pos;

    /**
     * Host of the blast
     */
    public Entity controller = null;

    /**
     * Is the blast alive, if false the blast is dead
     */
    private boolean isAlive = true;

    /**
     * The amount of times the explosion has been called
     */
    protected int callCount = 0;

    private boolean hasSetupBlast = false;

    private boolean hasBuilt = false;

    private IExplosiveData explosiveData;
    private IActionSource actionSource;

    /**
     * Only use the default if you plan to init required data
     */
    public Blast()
    {
        super(null, null, 0, 0, 0, 0, false, Mode.NONE);
    }

    public Blast(World world, double x, double y, double z) {
        this();
        setBlastWorld(world);
        setBlastPosition(x, y, z);
    }

    @Override
    @Nonnull
    public IActionSource getSource() {
        return this.actionSource;
    }

    /**
     * Gets data used to create this action
     *
     * @return data
     */
    @Override
    @Nonnull
    public IExplosiveData getActionData() {
        return explosiveData;
    }

    @Override
    public IBlastInit setActionSource(IActionSource source) {
        this.actionSource = source;
        return this;
    }

    @Nonnull
    @Override
    public IActionStatus doAction()
    {
        try
        {
            if (!this.getWorld().isRemote)
            {
                //Forge event, allows for interaction and canceling the explosion
                if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(world, this))
                {
                    return ActionResponses.EXPLOSION_CANCELED;
                }

                //Play audio to confirm explosion triggered
                playExplodeSound();

                //Start explosion
                if (this instanceof IBlastTickable)
                {
                    if (!this.getWorld().addEntity(EntityReg.TICKING_EXPLOSION.get().create(world)))
                    {
                        isAlive = false;
                        return ActionResponses.ENTITY_SPAWN_FAILED;
                    }
                    return ActionResponses.COMPLETED;
                }
                else
                {
                    //Do setup tasks
                    if (!this.doFirstSetup())
                    {
                        return BlastStatus.SETUP_ERROR; //TODO specify why we failed during setup
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
            return ActionResponses.COMPLETED;
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error(this + ": Unexpected error running blast", e);
            endBlast();
            return ActionResponses.UNKNOWN_ERROR; //TODO provide dynamic data
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
    @Deprecated // TODO inline with runBlast to provide proper feedback using IActionStatus
    public final boolean doFirstSetup()
    {
        if (isAlive && !hasSetupBlast)
        {
            hasSetupBlast = true;
            WorkTickingActionHandler.add(this);
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
            this.endBlast();

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
        clearBlast();
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
        this.pos = new BlockPos(posX, posY, posZ);
        return this;
    }

    @Override
    public Vec3d getPosition()
    {
        return new Vec3d(this.x, this.y, this.z);
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
        this.world.playSound((PlayerEntity) null,
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

    protected List<Entity> getEntities(double radius) {
        return getWorld().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(
            x - radius,
            y - radius,
            z - radius,
            x + radius,
            y + radius,
            z + radius
        ));
    }

    /**
     * @return true if the method ran successfully, false if it was interrupted
     */
    protected boolean doDamageEntities(float radius, float power, boolean destroyItem)
    {
        final List<Entity> allEntities = getEntities(radius * 2);
        return doDamageEntities(allEntities, radius, power, destroyItem);
    }

    protected boolean  doDamageEntities(List<Entity> entities, float radius, float power, boolean destroyItem) {
        final Vec3d center = this.getPosition();
        for (Entity entity : entities) {
            if (this.onDamageEntity(entity)) {
                continue;
            }

            if (entity instanceof ItemEntity && !destroyItem) {
                continue;
            }

            double distance = entity.getDistanceSq(center.x, center.y, center.z) / radius;

            if (distance <= 1.0D) {
                double xDifference = entity.posX - center.x;
                double yDifference = entity.posY - center.y;
                double zDifference = entity.posZ - center.z;

                double mag = MathHelper.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference); //TODO switch to sq for better speed

                xDifference /= mag;
                yDifference /= mag;
                zDifference /= mag;

                double blockProtection = getBlockDensity(center, entity);
                double exScale = (1.0D - distance) * blockProtection;
                int damage = (int) ((exScale * exScale + exScale) / 2.0D * 8.0D * power + 1.0D);

                entity.attackEntityFrom(getDamageSource(), damage);

                // Knock back
                if (entity instanceof LivingEntity) {
                    exScale = ProtectionEnchantment.getBlastDamageReduction((LivingEntity)entity, exScale);
                }
                entity.setMotion(entity.getMotion().add(xDifference * exScale, yDifference * exScale, zDifference * exScale));
            }
        }

        return true;
    }

    public static float getBlockDensity(Vec3d center, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
        if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
            int i = 0;
            int j = 0;

            for(float f = 0.0F; f <= 1.0F; f = (float)((double)f + d0)) {
                for(float f1 = 0.0F; f1 <= 1.0F; f1 = (float)((double)f1 + d1)) {
                    for(float f2 = 0.0F; f2 <= 1.0F; f2 = (float)((double)f2 + d2)) {
                        double d5 = MathHelper.lerp((double)f, axisalignedbb.minX, axisalignedbb.maxX);
                        double d6 = MathHelper.lerp((double)f1, axisalignedbb.minY, axisalignedbb.maxY);
                        double d7 = MathHelper.lerp((double)f2, axisalignedbb.minZ, axisalignedbb.maxZ);
                        Vec3d vec3d = new Vec3d(d5 + d3, d6, d7 + d4);
                        if (entity.world.rayTraceBlocks(new RayTraceContext(vec3d, center, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, entity)).getType() == RayTraceResult.Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }

    public DamageSource getDamageSource() {
        return DamageSource.causeExplosionDamage(this);
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
    public void load(CompoundNBT nbt)
    {
        this.callCount = nbt.getInt(NBTConstants.CALL_COUNT);
        this.size = nbt.getFloat(NBTConstants.EXPLOSION_SIZE);

        if (world instanceof ServerWorld && nbt.hasUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID)) //don't load the exploder if it hasn't been saved
        {
            exploder = ((ServerWorld) world).getEntityByUuid(nbt.getUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID));
        }
    }

    @Override
    public void save(CompoundNBT nbt)
    {
        nbt.putInt(NBTConstants.CALL_COUNT, this.callCount);
        nbt.putFloat(NBTConstants.EXPLOSION_SIZE, this.size);

        if (world instanceof ServerWorld && exploder != null) //don't save the exploder if there is none to save. TODO: do we even need to save it at all?
        {
            nbt.putUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID, this.exploder.getUniqueID());
        }
    }

    public boolean isMovable()
    {
        return false;
    }

    @Override
    public World getWorld() {
        return world;
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
    public IBlastInit setCustomData(@Nonnull CompoundNBT customData)
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
    public Entity getEntity()
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
       endBlast();
    }

    protected void endBlast() {
        isAlive = false;
        WorkTickingActionHandler.remove(this);

        if (getThread() != null)
        {
            getThread().kill();
        }
        if (controller != null)
        {
            controller.remove();
        }
    }
}
