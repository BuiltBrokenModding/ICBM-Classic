package icbm.classic.content.entity;

import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.explosion.IExplosive;
import icbm.classic.api.explosion.IExplosiveContainer;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.lib.emp.CapabilityEMP;
import icbm.classic.lib.emp.CapabilityEmpKill;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;

public class EntityExplosive extends Entity implements IRotatable, IEntityAdditionalSpawnData, IExplosiveContainer
{
    // How long the fuse is (in ticks)
    public int fuse = 90;

    // The ID of the explosive
    public Explosives explosiveID;

    private byte orientation = 3;

    public NBTTagCompound nbtData = new NBTTagCompound();

    public IEMPReceiver capabilityEMP;

    public EntityExplosive(World par1World)
    {
        super(par1World);
        this.fuse = 0;
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        //this.yOffset = this.height / 2.0F;
        capabilityEMP = new CapabilityEmpKill(this);
    }

    public EntityExplosive(World par1World, Pos position, byte orientation, Explosives explosiveID)
    {
        this(par1World);
        this.setPosition(position.x(), position.y(), position.z());
        float var8 = (float) (Math.random() * Math.PI * 2.0D);
        this.motionX = (-((float) Math.sin(var8)) * 0.02F);
        this.motionY = 0.20000000298023224D;
        this.motionZ = (-((float) Math.cos(var8)) * 0.02F);
        this.prevPosX = position.x();
        this.prevPosY = position.y();
        this.prevPosZ = position.z();
        this.explosiveID = explosiveID;
        this.fuse = explosiveID.handler.getYinXin();
        this.orientation = orientation;

        explosiveID.handler.yinZhaQian(par1World, this);
    }

    public EntityExplosive(World par1World, Pos position, Explosives explosiveID, byte orientation, NBTTagCompound nbtData)
    {
        this(par1World, position, orientation, explosiveID);
        this.nbtData = nbtData;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEMP.EMP)
        {
            return (T) capabilityEMP;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public String getName()
    {
        return "Explosives";
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionX *= 0.95;
        this.motionY -= 0.045D;
        this.motionZ *= 0.95;

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        if (this.fuse < 1)
        {
            this.explode();
        }
        else
        {
            this.explosiveID.handler.onYinZha(this.world, new Pos(this.posX, this.posY, this.posZ), this.fuse);
        }

        this.fuse--;

        super.onUpdate();
    }

    public void explode()
    {
        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        this.getExplosiveType().createExplosion(this.world, new BlockPos(this.posX, this.posY, this.posZ), this);
        this.setDead();
    }

    /** (abstract) Protected helper method to read subclass entity data from NBT. */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.fuse = nbt.getByte("Fuse");
        this.explosiveID = Explosives.get(nbt.getInteger("explosiveID"));
        this.nbtData = nbt.getCompoundTag("data");
    }

    /** (abstract) Protected helper method to write subclass entity data to NBT. */
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("Fuse", (byte) this.fuse);
        nbt.setInteger("explosiveID", this.explosiveID.ordinal());
        nbt.setTag("data", this.nbtData);
    }

    @Override
    protected void entityInit()
    {
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return true;
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
    public EnumFacing getDirection()
    {
        return EnumFacing.getFront(this.orientation);
    }

    @Override
    public void setDirection(EnumFacing facingDirection)
    {
        this.orientation = (byte) facingDirection.ordinal();
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.explosiveID.ordinal());
        data.writeInt(this.fuse);
        data.writeByte(this.orientation);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.explosiveID = Explosives.get(data.readInt());
        this.fuse = data.readInt();
        this.orientation = data.readByte();
    }

    public IExplosive getExplosiveType()
    {
        return this.explosiveID.handler;
    }

    public NBTTagCompound getExplosiveData()
    {
        return this.nbtData;
    }
}
