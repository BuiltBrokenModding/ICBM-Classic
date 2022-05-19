package icbm.classic.content.entity.missile.explosive;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigDebug;
import icbm.classic.config.ConfigMissile;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.MissileFlightType;
import icbm.classic.content.entity.missile.logic.BallisticFlightLogic;
import icbm.classic.content.entity.missile.logic.TargetRangeDet;
import icbm.classic.content.entity.missile.tracker.MissileTrackerHandler;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Entity version of the missile
 *
 * @Author - Calclavia, Darkguardsman
 */
public class EntityExplosiveMissile extends EntityMissile<EntityExplosiveMissile> implements IEntityAdditionalSpawnData
{
    public final BallisticFlightLogic ballisticFlightLogic = new BallisticFlightLogic(this);
    public final TargetRangeDet targetRangeDet = new TargetRangeDet(this);

    //Explosive cap vars
    public int explosiveID = -1;
    public NBTTagCompound blastData = new NBTTagCompound();
    public boolean isExploding = false;


    // Generic shared missile data
    public double deltaPathX;
    public double deltaPathY;
    public double deltaPathZ;
    public Pos motionVector = new Pos();
    private final HashSet<Entity> collisionIgnoreList = new HashSet<Entity>();


    // Missile Type
    public MissileFlightType missileType = MissileFlightType.PAD_LAUNCHER;



    public final IEMPReceiver empCapability = new CapabilityEmpMissile(this);
    public final IMissile missileCapability = new CapabilityMissile(this);




