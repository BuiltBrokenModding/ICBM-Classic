package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.data.EnumProjectileTypes;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.world.map.radar.RadarRegistry;
import com.builtbroken.mc.prefab.entity.EntityProjectile;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import resonant.api.explosion.ILauncherContainer;
import resonant.api.explosion.IMissile;

import java.util.HashSet;
import java.util.Random;


/**
 * Entity for the missile object
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Calclavia and reworked Dark(DarkGuardsman, Robert).
 */
public class EntityMissile extends EntityProjectile implements IEntityAdditionalSpawnData, IMissile
{
    /** Speed limit of missile when not arcing */
    public static final float SPEED = 0.012F;

    /** This offset for {@link #missilePathMaxY} when calculate */
    public static final double MISSILE_PATH_HEIGHT_INIT = 160;
    /** This is scale for {@link #missilePathMaxY} when calculate */
    public static final double MISSILE_PATH_HEIGHT_SCALE = 3;
    /** Amount of ticks required to travel a single meter flat distance when arcing */
    public static final int MISSILE_PATH_TICKS_PER_METER = 2;

    /** Limiter for {@link #missilePathMaxY}*/
    public static final double MISSILE_PATH_HEIGHT_MAX = 2000;
    /** Limiter for {@link #missilePathMaxY}*/
    public static final double MISSILE_PATH_HEIGHT_MIN = 0;

    /** Explosive type contained in the missile */
    public Explosives explosiveID = Explosives.CONDENSED;

    public Pos targetVector = null;
    public Pos launcherPos = null;

    public boolean isExploding = false;

    public int targetHeight = 0;

    /** Distance to target */
    public Pos missilePathDelta = new Pos();
    /** Desired max y value for missile path */
    public double missilePathMaxY = 200;
    /** Distance to target ignoring Y change */
    public double missilePathFlatDistance;
    /** Amount of time it will take to reach target */
    public double missilePathTime;
    /** Amount of pull towards the ground the missile will have while arcing towards target */
    public double missilePathDrag;

    // Protection Time
    public int protectionTime = 2;

    // For anti-ballistic missile
    public Entity lockedTarget;
    // Has this missile lock it's target before?
    public boolean didTargetLockBefore = false;
    // Tracking
    public int trackingVar = -1;
    // For cluster missile
    public int missileCount = 0;

    public double daoDanGaoDu = 2;

    // Missile Type
    public MissileType missileType = MissileType.MISSILE;

    public Pos xiaoDanMotion = new Pos();

    private double lockHeight = 3;

    // Used for the rocket launcher preventing the players from killing themselves.
    private final HashSet<Entity> ignoreEntity = new HashSet<Entity>();

    // Client side
    protected final IUpdatePlayerListBox shengYin; //TODO find out what this was used for

    public NBTTagCompound nbtData = new NBTTagCompound();

