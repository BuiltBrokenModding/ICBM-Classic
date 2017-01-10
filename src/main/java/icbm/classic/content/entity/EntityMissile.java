package icbm.classic.content.entity;

import com.builtbroken.mc.api.explosive.IExplosiveContainer;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.lib.world.radar.RadarRegistry;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.classic.DamageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import icbm.classic.content.machines.TileCruiseLauncher;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidBlock;
import resonant.api.explosion.ILauncherContainer;
import resonant.api.explosion.IMissile;

import java.util.HashSet;
import java.util.Random;

/** @Author - Calclavia */
public class EntityMissile extends Entity implements IExplosiveContainer, IEntityAdditionalSpawnData, IMissile
{
    public static final float SPEED = 0.012F;

    public Explosives explosiveID;
    public int maxHeight = 200;
    public Pos targetVector = null;
    public Pos startPos = null;
    public Pos launcherPos = null;
    public boolean isExpoding = false;

    public int targetHeight = 0;
    public int feiXingTick = -1;
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
    // Hp
    public float damage = 0;
    public float max_damage = 10;
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

    private double qiFeiGaoDu = 3;

    // Used for the rocket launcher preventing the players from killing themselves.
    private final HashSet<Entity> ignoreEntity = new HashSet<Entity>();

    // Client side
    protected final IUpdatePlayerListBox shengYin;

    public NBTTagCompound nbtData = new NBTTagCompound();

    public EntityMissile(World par1World)
    {
        super(par1World);
        this.setSize(1F, 1F);
        this.renderDistanceWeight = 3;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
        this.shengYin = this.worldObj != null ? ICBMClassic.proxy.getDaoDanShengYin(this) : null;
    }

    /**
     * Spawns a traditional missile and cruise missiles
     *
     * @param explosiveId - Explosive ID
     * @param startPos    - Starting Position
     * @param launcherPos - Missile Launcher Position
     */
    public EntityMissile(World world, Pos startPos, Pos launcherPos, Explosives explosiveId)
    {
        this(world);
        this.explosiveID = explosiveId;
        this.startPos = startPos;
        this.launcherPos = launcherPos;

        this.setPosition(this.startPos.x(), this.startPos.y(), this.startPos.z());
        this.setRotation(0, 90);
    }

    /**
     * For rocket launchers
     *
     * @param explosiveId - Explosive ID
     * @param startPos    - Starting Position
     * @param yaw         - The yaw of the missle
     * @param pitch       - the pitch of the missle
     */
    public EntityMissile(World world, Pos startPos, Explosives explosiveId, float yaw, float pitch)
    {
        this(world);
        this.explosiveID = explosiveId;
        this.launcherPos = this.startPos = startPos;
        this.missileType = MissileType.LAUNCHER;
        this.protectionTime = 0;

        this.setPosition(this.startPos.x(), this.startPos.y(), this.startPos.z());
        this.setRotation(yaw, pitch);
    }

