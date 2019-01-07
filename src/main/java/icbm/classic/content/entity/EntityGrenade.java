package icbm.classic.content.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityGrenade extends Entity implements IEntityAdditionalSpawnData
{
    /** Is the entity that throws this 'thing' (snowball, ender pearl, eye of ender or potion) */
    protected EntityLivingBase thrower;

    public Explosives explosiveID;
    public NBTTagCompound nbtData = new NBTTagCompound();

    public EntityGrenade(World par1World)
    {
        super(par1World);
        this.setSize(0.3F, 0.3F);
        //this.renderDistanceWeight = 8;
    }

    public EntityGrenade(World par1World, Pos position, Explosives explosiveID)
    {
        this(par1World);
        this.setPosition(position.x(), position.y(), position.z());
        this.explosiveID = explosiveID;
    }

    public EntityGrenade(World par1World, EntityLivingBase par2EntityLiving, Explosives explosiveID, float nengLiang)
    {
        this(par1World);
        this.thrower = par2EntityLiving;
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(par2EntityLiving.posX, par2EntityLiving.posY + par2EntityLiving.getEyeHeight(), par2EntityLiving.posZ, par2EntityLiving.rotationYaw, par2EntityLiving.rotationPitch);
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        this.posY -= 0.10000000149011612D;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        this.setPosition(this.posX, this.posY, this.posZ);
        //this.yOffset = 0.0F;
        float var3 = 0.4F;
        this.motionX = -MathHelper.sin(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * var3;
        this.motionZ = MathHelper.cos(this.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float) Math.PI) * var3;
        this.motionY = -MathHelper.sin((this.rotationPitch) / 180.0F * (float) Math.PI) * var3;
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 1.8f * nengLiang, 1.0F);
        this.explosiveID = explosiveID;
    }

    @Override
    public String getName()
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(this.explosiveID.ordinal());
        if (data != null)
        {
            return "icbm.grenade." + data.getRegistryName();
        }
        return "icbm.grenade";
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.explosiveID.ordinal());
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.explosiveID = Explosives.get(data.readInt());
    }

    /** Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction. */
    public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
    {
        float var9 = MathHelper.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= var9;
        par3 /= var9;
        par5 /= var9;
        par1 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par3 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par5 += this.rand.nextGaussian() * 0.007499999832361937D * par8;
        par1 *= par7;
        par3 *= par7;
        par5 *= par7;
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;
        float var10 = MathHelper.sqrt(par1 * par1 + par5 * par5);
        this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(par3, var10) * 180.0D / Math.PI);
    }

    /** Sets the velocity to the args. Args: x, y, z */
    @Override
    public void setVelocity(double par1, double par3, double par5)
    {
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float var7 = MathHelper.sqrt(par1 * par1 + par5 * par5);
            this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(par1, par5) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(par3, var7) * 180.0D / Math.PI);
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
        float var16 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float) (Math.atan2(this.motionY, var16) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
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

        if (this.ticksExisted > Math.max(60, (explosiveID.handler.getFuseTimer())))
        {
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
            (explosiveID.handler).createExplosion(this.world, new BlockPos(this.posX, this.posY + 0.3f, this.posZ), this, 1);
            this.setDead();
            return;
        }
        else
        {
            (explosiveID.handler).onFuseTick(this.world, new Pos(this.posX, this.posY + 0.5, this.posZ), this.ticksExisted);
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
        this.explosiveID = Explosives.get(nbt.getInteger("haoMa"));
        this.nbtData = nbt.getCompoundTag("data");

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("haoMa", this.explosiveID.ordinal());
        nbt.setTag("data", this.nbtData);

    }
}