    public EntityMissile(World w)
    {
        super(w);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.shengYin = this.worldObj != null ? ICBMClassic.proxy.getDaoDanShengYin(this) : null;
        this.renderDistanceWeight = 3;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityMissile(World w, double x, double y, double z, float yaw, float pitch, float speed)
    {
        super(w, x, y, z, yaw, pitch, speed, 1);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.shengYin = this.worldObj != null ? ICBMClassic.proxy.getDaoDanShengYin(this) : null;
        this.renderDistanceWeight = 3;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityMissile(EntityLivingBase entity)
    {
        super(entity.worldObj, entity, 2);
        this.setSize(.5F, .5F);
        this.launcherPos = new Pos(entity);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.shengYin = this.worldObj != null ? ICBMClassic.proxy.getDaoDanShengYin(this) : null;
        this.renderDistanceWeight = 3;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
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
        this.missileType = MissileType.values()[additionalMissileData.readInt()];
    }

    @Override
    public void launch(Pos target)
    {
        //Update data
        this.sourceOfProjectile = toPos();
        this.targetVector = target;
        this.targetHeight = this.targetVector != null ? this.targetVector.yi() : 0;
        if (explosiveID != null && explosiveID.handler instanceof Explosion)
        {
            ((Explosion) explosiveID.handler).launch(this);
        }
        this.ticksInAir = 2;

        //Trigger code
        this.recalculatePath();
        this.updateMotion();

        //Play audio
        this.worldObj.playSoundAtEntity(this, ICBMClassic.PREFIX + "missilelaunch", 4F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);

        //Trigger events
        // TODO add an event system here
        RadarRegistry.add(this);
        if (target != null)
        {
            ICBMClassic.INSTANCE.logger().info("Launching " + this.getEntityName() + " (" + this.getEntityId() + ") from " + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi() + " to " + targetVector.xi() + ", " + targetVector.yi() + ", " + targetVector.zi());
        }
        else
        {
            ICBMClassic.INSTANCE.logger().info("Launching " + this.getEntityName() + " (" + this.getEntityId() + ") from " + sourceOfProjectile.xi() + ", " + sourceOfProjectile.yi() + ", " + sourceOfProjectile.zi());
        }
    }

    @Override
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
     * Recalculates required parabolic path data.
     * <p>
     * Called from {@link #readFromNBT(NBTTagCompound)} as well other plus.
     * Make sure not to use {@link World } or other data not accessible during load
     */
    public void recalculatePath()
    {
        if (this.targetVector != null)
        {
            // Calculate the distance difference of the missile
            this.missilePathDelta = targetVector.sub(sourceOfProjectile);

            //Calculate the power required to reach the target co-ordinates

            // Ground Displacement
            this.missilePathFlatDistance = this.sourceOfProjectile.toVector2().distance(this.targetVector.toVector2());

            // Parabolic Height
            this.missilePathMaxY = MISSILE_PATH_HEIGHT_INIT + this.missilePathFlatDistance * MISSILE_PATH_HEIGHT_SCALE;
            this.missilePathMaxY = Math.max(MISSILE_PATH_HEIGHT_MIN, missilePathMaxY); //Limit by min
            this.missilePathMaxY = Math.min(MISSILE_PATH_HEIGHT_MAX, missilePathMaxY); //Limit by max

            // Flight time
            this.missilePathTime = MISSILE_PATH_TICKS_PER_METER * this.missilePathFlatDistance - this.ticksInAir;

            //Calculate drag
            double HD = missilePathMaxY / missilePathFlatDistance;
            double HT = missilePathMaxY / missilePathTime;
            double TD = missilePathTime / missilePathFlatDistance;
            this.missilePathDrag = ((missilePathMaxY - HD) * HD) / (missilePathTime / TD) / (HT * missilePathFlatDistance);
        }
    }

    @Override
    public void entityInit()
    {
        this.dataWatcher.addObject(16, -1);
        this.dataWatcher.addObject(17, 0);
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    protected void updateMotion()
    {
        if (!this.worldObj.isRemote)
        {
            if (this.ticksInAir >= 0)
            {
                if (this.missileType == MissileType.MISSILE)
                {
                    // Start the launch, hold to on lock height to prevent hitting silo walls
                    if (this.lockHeight > 0)
                    {
                        //Move upward
                        this.motionY = SPEED * this.ticksInAir * (this.ticksInAir / 2);

                        //Cancel out motion X and Z to prevent hitting silo walls
                        this.motionX = 0;
                        this.motionZ = 0;

                        //decrease lock height counter by motion
                        this.lockHeight -= this.motionY;

                        //If we hit zero start curving towards target
                        if (this.lockHeight <= 0)
                        {
                            //Set upwards motion (acceleration * half of flight time)
                            this.motionY = this.missilePathDrag * (this.missilePathTime / 2); //Velocity needed to get to top of arc

                            //Aim missile vector towards target
                            this.motionX = this.missilePathDelta.x() / missilePathTime; //Velocity needed to move towards target
                            this.motionZ = this.missilePathDelta.z() / missilePathTime; //Velocity needed to move towards target
                        }
                    }
                    else
                    {
                        //Decrease motion
                        this.motionY -= this.missilePathDrag;

                        //Update rotation to aim at the next point
                        this.rotationPitch = (float) (Math.atan(this.motionY / (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ))) * 180 / Math.PI);
                        this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180 / Math.PI);

                    }
                }
            }
            else
            {
                //this.rotationPitch = (float) (Math.atan(this.motionY / (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ))) * 180 / Math.PI);
                // Look at the next point
                //this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180 / Math.PI);
            }

            this.spawnMissileSmoke();
            this.protectionTime--;
        }
        if (this.explosiveID != null && this.explosiveID.handler instanceof Explosion)
        {
            ((Explosion) this.explosiveID.handler).update(this);
        }
        super.updateMotion();
    }

    @Override
    protected void decreaseMotion()
    {
        if (this.missileType != MissileType.MISSILE && ticksInAir > 1000)
        {
            super.decreaseMotion();
        }
    }

    @Override
    protected void onImpactTile()
    {
        explode();
    }

    @Override
    protected void onImpactEntity(Entity entityHit, float velocity)
    {
        if (!worldObj.isRemote)
        {
            super.onImpactEntity(entityHit, velocity);
            explode();
        }
    }

    @Override
    public ILauncherContainer getLauncher()
    {
        if (this.launcherPos != null)
        {
            TileEntity tileEntity = this.launcherPos.getTileEntity(this.worldObj);

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
    public boolean interactFirst(EntityPlayer entityPlayer)
    {
        //Allow missile to override interaction
        if (this.explosiveID != null && ((Explosion) this.explosiveID.handler).onInteract(this, entityPlayer))
        {
            return true;
        }

        //Handle player riding missile
        if (!this.worldObj.isRemote && (this.riddenByEntity == null || this.riddenByEntity == entityPlayer))
        {
            entityPlayer.mountEntity(this);
            return true;
        }

        return false;
    }

    @Override
    public double getMountedYOffset()
    {
        if (this.missilePathTime <= 0 && this.missileType == MissileType.MISSILE)
        {
            return height;
        }
        else if (this.missileType == MissileType.CruiseMissile)
        {
            return height / 10;
        }

        return height / 2 + motionY;
    }

    private void spawnMissileSmoke()
    {
        if (this.worldObj.isRemote)
        {
            Pos position = new Pos((IPos3D) this);
            // The distance of the smoke relative
            // to the missile.
            double distance = -this.daoDanGaoDu - 0.2f;
            // The delta Y of the smoke.
            double y = Math.sin(Math.toRadians(this.rotationPitch)) * distance;
            // The horizontal distance of the
            // smoke.
            double dH = Math.cos(Math.toRadians(this.rotationPitch)) * distance;
            // The delta X and Z.
            double x = Math.sin(Math.toRadians(this.rotationYaw)) * dH;
            double z = Math.cos(Math.toRadians(this.rotationYaw)) * dH;

            position = position.add(x, y, z);
            this.worldObj.spawnParticle("flame", position.x(), position.y(), position.z(), 0, 0, 0);
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.worldObj, position, 4, 2);
            position = position.multiply(1 - 0.001 * Math.random());
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.worldObj, position, 4, 2);
            position = position.multiply(1 - 0.001 * Math.random());
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.worldObj, position, 4, 2);
            position = position.multiply(1 - 0.001 * Math.random());
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.worldObj, position, 4, 2);
        }
    }

