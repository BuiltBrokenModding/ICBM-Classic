package icbm.classic.content.entity.missile;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.caps.IMissile;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.config.ConfigMissile;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.entity.EntityProjectile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Entity version of the missile
 *
 * @Author - Calclavia, Darkguardsman
 */
public class EntityMissile extends EntityProjectile implements IEntityAdditionalSpawnData
{

    public static final float MISSILE_SPEED = 2;
    public int explosiveID = -1;
    public int maxHeight = 200;
    public Pos targetPos = null;
    public Pos launcherPos = null;

    /**
     * State check to prevent the missile from blowing up twice
     */
    public boolean isExploding = false;

    public boolean destroyNextTick = false;
    public boolean destroyWithFullExplosion = false;
    public boolean explodeNextTick = false;

    public int targetHeight = -1;
    // Difference
    public double deltaPathX;
    public double deltaPathY;
    public double deltaPathZ;
    // Flat Distance
    public double flatDistance;
    // The flight time in ticks
    public float missileFlightTime;
    // Acceleration
    public float acceleration;
    // Protection Time
    public int protectionTime = 2;

    // For anti-ballistic missile
    public Entity lockedTarget;
    // Has this missile lock it's target before?
    public boolean didTargetLockBefore = false;
    // Tracking
    public int trackingVar = -1;

    // Missile Type
    public MissileFlightType missileType = MissileFlightType.PAD_LAUNCHER;

    public Pos motionVector = new Pos();

    public double lockHeight = 3;

    public boolean wasSimulated = false;

    // Used for the rocket launcher preventing the players from killing themselves.
    private final HashSet<Entity> ignoreEntity = new HashSet<Entity>();

    public NBTTagCompound blastData = new NBTTagCompound();

    public IEMPReceiver capabilityEMP;
    public final IMissile capabilityMissile = new CapabilityMissile(this);

    private final int maxPreLaunchSmokeTimer = 20;
    public int preLaunchSmokeTimer = getMaxPreLaunchSmokeTimer();
    public int launcherHasAirBelow = -1;
    private LinkedList<Pos> lastSmokePos = new LinkedList<>();

