package icbm.classic.content.blocks.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class TileEntityExplosive extends TileEntity implements IRotatable
{
    /**
     * Is the tile currently exploding
     */
    public boolean hasBeenTriggered = false;

    public CapabilityExplosiveStack capabilityExplosive = new CapabilityExplosiveStack(null);

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        capabilityExplosive = new CapabilityExplosiveStack(new ItemStack((NBTTagCompound)nbt.getTag(NBTConstants.EXPLOSIVE_STACK)));
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (capabilityExplosive != null && capabilityExplosive.toStack() != null)
        {
            nbt.setTag(NBTConstants.EXPLOSIVE_STACK, capabilityExplosive.toStack().serializeNBT());
        }
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY && capabilityExplosive != null || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY)
        {
            return (T) capabilityExplosive;
        }
        return super.getCapability(capability, facing);
    }

    public void trigger(boolean setFire)
    {
        if (!hasBeenTriggered)
        {
            hasBeenTriggered = true;
            EntityExplosive entityExplosive = new EntityExplosive(world, new Pos(pos).add(0.5), getDirection(), capabilityExplosive.toStack());
            //TODO check for tick rate, trigger directly if tick is less than 3

            if (setFire)
            {
                entityExplosive.setFire(100);
            }

            world.spawnEntity(entityExplosive);
            world.setBlockToAir(pos);

            ICBMClassic.logger().info("TileEntityExplosive: Triggered ITEM{" + capabilityExplosive.toStack() + "] " + capabilityExplosive.getExplosiveData().getRegistryName() + " at location " + getPos());
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public EnumFacing getDirection()
    {
        return EnumFacing.byIndex(this.getBlockMetadata());
    }

    @Override
    public void setDirection(EnumFacing facingDirection)
    {
        IBlockState state = world.getBlockState(pos);
        state = state.withProperty(BlockExplosive.ROTATION_PROP, facingDirection);
        this.world.setBlockState(pos, state, 2);
    }
}
