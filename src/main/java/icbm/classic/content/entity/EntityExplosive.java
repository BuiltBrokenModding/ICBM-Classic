package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.EntityRefs;
import icbm.classic.api.ExplosiveRefs;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.NBTConstants;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.emp.CapabilityEmpKill;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;

public class EntityExplosive extends Entity implements IRotatable, IEntityAdditionalSpawnData
{
    // How long the fuse is (in ticks)
    public int fuse = -1;

    private EnumFacing _facing = EnumFacing.NORTH;

    //Capabilities
    public final IEMPReceiver capabilityEMP = new CapabilityEmpKill(this);
    public final CapabilityExplosiveEntity capabilityExplosive = new CapabilityExplosiveEntity(this);

    public EntityExplosive(World par1World)
    {
        super(par1World);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        //this.yOffset = this.height / 2.0F;
    }

    public EntityExplosive(World par1World, Pos position, EnumFacing orientation, ItemStack stack)
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
        this._facing = orientation;

        capabilityExplosive.setStack(stack);
    }

    /**
     * Called to update the entity's position/logic.
     */
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

        //Init fuse
        if (fuse == -1)
        {
            this.fuse = ICBMClassicAPI.EX_BLOCK_REGISTRY.getFuseTime(world, posX, posY, posZ, getExplosiveData().getRegistryID());
        }

        //Tick fuse to render effects
        ICBMClassicAPI.EX_BLOCK_REGISTRY.tickFuse(world, posX, posY, posZ, this.fuse, getExplosiveData().getRegistryID());

        //Tick fuse
        if (this.fuse-- < 1)
        {
            this.explode();
        }

        super.onUpdate();
    }

    public void explode()
    {
        //TODO hook particles to blast, as well hook to explosive handler
        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY, this.posZ, getExplosiveCap());
        this.setDead();
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.fuse = nbt.getByte(NBTConstants.FUSE);
        getExplosiveCap().deserializeNBT(nbt.getCompoundTag(NBTConstants.EXPLOSIVE_STACK));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setByte(NBTConstants.FUSE, (byte) this.fuse);
        nbt.setTag(NBTConstants.EXPLOSIVE_STACK, getExplosiveCap().serializeNBT());
    }

    public static void registerDataFixer()
    {
        ICBMClassic.modFixs.registerFix(FixTypes.ENTITY, new IFixableData()
        {
            @Override
            public int getFixVersion()
            {
                return 1;
            }

            @Override
            public NBTTagCompound fixTagCompound(NBTTagCompound compound)
            {
                //Match to entity, we get all entity tags as input
                if (compound.hasKey(NBTConstants.ID) && compound.getString(NBTConstants.ID).equalsIgnoreCase(EntityRefs.BLOCK_EXPLOSIVE.toString()))
                {
                    //Fix explosive ID save
                    if (compound.hasKey(NBTConstants.EXPLOSIVE_ID))
                    {
                        int id = compound.getInteger(NBTConstants.EXPLOSIVE_ID);

                        //Generate stack so we can serialize off it
                        final ItemStack stack = new ItemStack(BlockReg.blockExplosive, 1, id);

                        //Handle custom explosive data
                        final IExplosive ex = stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null);
                        if(ex instanceof CapabilityExplosiveStack)
                        {
                            if (compound.hasKey(NBTConstants.DATA))
                            {
                                ((CapabilityExplosiveStack)ex).setCustomData(compound.getCompoundTag(NBTConstants.DATA));
                            }
                        }

                        //Remove old tags
                        compound.removeTag(NBTConstants.EXPLOSIVE_ID);
                        compound.removeTag(NBTConstants.DATA);

                        //Save stack
                        compound.setTag(NBTConstants.EXPLOSIVE_STACK, stack.serializeNBT());
                    }
                }
                return compound;
            }
        });
    }

    @Override
    protected void entityInit()
    {
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    public EnumFacing getDirection()
    {
        if (_facing == null)
        {
            _facing = EnumFacing.NORTH;
        }
        return this._facing;
    }

    @Override
    public void setDirection(EnumFacing facingDirection)
    {
        this._facing = facingDirection;
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.fuse);
        data.writeByte(getDirection().ordinal());
        ByteBufUtils.writeTag(data, getExplosiveCap().serializeNBT());
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.fuse = data.readInt();
        this._facing = EnumFacing.byIndex(data.readByte());
        getExplosiveCap().deserializeNBT(ByteBufUtils.readTag(data));
    }

    public CapabilityExplosiveEntity getExplosiveCap()
    {
        return capabilityExplosive;
    }

    public IExplosiveData getExplosiveData()
    {
        if (getExplosiveCap() != null)
        {
            final IExplosiveData data = getExplosiveCap().getExplosiveData();
            if (data != null)
            {
                return data;
            }
        }
        return ExplosiveRefs.CONDENSED;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEMP.EMP)
        {
            return (T) capabilityEMP;
        }
        else if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY)
        {
            return (T) getExplosiveCap();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEMP.EMP)
        {
            return true;
        }
        else if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public String getName()
    {
        if (getExplosiveData() != null)
        {
            return "Explosive[" + getExplosiveData().getRegistryName() + "]";
        }
        return "Explosive";
    }
}