    public EntityMissile(World w)
    {
        super(w);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityMissile(World w, double x, double y, double z, float yaw, float pitch, float speed)
    {
        super(w, x, y, z, yaw, pitch, speed, 1);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityMissile(EntityLivingBase entity)
    {
        super(entity.world, entity, MISSILE_SPEED);
        this.setSize(.5F, .5F);
        this.launcherPos = new Pos(entity);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEMP.EMP)
        {
            if (capabilityEMP == null)
            {
                capabilityEMP = new CapabilityEmpMissile(this);
            }
            return (T) capabilityEMP;
        }
        else if (capability == ICBMClassicAPI.MISSILE_CAPABILITY)
        {
            return (T) capabilityMissile;
        }
        //TODO add explosive capability
        return super.getCapability(capability, facing);

    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP || capability == ICBMClassicAPI.MISSILE_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return this.getEntityBoundingBox().expand(5, 5, 5);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getRenderDistanceWeight();
        return distance < d0 * d0;
    }

    public String getEntityName()
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(this.explosiveID);
        if (data != null)
        {
            return "icbm.missile." + data.getRegistryName();
        }
        return "icbm.missile";
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        additionalMissileData.writeInt(this.explosiveID); //TODO write full explosive data
        additionalMissileData.writeInt(this.missileType.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        this.explosiveID = additionalMissileData.readInt();
        this.missileType = MissileFlightType.values()[additionalMissileData.readInt()];
    }

    @Override
    public void onUpdate()
    {
        if (preLaunchSmokeTimer > 0)
        {
            this.prevRotationPitch = 90;
        }

        super.onUpdate();

        if (targetPos != null && targetHeight >= 0)
        {
            int deltaX = targetPos.xi() - (int) Math.floor(posX);
            int deltaY = targetPos.yi() - (int) Math.floor(posY);
            int deltaZ = targetPos.zi() - (int) Math.floor(posZ);

            if (inRange(1, deltaY) && inRange(1, deltaX) && inRange(1, deltaZ))
            {
                doExplosion();
            }
        }
    }

    private boolean inRange(int range, int value)
    {
        return value <= range && value >= -range;
    }


    /**
     * Used {@link #capabilityMissile} {@link CapabilityMissile#launch(double, double, double, double)}
     *
     * @param target
     */
    protected void launch(Pos target)
    {
        //Start motion
        if (ticksInAir <= 0)
        {
            this.ticksInAir = 2;
        }

        //Update data
        this.sourceOfProjectile = new Pos((IPos3D) this); //TODO get source of launch
        this.targetPos = target;
        this.targetHeight = this.targetPos != null ? this.targetPos.yi() : -1;

        //Trigger events
        //TODO add generic event
        ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerLaunch(capabilityMissile);

        //Trigger code
        this.recalculatePath();
        this.updateMotion();

        //Play audio
        ICBMSounds.MISSILE_LAUNCH.play(world, posX, posY, posZ, 1F, (1.0F + CalculationHelpers.randFloatRange(this.world.rand, 0.2F)) * 0.7F, true);

        //Trigger events
        // TODO add an event system here
        RadarRegistry.add(this);
        if (ConfigDebug.DEBUG_MISSILE_LAUNCHES)
        {
            if (target != null)
            {
                ICBMClassic.logger().info("Launching " + this.getEntityName() + " (" + this.getEntityId() + ") from " + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi() + " to " + targetPos.xi() + ", " + targetPos.yi() + ", " + targetPos.zi());
            }
            else
            {
                ICBMClassic.logger().info("Launching " + this.getEntityName() + " (" + this.getEntityId() + ") from " + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi());
            }
        }
    }

    /**
     * Used {@link #capabilityMissile} {@link CapabilityMissile#launch(double, double, double, double)}
     *
     * @param target
     */
    protected void launch(Pos target, int height)
    {
        if (height > 0)
        {
            this.lockHeight = height;
        }
        this.launch(target);
    }

    public EntityMissile ignore(Entity entity)
    {
        ignoreEntity.add(entity);
        return this;
    }

    /**
     * Calculates the data needed to get the missile moving on a set path
     * <p>
     * Path is based on the type of missile
     * {@link MissileFlightType#PAD_LAUNCHER} -> Moves on an parabolic path
     */
    public void recalculatePath()
    {
        if (this.targetPos != null)
        {
            // Calculate the distance difference of the missile
            this.deltaPathX = this.targetPos.x() - this.sourceOfProjectile.x();
            this.deltaPathY = this.targetPos.y() - this.sourceOfProjectile.y();
            this.deltaPathZ = this.targetPos.z() - this.sourceOfProjectile.z();

            if (missileType == MissileFlightType.PAD_LAUNCHER)
            {
                // TODO: Calculate parabola and relative out the targetHeight.
                // Calculate the power required to reach the target co-ordinates
                // Ground Displacement
                this.flatDistance = this.sourceOfProjectile.toVector2().distance(this.targetPos.toVector2());
                // Parabolic Height
                this.maxHeight = 160 + (int) (this.flatDistance * 3);
                // Flight time
                this.missileFlightTime = (float) Math.max(100, 2 * this.flatDistance) - this.ticksInAir;
                // Acceleration
                if (!this.wasSimulated)     // only set acceleration when doing a normal launch as the missile flight time is set to -1 when it comes out of simulation.
                {
                    this.acceleration = (float) this.maxHeight * 2 / (this.missileFlightTime * this.missileFlightTime);
                }
            }
            else if (missileType.movesDirectly)
            {
                shoot(deltaPathX, deltaPathY, deltaPathZ, MISSILE_SPEED, 0);
            }
        }
    }

    @Override
    public void entityInit()
    {
        //this.dataWatcher.addObject(16, -1);
        //this.dataWatcher.addObject(17, 0);
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    protected void updateMotion()
    {
        if (this.wasSimulated)
        {
            preLaunchSmokeTimer = 0;
        }

        if (!this.world.isRemote)
        {

            if (preLaunchSmokeTimer <= 0 || this.missileType != MissileFlightType.PAD_LAUNCHER)
            {
                //Start motion
                if (ticksInAir <= 0)
                {
                    this.ticksInAir = 2;
                }

                if (this.ticksInAir >= 0)
                {
                    if (this.missileType == MissileFlightType.PAD_LAUNCHER)
                    {

                        if (this.lockHeight > 0)
                        {
                            this.motionY = ConfigMissile.LAUNCH_SPEED * this.ticksInAir * (this.ticksInAir / 2f);
                            this.motionX = 0;
                            this.motionZ = 0;
                            this.lockHeight -= this.motionY;
                            if (this.lockHeight <= 0)
                            {
                                this.motionY = this.acceleration * (this.missileFlightTime / 2);
                                this.motionX = this.deltaPathX / missileFlightTime;
                                this.motionZ = this.deltaPathZ / missileFlightTime;
                            }
                        }
                        else
                        {
                            this.motionY -= this.acceleration;
                            this.rotationPitch = (float) (Math.atan(this.motionY / (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ))) * 180 / Math.PI);
                            // Look at the next point
                            this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180 / Math.PI);
                        }

                        //Simulate missile
                        if (shouldSimulate())
                        {
                            MissileTrackerHandler.simulateMissile(this);
                        }
                    }
                }
                else
                {
                    //this.rotationPitch = (float) (Math.atan(this.motionY / (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ))) * 180 / Math.PI);
                    // Look at the next point
                    //this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180 / Math.PI);
                }
                this.protectionTime--;
            }
            else
            {
                motionY = 0.001f;
                this.lockHeight -= motionY;
                posY = launcherPos.y() + 2.2f;
                this.prevRotationPitch = 90f;
                this.rotationPitch = 90f;
                ICBMClassic.proxy.spawnMissileSmoke(this);
                this.ticksInAir = 0;
            }
        }
        if (preLaunchSmokeTimer > 0)
        {
            preLaunchSmokeTimer--;
        }

        //Handle effects
        ICBMClassic.proxy.spawnMissileSmoke(this);
        ICBMSounds.MISSILE_ENGINE.play(world, posX, posY, posZ, Math.min(1, ticksInAir / 40F) * 1F, (1.0F + CalculationHelpers.randFloatRange(this.world.rand, 0.2F)) * 0.7F, true);


        //Trigger events
        ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(capabilityMissile);

        super.updateMotion();
    }

