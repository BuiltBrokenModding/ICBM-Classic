package icbm.classic.content.missile;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IExplosiveContainer;
import icbm.classic.api.explosion.ILauncherContainer;
import icbm.classic.api.explosion.IMissile;
import icbm.classic.config.ConfigMissile;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.handlers.Explosion;
import icbm.classic.lib.emp.CapabilityEMP;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.entity.EntityProjectile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

/** @Author - Calclavia */
public class EntityMissile extends EntityProjectile implements IEntityAdditionalSpawnData, IExplosiveContainer, IMissile
{
    public static final float MISSILE_SPEED = 2;
    public Explosives explosiveID = Explosives.CONDENSED;
    public int maxHeight = 200;
    public Pos targetPos = null;
    public Pos launcherPos = null;

    /** State check to prevent the missile from blowing up twice */
    public boolean isExploding = false;

    public boolean destroyNextTick = false;
    public boolean destroyWithFullExplosion = false;
    public boolean explodeNextTick = false;

    public int targetHeight = 0;
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

    public NBTTagCompound nbtData = new NBTTagCompound();

    public IEMPReceiver capabilityEMP;

    final int maxPreLaunchSmokeTimer = 50;
    public int preLaunchSmokeTimer = maxPreLaunchSmokeTimer;
    public int launcherHasAirBelow = -1;


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
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP || super.hasCapability(capability, facing);
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
        return this.explosiveID.handler.getMissileName();
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        additionalMissileData.writeInt(this.explosiveID.ordinal());
        additionalMissileData.writeInt(this.missileType.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        this.explosiveID = Explosives.get(additionalMissileData.readInt());
        this.missileType = MissileFlightType.values()[additionalMissileData.readInt()];
    }

    @Override
    public void launch(Pos target)
    {
        //Start motion
        if (ticksInAir <= 0)
        {
            this.ticksInAir = 2;
        }

        //Update data
        this.sourceOfProjectile = new Pos((IPos3D) this); //TODO get source of launch
        this.targetPos = target;
        this.targetHeight = this.targetPos != null ? this.targetPos.yi() : 0;
        if (explosiveID != null && explosiveID.handler instanceof Explosion)
        {
            ((Explosion) explosiveID.handler).launch(this);
        }

        //Trigger code
        this.recalculatePath();
        this.updateMotion();

        //Play audio
        //this.world.playSound(posX, posY, posZ, ICBMClassic.PREFIX + "missilelaunch", 4F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

        //Trigger events
        // TODO add an event system here
        RadarRegistry.add(this);
        if (target != null)
        {
            ICBMClassic.logger().info("Launching " + this.getEntityName() + " (" + this.getEntityId() + ") from " + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi() + " to " + targetPos.xi() + ", " + targetPos.yi() + ", " + targetPos.zi());
        }
        else
        {
            ICBMClassic.logger().info("Launching " + this.getEntityName() + " (" + this.getEntityId() + ") from " + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi());
        }
    }

    public void launch(Pos target, int height)
    {
        this.lockHeight = height;
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
                this.acceleration = (float) this.maxHeight * 2 / (this.missileFlightTime * this.missileFlightTime);

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


                        if (launcherPos != null && targetPos.distance(launcherPos) > 50 && !wasSimulated && this.ticksInAir > 20 * 10) // 10 seconds
                        {
                            ICBMClassic.logger().info("Simulating missile.");
                            MissileTrackerHandler.simulateMissile(this);
                        }
                    }

