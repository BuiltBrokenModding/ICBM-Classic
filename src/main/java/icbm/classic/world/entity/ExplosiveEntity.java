package icbm.classic.world.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.emp.CapabilityEmpKill;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.explosive.ExplosiveHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;

public class ExplosiveEntity extends Entity implements IRotatable {
    // How long the fuse is (in ticks)
    public int fuse = -1;

    private Direction _facing = Direction.NORTH;

    //Capabilities
    public final IEMPReceiver capabilityEMP = new CapabilityEmpKill(this);
    public final CapabilityExplosiveEntity capabilityExplosive = new CapabilityExplosiveEntity(this);

    public ExplosiveEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.preventEntitySpawning = true;
        this.setSize(0.98F, 0.98F);
        //this.yOffset = this.height / 2.0F;
    }

//    public ExplosiveEntity(Level par1World, Pos position, Direction orientation, ItemStack stack)
//    {
//        this(par1World);
//        this.setPosition(position.x(), position.y(), position.z());
//        float var8 = (float) (Math.random() * Math.PI * 2.0D);
//        this.motionX = (-((float) Math.sin(var8)) * 0.02F);
//        this.motionY = 0.20000000298023224D;
//        this.motionZ = (-((float) Math.cos(var8)) * 0.02F);
//        this.prevPosX = position.x();
//        this.prevPosY = position.y();
//        this.prevPosZ = position.z();
//        this._facing = orientation;
//
//        capabilityExplosive.setStack(stack);
//    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {
        this.prevPosX = this.getX();
        this.prevPosY = this.getY();
        this.prevPosZ = this.getZ();

        this.motionX *= 0.95;
        this.motionY -= 0.045D;
        this.motionZ *= 0.95;

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
            this.motionY *= -0.5D;
        }

        //Init fuse
        if (fuse == -1) {
            this.fuse = ICBMClassicAPI.EX_BLOCK_REGISTRY.getFuseTime(world, posX, posY, posZ, getExplosiveData().getRegistryID());
        }

        //Tick fuse to render effects
        ICBMClassicAPI.EX_BLOCK_REGISTRY.tickFuse(world, posX, posY, posZ, this.fuse, getExplosiveData().getRegistryID());

        //Tick fuse
        if (this.fuse-- < 1) {
            this.explode();
        }

        super.onUpdate();
    }

    public void explode() {
        ExplosiveHandler.createExplosion(this, this.world, this.getX() + 0.5D, this.getY() + 0.5D, this.getZ() + 0.5D, getExplosiveCap());
        this.setDead();
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
    protected void readEntityFromNBT(CompoundTag nbt) {
        this.fuse = nbt.getByte(NBTConstants.FUSE);
        getExplosiveCap().deserializeNBT(nbt.getCompound(NBTConstants.EXPLOSIVE_STACK));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
    protected void writeEntityToNBT(CompoundTag nbt) {
        nbt.setByte(NBTConstants.FUSE, (byte) this.fuse);
        nbt.put(NBTConstants.EXPLOSIVE_STACK, getExplosiveCap().serializeNBT());
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected boolean canTriggerWalking() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }

    @Override
    public Direction getDirection() {
        if (_facing == null) {
            _facing = Direction.NORTH;
        }
        return this._facing;
    }

    @Override
    public void setDirection(Direction facingDirection) {
        this._facing = facingDirection;
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeInt(this.fuse);
        data.writeByte(getDirection().ordinal());
        ByteBufUtils.writeTag(data, getExplosiveCap().serializeNBT());
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        this.fuse = data.readInt();
        this._facing = Direction.getFront(data.readByte());
        getExplosiveCap().deserializeNBT(ByteBufUtils.readTag(data));
    }

    public CapabilityExplosiveEntity getExplosiveCap() {
        return capabilityExplosive;
    }

    public ExplosiveType getExplosiveData() {
        if (getExplosiveCap() != null) {
            final ExplosiveType data = getExplosiveCap().getExplosiveData();
            if (data != null) {
                return data;
            }
        }
        return ICBMExplosives.CONDENSED;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityEMP.EMP) {
            return (T) capabilityEMP;
        } else if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return (T) getExplosiveCap();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        if (capability == CapabilityEMP.EMP) {
            return true;
        } else if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public String getName() {
        if (getExplosiveData() != null) {
            return "Explosive[" + getExplosiveData().getRegistryName() + "]";
        }
        return "Explosive";
    }
}