    protected boolean shouldSimulate()
    {
        //TODO predict position, if traveling into unloaded chunk simulate

        if (launcherPos != null)
        {
            if (getPassengers().stream().anyMatch(entity -> entity instanceof EntityPlayerMP))
            {
                return false;
            }
            else if (wasSimulated)
            {
                return false;
            }
            else if (posY >= ConfigMissile.SIMULATION_START_HEIGHT)
            {
                return true;
            }

            //About to enter an unloaded chunk
            final BlockPos pos = getPredictedPosition(1).toBlockPos();
            if (!world.isBlockLoaded(pos))
            {
                return true;
            }
        }
        return false;
    }

    protected void decreaseMotion()
    {
        if (this.missileType != MissileFlightType.PAD_LAUNCHER && ticksInAir > 1000)
        {
            super.decreaseMotion();
        }
    }

    @Override
    protected void onImpactTile(RayTraceResult hit)
    {
        doExplosion();
    }

    @Override
    protected boolean ignoreImpact(RayTraceResult hit)
    {
        return MinecraftForge.EVENT_BUS.post(new MissileEvent.PreImpact(capabilityMissile, this, hit));
    }

    @Override
    protected void postImpact(RayTraceResult hit)
    {
        MinecraftForge.EVENT_BUS.post(new MissileEvent.PostImpact(capabilityMissile, this, hit));
    }

    @Override
    protected void onImpactEntity(Entity entityHit, float velocity)
    {
        if (!world.isRemote && entityHit.getRidingEntity() != this)
        {
            super.onImpactEntity(entityHit, velocity);
            doExplosion();
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        //Allow missile to override interaction
        if (ICBMClassicAPI.EX_MISSILE_REGISTRY.onInteraction(this, player, hand))
        {
            return true;
        }

        //Handle player riding missile
        if (!this.world.isRemote && (this.getRidingEntity() == null || this.getRidingEntity() == player) && !MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Start(this, player)))
        {
            player.startRiding(this);
            return true;
        }

        return false;
    }

    @Override
    public double getMountedYOffset()
    {
        if (this.missileFlightTime <= 0 && this.missileType == MissileFlightType.PAD_LAUNCHER)
        {
            return height;
        }
        else if (this.missileType == MissileFlightType.CRUISE_LAUNCHER)
        {
            return height / 10;
        }

        return height / 2 + motionY;
    }

    public LinkedList<Pos> getLastSmokePos()
    {
        return lastSmokePos;
    }

    public int getMaxPreLaunchSmokeTimer()
    {
        return maxPreLaunchSmokeTimer;
    }