                    ICBMClassic.logger().info("x/y/z: " + this.posX + "/" + this.posY + "/" + this.posZ);
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
                motionY = 0.015f;
                this.lockHeight -= motionY;
                posY = launcherPos.y() + 2.2f;
                this.prevRotationPitch = 90f;
                this.rotationPitch = 90f;
                spawnMissileSmoke();
                this.ticksInAir = 0;
            }
        }
        if (preLaunchSmokeTimer > 0)
        {
            preLaunchSmokeTimer--;
        }
        this.spawnMissileSmoke();
        if (this.explosiveID != null && this.explosiveID.handler instanceof Explosion)
        {
            ((Explosion) this.explosiveID.handler).update(this);
        }
        super.updateMotion();
    }

    @Override
    protected void decreaseMotion()
    {
        if (this.missileType != MissileFlightType.PAD_LAUNCHER && ticksInAir > 1000)
        {
            super.decreaseMotion();
        }
    }

    @Override
    protected void onImpactTile()
    {
        doExplosion();
    }

    @Override
    protected void onImpactEntity(Entity entityHit, float velocity)
    {
        if (!world.isRemote)
        {
            super.onImpactEntity(entityHit, velocity);
            doExplosion();
        }
    }


    public ILauncherContainer getLauncher()
    {
        if (this.launcherPos != null)
        {
            TileEntity tileEntity = this.launcherPos.getTileEntity(this.world);

            if (tileEntity != null && tileEntity instanceof ILauncherContainer)
            {
                if (!tileEntity.isInvalid())
                {
                    return (ILauncherContainer) tileEntity;
                }
            }
        }

        return null;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        //Allow missile to override interaction
        if (this.explosiveID != null && ((Explosion) this.explosiveID.handler).onInteract(this, player, hand))
        {
            return true;
        }

        //Handle player riding missile
        if (!this.world.isRemote && (this.getRidingEntity() == null || this.getRidingEntity() == player))
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

    LinkedList<Pos> lastSmokePos = new LinkedList<>();

    private void spawnMissileSmoke()
    {
        if (this.world.isRemote)
        {
            if (this.missileType == MissileFlightType.PAD_LAUNCHER)
            {
                if(this.motionY > -1)
                {
                    if (this.world.isRemote && this.motionY > -1) {
                        if (launcherHasAirBelow == -1) {
                            BlockPos bp = new BlockPos(Math.signum(this.posX) * Math.floor(Math.abs(this.posX)), this.posY - 3, Math.signum(this.posZ) * Math.floor(Math.abs(this.posZ)));
                            launcherHasAirBelow = world.isAirBlock(bp) ? 1 : 0;
                        }
                        Pos position = new Pos((IPos3D) this);
                        // The distance of the smoke relative
                        // to the missile.
                        double distance = -1.2f;
                        // The delta Y of the smoke.
                        double y = Math.sin(Math.toRadians(this.rotationPitch)) * distance;
                        // The horizontal distance of the
                        // smoke.
                        double dH = Math.cos(Math.toRadians(this.rotationPitch)) * distance;
                        // The delta X and Z.
                        double x = Math.sin(Math.toRadians(this.rotationYaw)) * dH;
                        double z = Math.cos(Math.toRadians(this.rotationYaw)) * dH;
                        position = position.add(x, y, z);

                        if (preLaunchSmokeTimer > 0 && ticksInAir <= maxPreLaunchSmokeTimer) {
                            if (launcherHasAirBelow == 1) {
                                position = position.sub(0, 2, 0);
                                Pos velocity = new Pos(0, -1, 0).addRandom(world.rand, 0.5);
                                for (int i = 0; i < 10; i++) {
                                    ICBMClassic.proxy.spawnSmoke(this.world, position, velocity.x(), velocity.y(), velocity.z(), 1, 1, 1, 8, 180);
                                    position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                                }
                            }
                        } else {
                            lastSmokePos.add(position);
                            Pos lastPos = null;
                            if (lastSmokePos.size() > 5) {
                                lastPos = lastSmokePos.get(0);
                                lastSmokePos.remove(0);
                            }
                            ICBMClassic.proxy.spawnSmoke(this.world, position, -this.motionX * 0.75, -this.motionY * 0.75, -this.motionZ * 0.75, 1, 0.75f, 0, 5, 10);
                            if (ticksInAir > 5 && lastPos != null) {
                                for (int i = 0; i < 10; i++) {
                                    ICBMClassic.proxy.spawnSmoke(this.world, lastPos, -this.motionX * 0.5, -this.motionY * 0.5, -this.motionZ * 0.5, 1, 1, 1, (int) Math.max(1d, 6d * (1 / (1 + posY / 100))), 240);
                                    position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                Pos position = new Pos((IPos3D) this);
                // The distance of the smoke relative
                // to the missile.
                double distance = - 1.2f;
                // The delta Y of the smoke.
                double y = Math.sin(Math.toRadians(this.rotationPitch)) * distance;
                // The horizontal distance of the
                // smoke.
                double dH = Math.cos(Math.toRadians(this.rotationPitch)) * distance;
                // The delta X and Z.
                double x = Math.sin(Math.toRadians(this.rotationYaw)) * dH;
                double z = Math.cos(Math.toRadians(this.rotationYaw)) * dH;
                position = position.add(x, y, z);

                for (int i = 0; i < 10; i++) {
                    ICBMClassic.proxy.spawnSmoke(this.world, position, -this.motionX * 0.5, -this.motionY * 0.5, -this.motionZ * 0.5, 1, 1, 1, (int) Math.max(1d, 6d * (1 / (1 + posY / 100))), 240);
                    position.multiply(1 - 0.025 * Math.random(), 1 - 0.025 * Math.random(), 1 - 0.025 * Math.random());
                }
            }
        }
    }

    /** Checks to see if an entity is touching the missile. If so, blow up! */
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
    public void triggerExplosion()
    {
        explodeNextTick = true;
    }

    @Override
    public void destroyMissile(boolean fullExplosion)
    {
        destroyNextTick = true;
        destroyWithFullExplosion = fullExplosion;
    }

    @Override
    public boolean isExploding()
    {
        return isExploding;
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

    @Override
    public void doExplosion()
    {
        try
        {
            // Make sure the missile is not already exploding
            if (!this.isExploding)
            {
                //Make sure to note we are currently exploding
                this.isExploding = true;

                //Kill the misisle entity
                setDead();

                if (!this.world.isRemote)
                {
                    //Create TNT explosion if no explosive exists
                    if (this.explosiveID == null)
                    {
                        this.world.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);

                    }
                    //Triger normal explosion
                    else
                    {
                        this.explosiveID.handler.createExplosion(this.world, new BlockPos(this.posX, this.posY, this.posZ), this, 1);
                    }
                }

                //Log that the missile impacted
                ICBMClassic.logger().info(this.getEntityName() + " (" + this.getEntityId() + ") exploded in " + (int) this.posX + ", " + (int) this.posY + ", " + (int) this.posZ);
            }
        }
        catch (Exception e)
        {
            ICBMClassic.logger().error("EntityMissile#normalExplode() - Unexpected error while triggering explosive on missile", e);
        }
    }

    @Override
    public void dropMissileAsItem()
    {
        if (!this.isExploding && !this.world.isRemote)
        {
            EntityItem entityItem = new EntityItem(this.world, this.posX, this.posY, this.posZ, new ItemStack(ICBMClassic.itemMissile, 1, this.explosiveID.ordinal()));

            float var13 = 0.05F;
            Random random = new Random();
            entityItem.motionX = ((float) random.nextGaussian() * var13);
            entityItem.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
            entityItem.motionZ = ((float) random.nextGaussian() * var13);
            this.world.spawnEntity(entityItem);
        }

        this.setDead();
    }

    /** (abstract) Protected helper method to read subclass entity additionalMissileData from NBT. */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        this.targetPos = new Pos(nbt.getCompoundTag("target"));
        this.launcherPos = new Pos(nbt.getCompoundTag("launcherPos"));
        this.acceleration = nbt.getFloat("acceleration");
        this.targetHeight = nbt.getInteger("targetHeight");
        this.explosiveID = Explosives.get(nbt.getInteger("explosiveID"));
        this.ticksInAir = nbt.getInteger("ticksInAir");
        this.lockHeight = nbt.getDouble("lockHeight");
        this.missileType = MissileFlightType.get(nbt.getInteger("missileType"));
        this.nbtData = nbt.getCompoundTag("additionalMissileData");
    }

    /** (abstract) Protected helper method to write subclass entity additionalMissileData to NBT. */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        if (this.targetPos != null)
        {
            nbt.setTag("target", this.targetPos.toNBT());
        }

        if (this.launcherPos != null)
        {
            nbt.setTag("launcherPos", this.launcherPos.toNBT());
        }

        nbt.setFloat("acceleration", this.acceleration);
        nbt.setInteger("explosiveID", this.explosiveID.ordinal());
        nbt.setInteger("targetHeight", this.targetHeight);
        nbt.setInteger("ticksInAir", this.ticksInAir);
        nbt.setDouble("lockHeight", this.lockHeight);
        nbt.setInteger("missileType", this.missileType.ordinal());
        nbt.setTag("additionalMissileData", this.nbtData);
    }

    @Override
    public int getTicksInAir()
    {
        return this.ticksInAir;
    }

    @Override
    public Explosive getExplosiveType()
    {
        return this.explosiveID.handler;
    }

    @Override
    public NBTTagCompound getExplosiveData()
    {
        return this.nbtData;
    }

}
