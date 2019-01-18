package icbm.classic.content.entity;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.imp.transform.vector.Pos;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import icbm.classic.ICBMClassic;
import icbm.classic.Settings;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.explosion.ExplosionEvent.ExplosivePreDetonationEvent;
import resonant.api.explosion.ExplosiveType;
import resonant.api.explosion.IExplosiveContainer;

public class EntityExplosive extends Entity implements IRotatable, IEntityAdditionalSpawnData, IExplosiveContainer
{
    // How long the fuse is (in ticks)
    public int fuse = 90;

    // The ID of the explosive
    public Explosives explosiveID;

    private byte orientation = 3;

    public NBTTagCompound nbtData = new NBTTagCompound();

    public static int globalExplosiveCount =0;

    public EntityExplosive(World world)
    {
        super(world);
        this.fuse = 0;
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        this.yOffset = this.height / 2.0F;

	    globalExplosiveCount++;
	    if (Settings.MAXIMUM_CONCURRENT_EXPLOSIONS > 0 && globalExplosiveCount > Settings.MAXIMUM_CONCURRENT_EXPLOSIONS)
	    {
	        setDead();
	    }
    }

    public EntityExplosive(World world, Pos position, byte orientation, Explosives explosiveID)
    {
        this(world);
        this.setPosition(position.x(), position.y(), position.z());
        float var8 = (float) (Math.random() * Math.PI * 2.0D);
        this.motionX = (-((float) Math.sin(var8)) * 0.02F);
        this.motionY = 0.20000000298023224D;
        this.motionZ = (-((float) Math.cos(var8)) * 0.02F);
        this.prevPosX = position.x();
        this.prevPosY = position.y();
        this.prevPosZ = position.z();
        this.explosiveID = explosiveID;
        this.fuse = explosiveID.handler.getFuseTime();
        this.orientation = orientation;

        explosiveID.handler.playFuseSound(world, this);
    }

    public EntityExplosive(World world, Pos position, Explosives explosiveID, byte orientation, NBTTagCompound nbtData)
    {
        this(world, position, orientation, explosiveID);
        this.nbtData = nbtData;
    }

    @Override
    public String getCommandSenderName()
    {
        return "Explosives";
    }

	@Override
	public boolean attackEntityFrom(DamageSource source, float f) {
    	if(Settings.EXPLOSIONS_DESTROY_EXPLOSIVES && source.isExplosion())
    	{
		    this.setDead();
		    return false;
	    }
		return super.attackEntityFrom(source, f);
	}

	/** Called to update the entity's position/logic. */
    @Override
    public void onUpdate()
    {
        if (!this.worldObj.isRemote)
        {
            ExplosivePreDetonationEvent evt = new ExplosivePreDetonationEvent(worldObj, posX, posY, posZ, ExplosiveType.BLOCK, explosiveID.handler);
            MinecraftForge.EVENT_BUS.post(evt);

            if (evt.isCanceled())
            {
                ICBMClassic.blockExplosive.dropBlockAsItem(this.worldObj, (int) this.posX, (int) this.posY, (int) this.posZ, this.explosiveID.ordinal(), 0);
                this.setDead();
                return;
            }
            else if(this.isDead)
            {
                return;
            }
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        this.motionX *= 0.95;
        this.motionY -= 0.045D;
        this.motionZ *= 0.95;

        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.fuse < 1)
        {
            this.explode();
        }
        else
        {
            this.explosiveID.handler.onFuseTick(this.worldObj, new Pos(this.posX, this.posY, this.posZ), this.fuse);
        }

        this.fuse--;

        super.onUpdate();
    }

    public void explode()
    {
        this.worldObj.spawnParticle("hugeexplosion", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        this.getExplosiveType().createExplosion(this.worldObj, this.posX, this.posY, this.posZ, this);
        this.setDead();
    }

	@Override
	public void setDead()
	{
		super.setDead();
		globalExplosiveCount--;
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
    	if(Settings.REMOVE_UNLOADED_EXPLOSIVES)
    	{
		    worldObj.removeEntity(this);
	    }
	    else
	    {
		    nbt.setByte("Fuse", (byte) this.fuse);
		    nbt.setInteger("explosiveID", this.explosiveID.ordinal());
		    nbt.setTag("data", this.nbtData);
	    }
    }

    @Override
    public float getShadowSize()
    {
        return 0.5F;
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
    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(this.orientation);
    }

    @Override
    public void setDirection(ForgeDirection facingDirection)
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
}
