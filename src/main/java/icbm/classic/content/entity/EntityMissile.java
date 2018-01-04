package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.data.EnumProjectileTypes;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.world.map.radar.RadarRegistry;
import com.builtbroken.mc.prefab.entity.EntityProjectile;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import java.util.HashSet;
import java.util.Random;

/** @Author - Calclavia */
public class EntityMissile extends EntityProjectile implements IEntityAdditionalSpawnData
{
    public static final float SPEED = 0.012F;

    public Explosives explosiveID = Explosives.CONDENSED;
    public int maxHeight = 200;
    public Pos targetVector = null;
    public Pos launcherPos = null;
    public boolean isExpoding = false;

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
    // For cluster missile
    public int missileCount = 0;

    public double daoDanGaoDu = 2;

    private boolean setExplode;
    private boolean setNormalExplode;

    // Missile Type
    public MissileType missileType = MissileType.MISSILE;

    public Pos xiaoDanMotion = new Pos();

    private double lockHeight = 3;

    // Used for the rocket launcher preventing the players from killing themselves.
    private final HashSet<Entity> ignoreEntity = new HashSet<Entity>();

    public NBTTagCompound nbtData = new NBTTagCompound();

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
        super(entity.world, entity, 2);
        this.setSize(.5F, .5F);
        this.launcherPos = new Pos(entity);
        this.inAirKillTime = 144000 /* 2 hours */;
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
        //this.world.playSound(posX, posY, posZ, ICBMClassic.PREFIX + "missilelaunch", 4F, (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

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

    /** Recalculates required parabolic path for the missile Registry */
    public void recalculatePath()
    {
        if (this.targetVector != null)
        {
            // Calculate the distance difference of the missile
            this.deltaPathX = this.targetVector.x() - this.sourceOfProjectile.x();
            this.deltaPathY = this.targetVector.y() - this.sourceOfProjectile.y();
            this.deltaPathZ = this.targetVector.z() - this.sourceOfProjectile.z();

            // TODO: Calculate parabola and relative out the targetHeight.
            // Calculate the power required to reach the target co-ordinates
            // Ground Displacement
            this.flatDistance = this.sourceOfProjectile.toVector2().distance(this.targetVector.toVector2());
            // Parabolic Height
            this.maxHeight = 160 + (int) (this.flatDistance * 3);
            // Flight time
            this.missileFlightTime = (float) Math.max(100, 2 * this.flatDistance) - this.ticksInAir;
            // Acceleration
            this.acceleration = (float) this.maxHeight * 2 / (this.missileFlightTime * this.missileFlightTime);
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
        if (!this.world.isRemote)
        {
            if (this.ticksInAir >= 0)
            {
                if (this.missileType == MissileType.MISSILE)
                {
                    // Start the launch
                    if (this.lockHeight > 0)
                    {
                        this.motionY = SPEED * this.ticksInAir * (this.ticksInAir / 2);
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
        if (!world.isRemote)
        {
            super.onImpactEntity(entityHit, velocity);
            explode();
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
    public boolean interactFirst(EntityPlayer entityPlayer)
    {
        //Allow missile to override interaction
        if (this.explosiveID != null && ((Explosion) this.explosiveID.handler).onInteract(this, entityPlayer))
        {
            return true;
        }

        //Handle player riding missile
        if (!this.world.isRemote && (this.riddenByEntity == null || this.riddenByEntity == entityPlayer))
        {
            entityPlayer.mountEntity(this);
            return true;
        }

        return false;
    }

    @Override
    public double getMountedYOffset()
    {
        if (this.missileFlightTime <= 0 && this.missileType == MissileType.MISSILE)
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
        if (this.world.isRemote)
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
            this.world.spawnParticle("flame", position.x(), position.y(), position.z(), 0, 0, 0);
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.world, position, 4, 2);
            position = position.multiply(1 - 0.001 * Math.random());
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.world, position, 4, 2);
            position = position.multiply(1 - 0.001 * Math.random());
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.world, position, 4, 2);
            position = position.multiply(1 - 0.001 * Math.random());
            ICBMClassic.proxy.spawnParticle("missile_smoke", this.world, position, 4, 2);
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
        Pos guJiDiDian = toPos();
        double tempMotionY = this.motionY;

        if (this.ticksInAir > 20)
        {
            for (int i = 0; i < t; i++)
            {
                if (this.missileType == MissileType.CruiseMissile || this.missileType == MissileType.LAUNCHER)
                {
                    guJiDiDian = guJiDiDian.add(this.xiaoDanMotion);
                }
                else
                {
                    guJiDiDian = guJiDiDian.add(motionX, tempMotionY - this.acceleration, motionZ);
                }
            }
        }

        return guJiDiDian;
    }

    @Override
    public void setNormalExplode()
    {
        setNormalExplode = true;
        dataWatcher.updateObject(17, 1);
    }

    @Override
    public void setExplode()
    {
        setExplode = true;
        dataWatcher.updateObject(17, 2);
    }

    @Override
    public void setDead()
    {
        if (!world.isRemote)
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
        try
        {
            // Make sure the missile is not already exploding
            if (!this.isExpoding)
            {
                if (this.explosiveID == null)
                {
                    if (!this.world.isRemote)
                    {
                        this.world.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);
                    }
                }
                else
                {
                    ((Explosion) this.explosiveID.handler).createExplosion(this.world, this.posX, this.posY, this.posZ, this);
                }

                this.isExpoding = true;

                ICBMClassic.INSTANCE.logger().info(this.getEntityName() + " (" + this.getEntityId() + ") exploded in " + (int) this.posX + ", " + (int) this.posY + ", " + (int) this.posZ);
            }

            setDead();

        }
        catch (Exception e)
        {
            ICBMClassic.INSTANCE.logger().error("Missile failed to explode properly. Report this to the developers.", e);
        }
    }

    @Override
    public void normalExplode()
    {
        if (!this.isExpoding)
        {
            isExpoding = true;

            if (!this.world.isRemote)
            {
                world.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);
            }

            setDead();
        }
    }

    @Override
    public void dropMissileAsItem()
    {
        if (!this.isExpoding && !this.world.isRemote)
        {
            EntityItem entityItem = new EntityItem(this.world, this.posX, this.posY, this.posZ, new ItemStack(ICBMClassic.itemMissile, 1, this.explosiveID.ordinal()));

            float var13 = 0.05F;
            Random random = new Random();
            entityItem.motionX = ((float) random.nextGaussian() * var13);
            entityItem.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
            entityItem.motionZ = ((float) random.nextGaussian() * var13);
            this.world.spawnEntityInWorld(entityItem);
        }

        this.setDead();
    }

    /** (abstract) Protected helper method to read subclass entity additionalMissileData from NBT. */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        this.targetVector = new Pos(nbt.getCompoundTag("target"));
        this.launcherPos = new Pos(nbt.getCompoundTag("faSheQi"));
        this.acceleration = nbt.getFloat("acceleration");
        this.targetHeight = nbt.getInteger("targetHeight");
        this.explosiveID = Explosives.get(nbt.getInteger("explosiveID"));
        this.ticksInAir = nbt.getInteger("ticksInAir");
        this.lockHeight = nbt.getDouble("lockHeight");
        this.missileType = MissileType.values()[nbt.getInteger("missileType")];
        this.nbtData = nbt.getCompoundTag("additionalMissileData");
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