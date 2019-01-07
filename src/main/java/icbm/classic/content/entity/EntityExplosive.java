package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.emp.CapabilityEMP;
import icbm.classic.lib.emp.CapabilityEmpKill;
import icbm.classic.lib.explosive.cap.CapabilityExplosive;
import icbm.classic.lib.explosive.cap.CapabilityExplosiveEntity;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;

public class EntityExplosive extends Entity implements IRotatable, IEntityAdditionalSpawnData
{

    // How long the fuse is (in ticks)
    public int fuse = -1;

    private byte orientation = 3;

    public IEMPReceiver capabilityEMP;
    public CapabilityExplosive capabilityExplosive;

    public EntityExplosive(World par1World)
    {
        super(par1World);
        this.fuse = 0;
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        //this.yOffset = this.height / 2.0F;
        capabilityEMP = new CapabilityEmpKill(this);
    }

    public EntityExplosive(World par1World, Pos position, byte orientation, ItemStack stack)
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

        this.capabilityExplosive = new CapabilityExplosiveEntity(this, stack);
        this.orientation = orientation;
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
            return (T) capabilityExplosive;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public String getName()
    {
        if (capabilityExplosive != null && capabilityExplosive.getExplosiveData() != null)
        {
            return "Explosive[" + capabilityExplosive.getExplosiveData().getRegistryName() + "]";
        }
        return "Explosive";
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
        if(fuse == -1)
        {
            final IExplosiveData data = capabilityExplosive.getExplosiveData();
            if(data != null)
            {
                this.fuse = ICBMClassicAPI.EX_BLOCK_REGISTRY.getFuseTime(world, posX, posY, posZ, data.getRegistryID());
            }
            else
            {
                fuse = 90; //TODO config default
            }
        }

        //Tick fuse
        if (this.fuse-- < 1)
        {
            this.explode();
        }
        else
        {
            //this.explosiveID.handler.onFuseTick(this.world, new Pos(this.posX, this.posY, this.posZ), this.fuse); TODO fix
        }

        super.onUpdate();
    }

    public void explode()
    {
        this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
        ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY, this.posZ, capabilityExplosive);
        this.setDead();
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.fuse = nbt.getByte("fuse");
        capabilityExplosive.deserializeNBT(nbt.getCompoundTag("explosive"));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setByte("fuse", (byte) this.fuse);
        nbt.setTag("explosive", capabilityExplosive.serializeNBT());
    }

    public static void registerDataFixer()
    {
        ModFixs mf = FMLCommonHandler.instance().getDataFixer().init(ICBMClassic.DOMAIN, 1);
        mf.registerFix(FixTypes.ENTITY, new IFixableData()
        {
            @Override
            public int getFixVersion()
            {
                return 1;
            }

            @Override
            public NBTTagCompound fixTagCompound(NBTTagCompound compound)
            {
                if(compound.hasKey("id") && compound.getString("id").equalsIgnoreCase())
                {
                    if(compound.hasKey("Fuse"))
                    {
                        compound.setString("fuse", compound.getString("Fuse"));
                        compound.removeTag("Fuse");
                    }

                    if(compound.hasKey("explosiveID"))
                    {
                        NBTTagCompound exSave = new NBTTagCompound();
                        exSave.setInteger(CapabilityExplosive.NBT_ID, compound.getInteger("explosiveID"));
                        if(compound.hasKey("data"))
                        {
                            exSave.setTag(CapabilityExplosive.NBT_BLAST_DATA, compound.getTag("data"));
                        }

                        compound.removeTag("explosiveID");
                        compound.removeTag("data");
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

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for
     * spiders and wolves to prevent them from trampling crops
     */
    @Override
    protected boolean canTriggerWalking()
    {
        return true;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    public EnumFacing getDirection()
    {
        return EnumFacing.byIndex(this.orientation);
    }

    @Override
    public void setDirection(EnumFacing facingDirection)
    {
        this.orientation = (byte) facingDirection.ordinal();
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.fuse);
        data.writeByte(this.orientation);
        ByteBufUtils.writeTag(data, capabilityExplosive.serializeNBT());
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.fuse = data.readInt();
        this.orientation = data.readByte();
        capabilityExplosive.deserializeNBT(ByteBufUtils.readTag(data));
    }
}
