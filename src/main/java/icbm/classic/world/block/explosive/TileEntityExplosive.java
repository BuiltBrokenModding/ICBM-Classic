package icbm.classic.world.block.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.world.entity.ExplosiveEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.common.capabilities.Capability;

import javax.annotation.Nullable;

public class BlockEntityExplosive extends BlockEntity implements IRotatable {
    public static final String NBT_EXPLOSIVE_STACK = "explosive_stack";

    /**
     * Is the tile currently exploding
     */
    public boolean hasBeenTriggered = false;

    public CapabilityExplosiveStack capabilityExplosive = new CapabilityExplosiveStack(null);

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        if (nbt.contains(NBT_EXPLOSIVE_STACK, 10)) {
            final CompoundTag itemStackTag = nbt.getCompound(NBT_EXPLOSIVE_STACK);
            capabilityExplosive = new CapabilityExplosiveStack(new ItemStack(itemStackTag));
        }
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        if (capabilityExplosive != null && capabilityExplosive.toStack() != null) {
            nbt.put(NBT_EXPLOSIVE_STACK, capabilityExplosive.toStack().serializeNBT());
        }
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY && capabilityExplosive != null || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return ICBMClassicAPI.EXPLOSIVE_CAPABILITY.cast(capabilityExplosive);
        }
        return super.getCapability(capability, facing);
    }

    public void trigger(boolean setFire) {
        if (!hasBeenTriggered) {
            hasBeenTriggered = true;
            ExplosiveEntity explosiveEntity = new ExplosiveEntity(world, new Pos(pos).add(0.5), getDirection(), capabilityExplosive.toStack());
            //TODO check for tick rate, trigger directly if tick is less than 3

            if (setFire) {
                explosiveEntity.setFire(100);
            }

            world.spawnEntity(explosiveEntity);
            world.setBlockToAir(pos);

            ICBMClassic.logger().info("BlockEntityExplosive: Triggered ITEM{" + capabilityExplosive.toStack() + "] " + capabilityExplosive.getExplosiveData().getRegistryName() + " at location " + getPos());
        }
    }

    @Override
    public SPacketUpdateBlockEntity getUpdatePacket() {
        return new SPacketUpdateBlockEntity(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return writeToNBT(new CompoundTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateBlockEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public Direction getDirection() {
        return Direction.getFront(this.getBlockMetadata());
    }

    @Override
    public void setDirection(Direction facingDirection) {
        BlockState state = world.getBlockState(pos);
        state = state.withProperty(ExplosiveBlock.ROTATION_PROP, facingDirection);
        this.world.setBlockState(pos, state, 2);
    }
}
