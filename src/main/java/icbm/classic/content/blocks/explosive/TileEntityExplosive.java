package icbm.classic.content.blocks.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.content.items.ItemRemoteDetonator;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityExplosive extends TileEntity implements IPacketIDReceiver, IRotatable
{
    /** Is the tile currently exploding */
    public boolean exploding = false;
    /** Explosive ID */
    public IExplosiveData explosive = null;
    /** Extra explosive data */
    public NBTTagCompound nbtData = new NBTTagCompound();

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.explosive = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(par1NBTTagCompound.getInteger("explosiveID"));
        this.nbtData = par1NBTTagCompound.getCompoundTag("data");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        if(explosive != null)
        {
            par1NBTTagCompound.setInteger("explosiveID", this.explosive.getRegistryID());
            par1NBTTagCompound.setTag("data", this.nbtData);
        }
        return super.writeToNBT(par1NBTTagCompound);
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket packet)
    {
        if (id == 1)
        {
            explosive = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(data.readInt());
            world.markBlockRangeForRenderUpdate(pos, pos);
            return true;
        }
        else if (id == 2 && !this.world.isRemote)
        {
            // Packet explode command
            if (player.inventory.getCurrentItem().getItem() instanceof ItemRemoteDetonator)
            {
                ItemStack itemStack = player.inventory.getCurrentItem();
                BlockExplosive.triggerExplosive(this.world, pos, this.explosive.getRegistryID(), 0);
                ((ItemRemoteDetonator) ICBMClassic.itemRemoteDetonator).discharge(itemStack, ItemRemoteDetonator.ENERGY, true);
            }
            return true;
        }
        return false;
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
        this.world.setBlockState(pos, getBlockType().getDefaultState().withProperty(BlockExplosive.ROTATION_PROP, facingDirection), 2);
    }
}
