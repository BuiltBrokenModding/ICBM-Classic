package icbm.classic.content.entity;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityExplosive extends Entity implements IRotatable, IEntityAdditionalSpawnData
{
    // How long the fuse is (in ticks)
    public int fuse = 90;

    // The ID of the explosive
    public Explosives explosiveID;

    private byte orientation = 3;

    public NBTTagCompound nbtData = new NBTTagCompound();

    public EntityExplosive(World par1World)
    {
        super(par1World);
        this.fuse = 0;
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        //this.yOffset = this.height / 2.0F;
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
        this.getExplosiveType().createExplosion(this.world, this.posX, this.posY, this.posZ, this);
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

    /** returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops */
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
    public Direction getDirection()
    {
        return Direction.getOrientation(this.orientation);
    }

    @Override
    public void setDirection(Direction facingDirection)
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

    public Explosive getExplosiveType()
    {
        return this.explosiveID.handler;
    }

    public NBTTagCompound getTagCompound()
    {
        return this.nbtData;
    }
}
