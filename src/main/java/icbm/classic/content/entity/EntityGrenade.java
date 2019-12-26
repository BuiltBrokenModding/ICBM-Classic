package icbm.classic.content.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.NBTConstants;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.explosive.ExplosiveHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityGrenade extends Entity implements IEntityAdditionalSpawnData
{
    /** Entity that created the grenade and set it into motion */
    protected EntityLivingBase thrower;

    @Deprecated
    public int explosiveID; //TODO move to capability
    @Deprecated
    public NBTTagCompound blastData = new NBTTagCompound(); //TODO move to capability

    public EntityGrenade(World par1World)
    {
        super(par1World);
        this.setSize(0.25F, 0.25F);
        //this.renderDistanceWeight = 8;
    }

    public EntityGrenade setItemStack(ItemStack stack)
    {
        this.explosiveID = stack.getItemDamage();
        return this;
    }

    public EntityGrenade setThrower(EntityLivingBase thrower)
    {
        this.thrower = thrower;
        return this;
    }

    public EntityGrenade aimFromThrower()
    {
        this.setLocationAndAngles(thrower.posX, thrower.posY + thrower.getEyeHeight(), thrower.posZ, thrower.rotationYaw, thrower.rotationPitch);

        //Set position
        final float horizontalOffset = 0.16F;
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * horizontalOffset;
        this.posY -= 0.10000000149011612D;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * horizontalOffset;
        this.setPosition(this.posX, this.posY, this.posZ);

        return this;
    }

    public EntityGrenade spawn()
    {
        world.spawnEntity(this);
        return this;
    }

    public EntityGrenade setThrowMotion(float energy)
    {
        //Set velocity
        final float powerScale = 0.4F;
        this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * powerScale;
        this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * powerScale;
        this.motionY = -MathHelper.sin((this.rotationPitch) / 180.0F * (float) Math.PI) * powerScale;
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 1.8f * energy, 1.0F);
        return this;
    }

    @Override
    public String getName()
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(this.explosiveID);
        if (data != null)
        {
            return "icbm.grenade." + data.getRegistryName();
        }
        return "icbm.grenade";
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.explosiveID);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.explosiveID = data.readInt();
    }

    /**
     * Sets the velocity of the grenade
     *
     * @param vx     - x component velocity vector
     * @param vy     - y component velocity vector
     * @param vz     - z component velocity vector
     * @param scale  - amount to scale the vector by
     * @param random - amount to randomize the vector
     */
    public void setThrowableHeading(double vx, double vy, double vz, float scale, float random)
    {
        //normalize
        float power = MathHelper.sqrt(vx * vx + vy * vy + vz * vz);
        vx /= power;
        vy /= power;
        vz /= power;

        //Randomize
        vx += this.rand.nextGaussian() * 0.007499999832361937D * random;
        vy += this.rand.nextGaussian() * 0.007499999832361937D * random;
        vz += this.rand.nextGaussian() * 0.007499999832361937D * random;

        //Scale
        vx *= scale;
        vy *= scale;
        vz *= scale;

        //Apply
        setVelocity(vx, vy, vz);
    }

    /** Sets the velocity to the args. Args: x, y, z */
    @Override
    public void setVelocity(double vx, double vy, double vz)
    {
        this.motionX = vx;
        this.motionY = vy;
        this.motionZ = vz;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float var7 = MathHelper.sqrt(vx * vx + vz * vz);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(vx, vz) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(vy, var7) * 180.0D / Math.PI);
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    protected void entityInit()
    {
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onUpdate();

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        final float horizontalMag = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float) (Math.atan2(this.motionY, horizontalMag) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
        float var17 = 0.98F;
        float gravity = 0.03F;

        if (this.isInWater())
        {
            for (int var7 = 0; var7 < 4; ++var7)
            {
                float var19 = 0.25F;
                this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * var19, this.posY - this.motionY * var19, this.posZ - this.motionZ * var19, this.motionX, this.motionY, this.motionZ);
            }

            var17 = 0.8F;
        }

        this.motionX *= var17;
        this.motionY *= var17;
        this.motionZ *= var17;

        if (this.onGround)
        {
            this.motionX *= 0.5;
            this.motionZ *= 0.5;
            this.motionY *= 0.5;
        }
        else
        {
            this.motionY -= gravity;
            //this.pushOutOfBlocks(this.posX, (this.boundingBox.minY + this.boundingBox.maxY) / 2.0D, this.posZ);
        }

        tickFuse();
    }

    protected void tickFuse()
    {
        if (this.ticksExisted > ICBMClassicAPI.EX_GRENADE_REGISTRY.getFuseTime(this, explosiveID))
        {
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
            ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY + 0.3f, this.posZ, explosiveID, 1, blastData);
            this.setDead();
        }
        else
        {
            ICBMClassicAPI.EX_GRENADE_REGISTRY.tickFuse(this, explosiveID, ticksExisted);
        }
    }

    /** Returns if this entity is in water and will end up adding the waters velocity to the entity */
    @Override
    public boolean handleWaterMovement()
    {
        return this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this);
    }

    /** Returns true if other Entities should be prevented from moving through this Entity. */
    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /** Returns true if this entity should push and be pushed by other entities when colliding. */
    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.explosiveID = nbt.getInteger(NBTConstants.EXPLOSIVE_ID);
        this.blastData = nbt.getCompoundTag(NBTConstants.DATA);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.EXPLOSIVE_ID, this.explosiveID);
        nbt.setTag(NBTConstants.DATA, this.blastData);
    }
}