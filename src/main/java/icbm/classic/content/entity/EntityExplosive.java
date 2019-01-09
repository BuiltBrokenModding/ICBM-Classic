package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.EntityRefs;
import icbm.classic.api.ExplosiveRefs;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.emp.CapabilityEmpKill;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
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
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;

public class EntityExplosive extends Entity implements IRotatable, IEntityAdditionalSpawnData
{

    public static final String NBT_FUSE = "Fuse";

    // How long the fuse is (in ticks)
    public int fuse = -1;

    private EnumFacing _facing = EnumFacing.NORTH;

    public IEMPReceiver capabilityEMP;
    public CapabilityExplosive capabilityExplosive;

    public EntityExplosive(World par1World)
    {
        super(par1World);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        //this.yOffset = this.height / 2.0F;
        capabilityEMP = new CapabilityEmpKill(this);
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

        this.capabilityExplosive = new CapabilityExplosiveEntity(this, stack);
        this._facing = orientation;
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
        if (fuse == -1)
        {
            final IExplosiveData data = capabilityExplosive.getExplosiveData();
            if (data != null)
            {
                this.fuse = ICBMClassicAPI.EX_BLOCK_REGISTRY.getFuseTime(world, posX, posY, posZ, data.getRegistryID());
            }
            else
            {
                fuse = 90; //TODO config default
            }
        }

        //Tick fuse to render effects
        ICBMClassicAPI.EX_BLOCK_REGISTRY.tickFuse(world, posX, posY, posZ, this.fuse, capabilityExplosive.explosiveID);

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
        ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY, this.posZ, capabilityExplosive);
        this.setDead();
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        this.fuse = nbt.getByte(NBT_FUSE);
        capabilityExplosive.deserializeNBT(nbt.getCompoundTag(CapabilityExplosive.NBT_EXPLOIVE));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        nbt.setByte(NBT_FUSE, (byte) this.fuse);
        nbt.setTag(CapabilityExplosive.NBT_EXPLOIVE, capabilityExplosive.serializeNBT());
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
                //Match to entity, we get all entity tags as input
                if (compound.hasKey("id") && compound.getString("id").equalsIgnoreCase(EntityRefs.BLOCK_EXPLOSIVE.toString()))
                {
                    //Fix explosive ID save
                    if (compound.hasKey("explosiveID"))
                    {
                        //Convert data
                        NBTTagCompound exSave = new NBTTagCompound();
                        exSave.setInteger(CapabilityExplosive.NBT_EXPLOSIVE_ID, compound.getInteger("explosiveID"));
                        if (compound.hasKey("data"))
                        {
                            exSave.setTag(CapabilityExplosive.NBT_BLAST_DATA, compound.getTag("data"));
                        }

                        //Remove old tags
                        compound.removeTag("explosiveID");
                        compound.removeTag("data");

                        //Save
                        compound.setTag(CapabilityExplosive.NBT_EXPLOIVE, exSave);
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
        ByteBufUtils.writeTag(data, capabilityExplosive.serializeNBT());
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.fuse = data.readInt();
        this._facing = EnumFacing.byIndex(data.readByte());
        capabilityExplosive.deserializeNBT(ByteBufUtils.readTag(data));
    }

    public IExplosiveData getExplosiveData()
    {
        if (capabilityExplosive != null)
        {
            IExplosiveData data = capabilityExplosive.getExplosiveData();
            if (data != null)
            {
                return data;
            }
        }
        return ExplosiveRefs.CONDENSED;
    }
}