    public EntityExplosiveMissile(World w)
    {
        super(w);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEMP.EMP)
        {
            return (T) empCapability;
        }
        else if (capability == ICBMClassicAPI.MISSILE_CAPABILITY)
        {
            return (T) missileCapability;
        }
        //TODO add explosive capability
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP || capability == ICBMClassicAPI.MISSILE_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public String getName()
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(this.explosiveID);
        if (data != null)
        {
            return I18n.translateToLocal("missile." + data.getRegistryName().toString() + ".name");
        }
        return I18n.translateToLocal("missile.icbmclassic:generic.name");
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
        if (ballisticFlightLogic.preLaunchSmokeTimer > 0)
        {
            this.prevRotationPitch = 90;
        }
        targetRangeDet.update();
        super.onUpdate();
    }


    /**
     * Used {@link #missileCapability} {@link CapabilityMissile#launch(double, double, double, double)}
     *
     * @param target
     */
    @Deprecated //TODO replace with a set target method and a launch with no args
    public void launch(Pos target, int height)
    {
        if (height > 0)
        {
            this.ballisticFlightLogic.lockHeight = height;
        }

        //Start motion
        if (ticksInAir <= 0)
        {
            this.ticksInAir = 2;
        }

        //Update data
        this.sourceOfProjectile = new Pos((IPos3D) this); //TODO get source of launch
        this.ballisticFlightLogic.targetPos = target;
        this.ballisticFlightLogic.targetHeight = this.ballisticFlightLogic.targetPos != null ? this.ballisticFlightLogic.targetPos.yi() : -1;

        //Trigger events
        //TODO add generic event
        ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerLaunch(missileCapability);

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
                ICBMClassic.logger().info("Launching " + this.getName() + " (" + this.getEntityId() + ") from "
                        + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi()
                        + " to " + this.ballisticFlightLogic.targetPos.xi() + ", " + this.ballisticFlightLogic.targetPos.yi() + ", " + this.ballisticFlightLogic.targetPos.zi());
            }
            else
            {
                ICBMClassic.logger().info("Launching " + this.getName() + " (" + this.getEntityId() + ") from " + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi());
            }
        }
    }

    public EntityExplosiveMissile ignore(Entity entity)
    {
        collisionIgnoreList.add(entity);
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
        if (this.ballisticFlightLogic.targetPos != null)
        {
            // Calculate the distance difference of the missile
            this.deltaPathX = this.ballisticFlightLogic.targetPos.x() - this.sourceOfProjectile.x();
            this.deltaPathY = this.ballisticFlightLogic.targetPos.y() - this.sourceOfProjectile.y();
            this.deltaPathZ = this.ballisticFlightLogic.targetPos.z() - this.sourceOfProjectile.z();

            if (missileType == MissileFlightType.PAD_LAUNCHER)
            {

            }
            else if (missileType.movesDirectly)
            {
                shoot(deltaPathX, deltaPathY, deltaPathZ, DIRECT_FLIGHT_SPEED, 0);
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
    protected void updateMotion()
    {
        if (this.ballisticFlightLogic.wasSimulated)
        {
            ballisticFlightLogic.preLaunchSmokeTimer = 0;
        }

        if (!this.world.isRemote)
        {

            if (ballisticFlightLogic.preLaunchSmokeTimer <= 0 || this.missileType != MissileFlightType.PAD_LAUNCHER)
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

                        if (this.ballisticFlightLogic.lockHeight > 0)
                        {
                            this.motionY = ConfigMissile.LAUNCH_SPEED * this.ticksInAir * (this.ticksInAir / 2f);
                            this.motionX = 0;
                            this.motionZ = 0;
                            this.ballisticFlightLogic.lockHeight -= this.motionY;
                            if (this.ballisticFlightLogic.lockHeight <= 0)
                            {
                                this.motionY = this.ballisticFlightLogic.acceleration * (this.ballisticFlightLogic.missileFlightTime / 2);
                                this.motionX = this.deltaPathX / ballisticFlightLogic.missileFlightTime;
                                this.motionZ = this.deltaPathZ / ballisticFlightLogic.missileFlightTime;
                            }
                        }
                        else
                        {
                            this.motionY -= this.ballisticFlightLogic.acceleration;
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
            }
            else
            {
                motionY = 0.001f;
                this.ballisticFlightLogic.lockHeight -= motionY;
                posY = ballisticFlightLogic.launcherPos.y() + 2.2f;
                this.prevRotationPitch = 90f;
                this.rotationPitch = 90f;
                ICBMClassic.proxy.spawnMissileSmoke(this);
                this.ticksInAir = 0;
            }
        }
        if (ballisticFlightLogic.preLaunchSmokeTimer > 0)
        {
            ballisticFlightLogic.preLaunchSmokeTimer--;
        }

        //Handle effects
        ICBMClassic.proxy.spawnMissileSmoke(this);
        ICBMSounds.MISSILE_ENGINE.play(world, posX, posY, posZ, Math.min(1, ticksInAir / 40F) * 1F, (1.0F + CalculationHelpers.randFloatRange(this.world.rand, 0.2F)) * 0.7F, true);


        //Trigger events
        ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(missileCapability);

        super.updateMotion();
    }

    protected boolean shouldSimulate()
    {
        //TODO predict position, if traveling into unloaded chunk simulate

        if (ballisticFlightLogic.launcherPos != null)
        {
            if (getPassengers().stream().anyMatch(entity -> entity instanceof EntityPlayerMP))
            {
                return false;
            }
            else if (ballisticFlightLogic.wasSimulated)
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
        return MinecraftForge.EVENT_BUS.post(new MissileEvent.PreImpact(missileCapability, this, hit));
    }

    @Override
    protected void postImpact(RayTraceResult hit)
    {
        MinecraftForge.EVENT_BUS.post(new MissileEvent.PostImpact(missileCapability, this, hit));
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
        if (this.ballisticFlightLogic.missileFlightTime <= 0 && this.missileType == MissileFlightType.PAD_LAUNCHER)
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
        return ballisticFlightLogic.lastSmokePos;
    }



    /**
     * Checks to see if an entity is touching the missile. If so, blow up!
     */
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        if (collisionIgnoreList.contains(entity))
        {
            return null;
        }
        return getEntityBoundingBox();
    }

    public Pos getPredictedPosition(int t) //TODO rewrite, no reason this should loop and it fails to account for curve path
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
                    position = position.add(motionX, tempMotionY - this.ballisticFlightLogic.acceleration, motionZ);
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
        this.ballisticFlightLogic.targetPos = new Pos(nbt.getCompoundTag(NBTConstants.TARGET));
        this.ballisticFlightLogic.launcherPos = new Pos(nbt.getCompoundTag(NBTConstants.LAUNCHER_POS));
        this.ballisticFlightLogic.acceleration = nbt.getFloat(NBTConstants.ACCELERATION);
        this.ballisticFlightLogic.targetHeight = nbt.getInteger(NBTConstants.TARGET_HEIGHT);
        this.explosiveID = nbt.getInteger(NBTConstants.EXPLOSIVE_ID);
        this.ticksInAir = nbt.getInteger(NBTConstants.TICKS_IN_AIR);
        this.ballisticFlightLogic.lockHeight = nbt.getDouble(NBTConstants.LOCK_HEIGHT);
        this.missileType = MissileFlightType.get(nbt.getInteger(NBTConstants.MISSILE_TYPE));
        this.ballisticFlightLogic.preLaunchSmokeTimer = nbt.getInteger(NBTConstants.PRE_LAUNCH_SMOKE_TIMER);
        this.blastData = nbt.getCompoundTag(NBTConstants.ADDITIONAL_MISSILE_DATA);
    }

    /**
     * (abstract) Protected helper method to write subclass entity additionalMissileData to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        if (this.ballisticFlightLogic.targetPos != null)
        {
            nbt.setTag(NBTConstants.TARGET, this.ballisticFlightLogic.targetPos.toNBT());
        }

        if (this.ballisticFlightLogic.launcherPos != null)
        {
            nbt.setTag(NBTConstants.LAUNCHER_POS, this.ballisticFlightLogic.launcherPos.toNBT());
        }

        nbt.setFloat(NBTConstants.ACCELERATION, this.ballisticFlightLogic.acceleration);
        nbt.setInteger(NBTConstants.EXPLOSIVE_ID, this.explosiveID);
        nbt.setInteger(NBTConstants.TARGET_HEIGHT, this.ballisticFlightLogic.targetHeight);
        nbt.setInteger(NBTConstants.TICKS_IN_AIR, this.ticksInAir);
        nbt.setDouble(NBTConstants.LOCK_HEIGHT, this.ballisticFlightLogic.lockHeight);
        nbt.setInteger(NBTConstants.MISSILE_TYPE, this.missileType.ordinal());
        nbt.setInteger(NBTConstants.PRE_LAUNCH_SMOKE_TIMER, this.ballisticFlightLogic.preLaunchSmokeTimer);
        nbt.setTag(NBTConstants.ADDITIONAL_MISSILE_DATA, this.blastData);
    }
}
