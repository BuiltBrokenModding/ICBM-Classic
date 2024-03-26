package icbm.classic.world.blast;

import icbm.classic.ICBMClassic;
import icbm.classic.api.events.BlastBuildEvent;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.IBlastRestore;
import icbm.classic.api.explosion.IBlastTickable;
import icbm.classic.api.explosion.responses.BlastForgeResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.config.ConfigDebug;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.world.blast.thread.ThreadExplosion;
import icbm.classic.world.entity.ExplosionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.common.MinecraftForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Prefab for any Explosion/Blast object created
 */
public abstract class Blast extends Explosion implements IBlastInit, IBlastRestore {

    private final Level level;
    //Thread stuff
    private ThreadExplosion thread;
    private ConcurrentLinkedQueue<BlockPos> threadResults;
    private boolean threadComplete = false;

    //TODO remove position as we are double storing location data
    public Vec3 location;

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

    private ExplosiveType explosiveData;

    /**
     * Only use the default if you plan to init required data
     */
    public Blast(Level level,
                 @Nullable Entity source,
                 @Nullable DamageSource damageSource,
                 @Nullable ExplosionDamageCalculator damageCalculator,
                 double x,
                 double y,
                 double z,
                 float radius,
                 boolean fire,
                 Explosion.BlockInteraction blockInteraction,
                 ParticleOptions smallExplosionParticles,
                 ParticleOptions largeExplosionParticles,
                 SoundEvent explosionSound) {
        super(level, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction, smallExplosionParticles, largeExplosionParticles, explosionSound);
        this.level = level;
    }

    @Override
    public ExplosiveType getExplosiveData() {
        return explosiveData;
    }

    @Override
    public BlastResponse runBlast() {
        try {
            if (!this.level().isClientSide()) {
                //Forge event, allows for interaction and canceling the explosion
                if (net.neoforged.event.ForgeEventFactory.onExplosionStart(level(), this)) {
                    return BlastForgeResponses.EXPLOSION_EVENT.get();
                }

                //Play audio to confirm explosion triggered
                playExplodeSound();

                //Start explosion
                if (this instanceof IBlastTickable) {
                    if (!this.level().spawnEntity(new ExplosionEntity(this))) {
                        isAlive = false;
                        return BlastForgeResponses.ENTITY_SPAWNING.get();
                    }
                    return BlastState.TRIGGERED.genericResponse;
                } else {
                    //Do setup tasks
                    if (!this.doFirstSetup()) {
                        return BlastState.CANCLED.genericResponse; //TODO specify why
                    }

                    //Call explosive, only complete if true
                    if (this.doExplode(-1)) {
                        this.completeBlast();
                    }
                }
            } else {
                clientRunBlast();
            }
            return BlastState.TRIGGERED.genericResponse;
        } catch (Exception e) {
            ICBMClassic.logger().error(this + ": Unexpected error running blast", e);
            return new BlastResponse(BlastState.ERROR, e.getMessage(), e);
        }
    }

    //Handles client only stuff
    protected void clientRunBlast() {

    }