    public String getEntityName()
    {
        return this.explosiveID.handler.getMissileName();
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        try
        {
            data.writeInt(this.explosiveID.ordinal());
            data.writeInt(this.missileType.ordinal());

            data.writeDouble(this.startPos.x());
            data.writeDouble(this.startPos.y());
            data.writeDouble(this.startPos.z());

            data.writeInt(this.launcherPos.xi());
            data.writeInt(this.launcherPos.yi());
            data.writeInt(this.launcherPos.zi());

            data.writeFloat(rotationYaw);
            data.writeFloat(rotationPitch);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        try
        {
            this.explosiveID = Explosives.get(data.readInt());
            this.missileType = MissileType.values()[data.readInt()];
            this.startPos = new Pos(data.readDouble(), data.readDouble(), data.readDouble());
            this.launcherPos = new Pos(data.readInt(), data.readInt(), data.readInt());

            rotationYaw = data.readFloat();
            rotationPitch = data.readFloat();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void launch(Pos target)
    {
        this.startPos = new Pos(this);
        this.targetVector = target;
        this.targetHeight = this.targetVector.yi();
        ((Explosion) explosiveID.handler).launch(this);
        this.feiXingTick = 0;
        this.recalculatePath();
        this.worldObj.playSoundAtEntity(this, ICBMClassic.PREFIX + "missilelaunch", 4F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
        // TODO add an event system here
        RadarRegistry.add(this);
        ICBMClassic.INSTANCE.logger().info("Launching " + this.getEntityName() + " (" + this.getEntityId() + ") from " + startPos.xi() + ", " + startPos.yi() + ", " + startPos.zi() + " to " + targetVector.xi() + ", " + targetVector.yi() + ", " + targetVector.zi());
    }

    @Override
    public void launch(Pos target, int height)
    {
        this.qiFeiGaoDu = height;
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
            this.deltaPathX = this.targetVector.x() - this.startPos.x();
            this.deltaPathY = this.targetVector.y() - this.startPos.y();
            this.deltaPathZ = this.targetVector.z() - this.startPos.z();

            // TODO: Calculate parabola and relative out the height.
            // Calculate the power required to reach the target co-ordinates
            // Ground Displacement
            this.flatDistance = this.startPos.toVector2().distance(this.targetVector.toVector2());
            // Parabolic Height
            this.maxHeight = 160 + (int) (this.flatDistance * 3);
            // Flight time
            this.missileFlightTime = (float) Math.max(100, 2 * this.flatDistance) - this.feiXingTick;
            // Acceleration
            this.acceleration = (float) this.maxHeight * 2 / (this.missileFlightTime * this.missileFlightTime);
        }
    }

    @Override
    public void entityInit()
    {
        this.dataWatcher.addObject(16, -1);
        this.dataWatcher.addObject(17, 0);
        //this.chunkLoaderInit(ForgeChunkManager.requestTicket(ICBMExplosion.instance, this.worldObj, Type.ENTITY));
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        if (this.shengYin != null)
        {
            this.shengYin.update();
        }

        try
        {
            if (this.worldObj.isRemote)
            {
                this.feiXingTick = this.dataWatcher.getWatchableObjectInt(16);
                int status = this.dataWatcher.getWatchableObjectInt(17);

                switch (status)
                {
                    case 1:
                        setNormalExplode = true;
                        break;
                    case 2:
                        setExplode = true;
                        break;
                }
            }
            else
            {
                this.dataWatcher.updateObject(16, feiXingTick);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (setNormalExplode)
        {
            normalExplode();
            return;
        }

        if (setExplode)
        {
            explode();
            return;
        }

        if (this.feiXingTick >= 0)
        {
            if (!this.worldObj.isRemote)
            {
                if (this.missileType == MissileType.CruiseMissile || this.missileType == MissileType.LAUNCHER)
                {
                    if (this.feiXingTick == 0 && this.xiaoDanMotion != null)
                    {
                        this.xiaoDanMotion = new Pos(this.deltaPathX / (missileFlightTime * 0.3), this.deltaPathY / (missileFlightTime * 0.3), this.deltaPathZ / (missileFlightTime * 0.3));
                        this.motionX = this.xiaoDanMotion.x();
                        this.motionY = this.xiaoDanMotion.y();
                        this.motionZ = this.xiaoDanMotion.z();
                    }

                    this.rotationPitch = (float) (Math.atan(this.motionY / (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ))) * 180 / Math.PI);

                    // Look at the next point
                    this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180 / Math.PI);

                    ((Explosion) this.explosiveID.handler).update(this);

                    Block block = this.worldObj.getBlock((int) this.posX, (int) this.posY, (int) this.posZ);

                    if (this.protectionTime <= 0 && ((block != null && !(block instanceof IFluidBlock)) || this.posY > 1000 || this.isCollided || this.feiXingTick > 20 * 1000 || (this.motionX == 0 && this.motionY == 0 && this.motionZ == 0)))
                    {
                        setExplode();
                        return;
                    }

                    this.moveEntity(this.motionX, this.motionY, this.motionZ);
                }
                else
                {
                    // Start the launch
                    if (this.qiFeiGaoDu > 0)
                    {
                        this.motionY = SPEED * this.feiXingTick * (this.feiXingTick / 2);
                        this.motionX = 0;
                        this.motionZ = 0;
                        this.qiFeiGaoDu -= this.motionY;
                        this.moveEntity(this.motionX, this.motionY, this.motionZ);

                        if (this.qiFeiGaoDu <= 0)
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

                        ((Explosion) this.explosiveID.handler).update(this);

                        this.moveEntity(this.motionX, this.motionY, this.motionZ);

                        // If the missile contacts anything, it will explode.
                        if (this.isCollided)
                        {
                            this.explode();
                        }

                        // If the missile is commanded to explode before impact
                        if (this.targetHeight > 0 && this.motionY < 0)
                        {
                            // Check the block below it.
                            Block block = this.worldObj.getBlock((int) this.posX, (int) this.posY - targetHeight, (int) this.posZ);

                            if (block != null && block != Blocks.air)
                            {
                                this.targetHeight = 0;
                                this.explode();
                            }
                        }
                    } // end else
                }
            }
            else
            {
                this.rotationPitch = (float) (Math.atan(this.motionY / (Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ))) * 180 / Math.PI);
                // Look at the next point
                this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180 / Math.PI);
            }

            this.lastTickPosX = this.posX;
            this.lastTickPosY = this.posY;
            this.lastTickPosZ = this.posZ;

            this.spawnMissileSmoke();
            this.protectionTime--;
            this.feiXingTick++;
        }
        else if (this.missileType != MissileType.LAUNCHER)
        {
            // Check to find the launcher in which this missile belongs in.
            ILauncherContainer launcher = this.getLauncher();

            if (launcher != null)
            {
                launcher.setContainingMissile(this);

                /** Rotate the missile to the cruise launcher's rotation. */
                if (launcher instanceof TileCruiseLauncher)
                {
                    this.missileType = MissileType.CruiseMissile;
                    this.noClip = true;

                    if (this.worldObj.isRemote)
                    {
                        this.rotationYaw = -((TileCruiseLauncher) launcher).rotationYaw + 90;
                        this.rotationPitch = ((TileCruiseLauncher) launcher).rotationPitch;
                    }

                    this.posY = ((TileCruiseLauncher) launcher).yCoord + 1;
                }
            }
            else
            {
                this.setDead();
            }
        }

        super.onUpdate();
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
        if (this.explosiveID != null)
        {
            if (((Explosion) this.explosiveID.handler).onInteract(this, entityPlayer))
            {
                return true;
            }
        }

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
        if (this.worldObj.isRemote)
        {
            Pos position = new Pos(this);
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
        Pos guJiDiDian = new Pos(this);
        double tempMotionY = this.motionY;

        if (this.feiXingTick > 20)
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
        RadarRegistry.remove(this);

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
                    if (!this.worldObj.isRemote)
                    {
                        this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 5F, true);
                    }
                }
                else
                {
                    ((Explosion) this.explosiveID.handler).createExplosion(this.worldObj, this.posX, this.posY, this.posZ, this);
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
        if (!this.isExpoding && !this.worldObj.isRemote)
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

    /** (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.startPos = new Pos(nbt.getCompoundTag("kaiShi"));
        this.targetVector = new Pos(nbt.getCompoundTag("muBiao"));
        this.launcherPos = new Pos(nbt.getCompoundTag("faSheQi"));
        this.acceleration = nbt.getFloat("jiaSu");
        this.targetHeight = nbt.getInteger("baoZhaGaoDu");
        this.explosiveID = Explosives.get(nbt.getInteger("haoMa"));
        this.feiXingTick = nbt.getInteger("feiXingTick");
        this.qiFeiGaoDu = nbt.getDouble("qiFeiGaoDu");
        this.missileType = MissileType.values()[nbt.getInteger("xingShi")];
        this.nbtData = nbt.getCompoundTag("data");
    }

    /** (abstract) Protected helper method to write subclass entity data to NBT. */
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        if (this.startPos != null)
        {
            nbt.setTag("kaiShi", this.startPos.toNBT());
        }
        if (this.targetVector != null)
        {
            nbt.setTag("muBiao", this.targetVector.toNBT());
        }

        if (this.launcherPos != null)
        {
            nbt.setTag("faSheQi", this.launcherPos.toNBT());
        }

        nbt.setFloat("jiaSu", this.acceleration);
        nbt.setInteger("haoMa", this.explosiveID.ordinal());
        nbt.setInteger("baoZhaGaoDu", this.targetHeight);
        nbt.setInteger("feiXingTick", this.feiXingTick);
        nbt.setDouble("qiFeiGaoDu", this.qiFeiGaoDu);
        nbt.setInteger("xingShi", this.missileType.ordinal());
        nbt.setTag("data", this.nbtData);
    }

    @Override
    public float getShadowSize()
    {
        return 1.0F;
    }

    @Override
    public int getTicksInAir()
    {
        return this.feiXingTick;
    }

    @Override
    public Explosive getExplosiveType()
    {
        return this.explosiveID.handler;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage)
    {
        if (DamageUtility.canHarm(this, source, damage))
        {
            this.damage += damage;
            if (this.damage >= this.max_damage)
            {
                this.setDead();
            }
            return true;
        }
        return false;
    }

    @Override
    public NBTTagCompound getTagCompound()
    {
        return this.nbtData;
    }


    @Override
    public ItemStack getExplosiveStack()
    {
        return null;
    }

    @Override
    public boolean setExplosiveStack(ItemStack stack)
    {
        return false;
    }

    public enum MissileType
    {
        MISSILE,
        CruiseMissile,
        LAUNCHER
    }

}