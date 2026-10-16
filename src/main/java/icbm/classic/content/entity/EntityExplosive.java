package icbm.classic.content.entity;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.emp.CapabilityEmpKill;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.capability.holder.ICapabilityHolderEntity;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nullable;

public class EntityExplosive extends Entity implements IRotatable, IEntityWithComplexSpawn, ICapabilityHolderEntity
{
    // How long the fuse is (in ticks)
    public int fuse = -1;

    private Direction _facing = Direction.NORTH;

    //Capabilities
    public final IEMPReceiver capabilityEMP = new CapabilityEmpKill<>(this);
    public final CapabilityExplosiveEntity capabilityExplosive = new CapabilityExplosiveEntity(this);

    public EntityExplosive(EntityType<? extends EntityExplosive> type, Level level)
    {
        super(type, level);
        this.blocksBuilding = true;
    }

    public EntityExplosive(Level level)
    {
        this(EntityReg.BLOCK_EXPLOSIVE.get(), level);
    }

    public EntityExplosive(Level level, Pos position, Direction orientation, ItemStack stack)
    {
        this(level);
        this.setPos(position.x(), position.y(), position.z());
        float var8 = (float) (Math.random() * Math.PI * 2.0D);
        this.setDeltaMovement(-((float) Math.sin(var8)) * 0.02F, 0.20000000298023224D, -((float) Math.cos(var8)) * 0.02F);
        this.xo = position.x();
        this.yo = position.y();
        this.zo = position.z();
        this._facing = orientation;

        capabilityExplosive.setStack(stack);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        Vec3 motion = this.getDeltaMovement();
        motion = new Vec3(motion.x * 0.95, motion.y - 0.045D, motion.z * 0.95);
        this.setDeltaMovement(motion);

        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.onGround())
        {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.699999988079071D, -0.5D, 0.699999988079071D));
        }

        //Init fuse
        if (fuse == -1)
        {
            this.fuse = ICBMClassicAPI.EX_BLOCK_REGISTRY.getFuseTime(level(), getX(), getY(), getZ(), getExplosiveData().getRegistryID());
        }

        //Tick fuse to render effects
        ICBMClassicAPI.EX_BLOCK_REGISTRY.tickFuse(level(), getX(), getY(), getZ(), this.fuse, getExplosiveData().getRegistryID());

        //Tick fuse
        if (this.fuse-- < 1)
        {
            this.explode();
        }

        super.tick();
    }

    public void explode()
    {
        getExplosiveCap().doExplosion(this.getX() + 0.5D, this.getY() + 0.5D, this.getZ() + 0.5D, new ActionSource(level(), new Vec3(getX(), getY(), getZ()), new EntityCause(this)));
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        // PORT: 1.12 saved the fuse as a byte which wrapped fuses over 127 ticks, getInt also reads old byte tags
        this.fuse = nbt.getInt(NBTConstants.FUSE);
        final ItemStack stack = ItemStack.parseOptional(registryAccess(), nbt.getCompound(NBTConstants.EXPLOSIVE_STACK));
        if (!stack.isEmpty())
        {
            getExplosiveCap().setStack(stack);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        nbt.putInt(NBTConstants.FUSE, this.fuse);
        nbt.put(NBTConstants.EXPLOSIVE_STACK, getExplosiveCap().toStack().saveOptional(registryAccess()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
    }

    @Override
    public boolean isPickable()
    {
        return true;
    }

    @Override
    public boolean isPushable()
    {
        return true;
    }

    @Override
    public Direction getDirection()
    {
        if (_facing == null)
        {
            _facing = Direction.NORTH;
        }
        return this._facing;
    }

    @Override
    public void setDirection(Direction facingDirection)
    {
        this._facing = facingDirection;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        data.writeInt(this.fuse);
        data.writeByte(getDirection().ordinal());
        ItemStack.OPTIONAL_STREAM_CODEC.encode(data, getExplosiveCap().toStack());
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        this.fuse = data.readInt();
        this._facing = Direction.from3DDataValue(data.readByte());
        final ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
        if (!stack.isEmpty())
        {
            getExplosiveCap().setStack(stack);
        }
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
        return ICBMExplosives.CONDENSED;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getEntityCapability(EntityCapability<T, Void> capability)
    {
        if (capability == ICBMClassicAPI.EMP_ENTITY_CAPABILITY)
        {
            return (T) capabilityEMP;
        }
        else if (capability == ICBMClassicAPI.EXPLOSIVE_ENTITY_CAPABILITY)
        {
            return (T) getExplosiveCap();
        }
        return null;
    }

    @Override
    public Component getName()
    {
        if (hasCustomName())
        {
            return super.getName();
        }
        if (getExplosiveData() != null)
        {
            return Component.literal("Explosive[" + getExplosiveData().getRegistryKey() + "]");
        }
        return Component.literal("Explosive");
    }
}
// PORTED