    /** Checks to see if and entity is touching the missile. If so, blow up! */
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        if (ignoreEntity.contains(entity))
        {
            return null;
        }

        // Make sure the entity is not an item
        if (!(entity instanceof EntityItem) && entity != this.riddenByEntity && this.protectionTime <= 0)
        {
            if (entity instanceof EntityMissile)
            {
                ((EntityMissile) entity).setNormalExplode();
            }

            this.setExplode();
        }

        return null;
    }

    public Pos getPredictedPosition(int t)
    {
        Pos pos = toPos();
        double tempMotionY = this.motionY;

        if (this.ticksInAir > 20)
        {
            for (int i = 0; i < t; i++)
            {
                if (this.missileType == MissileType.CruiseMissile || this.missileType == MissileType.LAUNCHER)
                {
                    pos = pos.add(this.xiaoDanMotion);
                }
                else
                {
                    pos = pos.add(motionX, tempMotionY - this.missilePathDrag, motionZ);
                }
            }
        }

        return pos;
    }

    @Override
    public void setNormalExplode()
    {
        dataWatcher.updateObject(17, 1);
    }

    @Override
    public void setExplode()
    {
        dataWatcher.updateObject(17, 2);
    }

    @Override
    public void setDead()
    {
        if (!worldObj.isRemote)
        {
            RadarRegistry.remove(this);
        }

        super.setDead();

        if (this.shengYin != null)
        {
            this.shengYin.update();
        }
    }

    @Override
    public void explode()
    {
        setDead();
        try
        {
            // Make sure the missile is not already exploding
            if (!this.isExploding)
            {
                if (this.explosiveID == null)
                {
                    if (!this.worldObj.isRemote)
                    {
                        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);
                    }
                }
                else
                {
                    this.explosiveID.handler.createExplosion(this.worldObj, this.posX, this.posY, this.posZ, this);
                }

                this.isExploding = true;

                ICBMClassic.INSTANCE.logger().info(this.getEntityName() + " (" + this.getEntityId() + ") exploded in " + (int) this.posX + ", " + (int) this.posY + ", " + (int) this.posZ);
            }
        }
        catch (Exception e)
        {
            ICBMClassic.INSTANCE.logger().error("Missile failed to explode properly. Report this to the developers.", e);
        }
    }

    @Override
    public void normalExplode()
    {
        if (!this.isExploding)
        {
            isExploding = true;

            if (!this.worldObj.isRemote)
            {
                worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);
            }

            setDead();
        }
    }

    @Override
    public void dropMissileAsItem()
    {
        if (!this.isExploding && !this.worldObj.isRemote)
        {
            EntityItem entityItem = new EntityItem(this.worldObj, this.posX, this.posY, this.posZ, new ItemStack(ICBMClassic.itemMissile, 1, this.explosiveID.ordinal()));

            float var13 = 0.05F;
            Random random = new Random();
            entityItem.motionX = ((float) random.nextGaussian() * var13);
            entityItem.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
            entityItem.motionZ = ((float) random.nextGaussian() * var13);
            this.worldObj.spawnEntityInWorld(entityItem);
        }

        this.setDead();
    }

    /** (abstract) Protected helper method to read subclass entity additionalMissileData from NBT. */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        this.targetVector = new Pos(nbt.getCompoundTag("target"));
        this.launcherPos = new Pos(nbt.getCompoundTag("launcherPos"));
        this.targetHeight = nbt.getInteger("targetHeight");
        this.explosiveID = Explosives.get(nbt.getInteger("explosiveID"));
        this.ticksInAir = nbt.getInteger("ticksInAir");
        this.lockHeight = nbt.getDouble("lockHeight");
        this.missileType = MissileType.values()[nbt.getInteger("missileType")];
        this.nbtData = nbt.getCompoundTag("additionalMissileData");

        recalculatePath();
    }

    /** (abstract) Protected helper method to write subclass entity additionalMissileData to NBT. */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        if (this.targetVector != null)
        {
            nbt.setTag("target", this.targetVector.toNBT());
        }
        if (this.launcherPos != null)
        {
            nbt.setTag("launcherPos", this.launcherPos.toNBT());
        }
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
    public NBTTagCompound getTagCompound()
    {
        return this.nbtData;
    }

    @Override
    public EnumProjectileTypes getProjectileType()
    {
        return EnumProjectileTypes.ROCKET;
    }

    public enum MissileType
    {
        MISSILE,
        CruiseMissile,
        LAUNCHER
    }

}