    /**
     * Checks to see if an entity is touching the missile. If so, blow up!
     */
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        if (ignoreEntity.contains(entity))
        {
            return null;
        }
        return getEntityBoundingBox();
    }

    public Pos getPredictedPosition(int t)
    {
        Pos position = new Pos((IPos3D) this);
        double tempMotionY = this.motionY;

        if (this.ticksInAir > 20)
        {
            for (int i = 0; i < t; i++)
            {
                if (this.missileType.movesDirectly)
                {
                    position = position.add(this.motionVector);
                }
                else
                {
                    position = position.add(motionX, tempMotionY - this.acceleration, motionZ);
                }
            }
        }

        return position;
    }

    @Override
    public void setDead()
    {
        if (!world.isRemote)
        {
            RadarRegistry.remove(this);
        }

        super.setDead();
    }

    protected void logImpact() {
        // TODO make optional via config
        // TODO log to ICBM file separated from main config
        // TODO offer hook for database logging
        final String formatString = "Missile[%s] E_ID(%s) impacted at (%sx,%sy,%sz,%sd)";
        final String formattedMessage = String.format(formatString,
                this.explosiveID,
                this.getEntityId(),
                xi(),
                yi(),
                zi(),
                world().provider.getDimension()
        );
        ICBMClassic.logger().info(formattedMessage);
    }

    public BlastResponse doExplosion()
    {
        //Eject from riding
        dismountRidingEntity();
        //Eject passengers
        removePassengers();

        try
        {
            // Make sure the missile is not already exploding
            if (!this.isExploding)
            {
                //Log that the missile impacted
                logImpact();

                //Make sure to note we are currently exploding
                this.isExploding = true;

                //Kill the misisle entity
                setDead();

                if (!this.world.isRemote)
                {
                    return ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY, this.posZ, explosiveID, 1, blastData);
                }
                return BlastState.TRIGGERED_CLIENT.genericResponse;
            }
            return BlastState.ALREADY_TRIGGERED.genericResponse;
        }
        catch (Exception e)
        {
            return new BlastResponse(BlastState.ERROR, e.getMessage(), e);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity additionalMissileData from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        this.targetPos = new Pos(nbt.getCompoundTag(NBTConstants.TARGET));
        this.launcherPos = new Pos(nbt.getCompoundTag(NBTConstants.LAUNCHER_POS));
        this.acceleration = nbt.getFloat(NBTConstants.ACCELERATION);
        this.targetHeight = nbt.getInteger(NBTConstants.TARGET_HEIGHT);
        this.explosiveID = nbt.getInteger(NBTConstants.EXPLOSIVE_ID);
        this.ticksInAir = nbt.getInteger(NBTConstants.TICKS_IN_AIR);
        this.lockHeight = nbt.getDouble(NBTConstants.LOCK_HEIGHT);
        this.missileType = MissileFlightType.get(nbt.getInteger(NBTConstants.MISSILE_TYPE));
        this.preLaunchSmokeTimer = nbt.getInteger(NBTConstants.PRE_LAUNCH_SMOKE_TIMER);
        this.blastData = nbt.getCompoundTag(NBTConstants.ADDITIONAL_MISSILE_DATA);
    }

    /**
     * (abstract) Protected helper method to write subclass entity additionalMissileData to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        if (this.targetPos != null)
        {
            nbt.setTag(NBTConstants.TARGET, this.targetPos.toNBT());
        }

        if (this.launcherPos != null)
        {
            nbt.setTag(NBTConstants.LAUNCHER_POS, this.launcherPos.toNBT());
        }

        nbt.setFloat(NBTConstants.ACCELERATION, this.acceleration);
        nbt.setInteger(NBTConstants.EXPLOSIVE_ID, this.explosiveID);
        nbt.setInteger(NBTConstants.TARGET_HEIGHT, this.targetHeight);
        nbt.setInteger(NBTConstants.TICKS_IN_AIR, this.ticksInAir);
        nbt.setDouble(NBTConstants.LOCK_HEIGHT, this.lockHeight);
        nbt.setInteger(NBTConstants.MISSILE_TYPE, this.missileType.ordinal());
        nbt.setInteger(NBTConstants.PRE_LAUNCH_SMOKE_TIMER, this.preLaunchSmokeTimer);
        nbt.setTag(NBTConstants.ADDITIONAL_MISSILE_DATA, this.blastData);
    }
}