    /**
     * Called by ticking explosives
     *
     * @param ticksExisted
     * @return
     */
    public boolean onBlastTick(int ticksExisted) {
        if (!world.isClientSide()) {
            //Do setup work
            if (!doFirstSetup()) {
                return false;
            }

            //Do ticks
            if (isAlive) {
                if (this.isCompleted() || this.doExplode(callCount++)) {
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
    public final boolean doFirstSetup() {
        if (isAlive && !hasSetupBlast) {
            hasSetupBlast = true;
            ExplosiveHandler.add(this);
            return this.setupBlast();
        }

        return true;
    }

    /**
     * @return true if the blast should continue to run, false otherwhise
     */
    protected boolean setupBlast() {
        return true;
    }

    /**
     * Called each tick of the blast
     *
     * @param callCount - call count or -1 to indicate this is not a ticking explosive
     * @return true to finish, false to continue/indicate the blast is doing something external like a thread
     */
    protected boolean doExplode(int callCount) {
        return false;
    }

    /**
     * Called to kill the blast and run last min code
     */
    public final void completeBlast() {
        if (isAlive) {
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
    protected void onBlastCompleted() {
        clearBlast();
    }

    /**
     * Called each tick the blast has moved
     *
     * @param posX
     * @param posY
     * @param posZ
     */
    public void onPositionUpdate(double posX, double posY, double posZ) {
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
    public Blast setPosition(double posX, double posY, double posZ) {
        this.x = posX;
        this.y = posY;
        this.z = posZ;
        location = new Location(world, posX, posY, posZ);
        //TODO super contains a vec3 also called position, we need to set that value instead of overriding the return
        return this;
    }

    @Override
    public Vec3 getPosition() {
        return this.location.toVec3();
    }

    /**
     * Make the default functions useless.
     */
    @Override
    public void doExplosionA() {
        //Empty to cancel MC code
        ICBMClassic.logger().error("Blast#doExplosionA() -> Something called the vanilla explosion method. This is not a supported behavior for ICBM explosions. Blast: " + this, new RuntimeException());
    }

    @Override
    public void doExplosionB(boolean par1) {
        //Empty to cancel MC code
        ICBMClassic.logger().error("Blast#doExplosionB(" + par1 + ") -> Something called the vanilla explosion method. This is not a supported behavior for ICBM explosions. Blast: " + this, new RuntimeException());
    }

    protected void playExplodeSound() {
        this.world.playSound((Player) null,
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
    public float getBlastRadius() {
        return Math.max(3, this.size);
    }

    protected boolean doDamageEntities(float radius, float power) {
        return this.doDamageEntities(radius, power, true);
    }

    protected List<Entity> getEntities(double radius) {
        Location minCoord = location.add(-radius - 1); //TODO drop need for location
        Location maxCoord = location.add(radius + 1);
        return level().getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(minCoord.xi(), minCoord.yi(), minCoord.zi(), maxCoord.xi(), maxCoord.yi(), maxCoord.zi()));
    }

    /**
     * @return true if the method ran successfully, false if it was interrupted
     */
    protected boolean doDamageEntities(float radius, float power, boolean destroyItem) {
        final List<Entity> allEntities = getEntities(radius * 2);
        return doDamageEntities(allEntities, radius, power, destroyItem);
    }

    protected boolean doDamageEntities(List<Entity> entities, float radius, float power, boolean destroyItem) {
        final Vec3 center = new Vec3(location.x(), location.y(), location.z());
        for (Entity entity : entities) {
            if (this.onDamageEntity(entity)) {
                continue;
            }

            if (entity instanceof ItemEntity && !destroyItem) {
                continue;
            }

            double distance = entity.getDistance(location.x(), location.y(), location.z()) / radius;

            if (distance <= 1.0D) {
                double xDifference = entity.getX() - location.x();
                double yDifference = entity.getY() - location.y();
                double zDifference = entity.getZ() - location.z();

                double mag = MathHelper.sqrt(xDifference * xDifference + yDifference * yDifference + zDifference * zDifference); //TODO switch to sq for better speed

                xDifference /= mag;
                yDifference /= mag;
                zDifference /= mag;

                double var34 = level().getBlockDensity(center, entity.getEntityBoundingBox());
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
    protected boolean onDamageEntity(Entity entity) {
        return false;
    }

    @Override
    public void load(CompoundTag tag) {
        this.callCount = tag.getInt(NBTConstants.CALL_COUNT);
        this.size = tag.getFloat(NBTConstants.EXPLOSION_SIZE);

        if (world instanceof WorldServer && tag.hasUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID)) //don't load the exploder if it hasn't been saved
        {
            exploder = ((WorldServer) world).getEntityFromUuid(tag.getUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID));
        }
    }

    @Override
    public void save(CompoundTag nbt) {
        nbt.setInteger(NBTConstants.CALL_COUNT, this.callCount);
        nbt.setFloat(NBTConstants.EXPLOSION_SIZE, this.size);

        if (world instanceof WorldServer && exploder != null) //don't save the exploder if there is none to save. TODO: do we even need to save it at all?
        {
            nbt.setUniqueId(NBTConstants.BLAST_EXPLODER_ENT_ID, this.exploder.getUniqueID());
        }
    }

    public boolean isMovable() {
        return false;
    }

    @Override
    public Level level() {
        return this.level;
    }

    @Override
    public double x() {
        return this.location.x();
    }

    @Override
    public double y() {
        return this.location.y();
    }

    @Override
    public double z() {
        return this.location.z();
    }

    @Override
    public Entity getBlastSource() {
        return getBlastSource();
    }

    protected void createAndStartThread(ThreadExplosion thread) {
        //Debug
        if (ConfigDebug.DEBUG_THREADS) {
            ICBMClassic.logger().info("Blast#createAndStartThread(" + thread + ") -> Thread set");
        }

        if (this.thread != null && !this.thread.isComplete) {
            ICBMClassic.logger().info("Blast#createAndStartThread(" + thread + ") -> Error new thread was set before last finished\nLast: " + thread);
        }

        //Store thread instance
        this.thread = thread;

        //Reset thread state
        this.threadComplete = false;

        //Start thread
        this.thread.start();

        //Debug
        if (ConfigDebug.DEBUG_THREADS) {
            ICBMClassic.logger().info("Blast#createAndStartThread(" + thread + ") -> Thread started: " + thread.isAlive());
        }
    }

    protected boolean isThreadCompleted() {
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
        if (ConfigDebug.DEBUG_THREADS) {
            ICBMClassic.logger().info("Blast#markThreadCompleted(" + exThread + ") -> Thread responded that is has completed, Blast: " + this);
        }
        if (thread == null || thread == exThread) {
            threadComplete = true;
        } else {
            ICBMClassic.logger().info("Blast#markThreadCompleted(" + exThread + ") -> Error thread attempted to mark for complete but did not match current thread \nCurrent: " + thread + "\nBlast: " + this);
        }
    }

    public void addThreadResult(BlockPos pos) {
        getThreadResults().add(pos);
    }

    protected ConcurrentLinkedQueue getThreadResults() {
        if (threadResults == null) {
            threadResults = new ConcurrentLinkedQueue();
        }
        return threadResults;
    }

    public ThreadExplosion getThread() {
        return thread;
    }

    //================================================
    //=====             Properties              ======
    //================================================

    @Override
    public IBlastInit setBlastSource(Entity entity) {
        checkBuilt();
        this.exploder = entity;
        return this;
    }

    @Override
    public Blast setBlastSize(double power) {
        checkBuilt();
        this.size = (float) power;
        return this;
    }

    @Override
    public Blast scaleBlast(double scale) {
        checkBuilt();
        this.size *= scale;
        return this;
    }

    @Override
    public Blast setBlastLevel(Level level) {
        checkBuilt();
        this.level = level;
        return this;
    }

    @Override
    public Blast setBlastPosition(double posX, double posY, double posZ) {
        checkBuilt();
        setPosition(posX, posY, posZ);
        return this;
    }

    @Override
    public IBlastInit setExplosiveData(ExplosiveType data) {
        checkBuilt();
        this.explosiveData = data;
        return this;
    }

    @Override
    public IBlastInit setCustomData(@Nonnull CompoundTag customData) {
        return this;
    }

    @Override
    public boolean isCompleted() {
        return !isAlive;
    }

    @Override
    public IBlastInit setEntityController(Entity entityController) {
        this.controller = entityController;
        return this;
    }

    @Nullable
    @Override
    public Entity getEntity() {
        return controller;
    }

    private final void checkBuilt() {
        if (hasBuilt) {
            throw new RuntimeException("Can not init properties of a blast after it has built");
        }
    }

    @Override
    public Blast buildBlast() {
        if (hasBuilt) {
            throw new RuntimeException("Blast has already been built");
        }
        MinecraftForge.EVENT_BUS.post(new BlastBuildEvent(this));
        hasBuilt = true;
        return this;
    }

    @Override
    public void clearBlast() {
        if (getThread() != null) {
            getThread().kill();
        }
        if (controller != null) {
            controller.setDead();
        }
        isAlive = false;
    }
}
