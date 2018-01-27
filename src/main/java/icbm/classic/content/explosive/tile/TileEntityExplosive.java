package icbm.classic.content.explosive.tile;

import com.builtbroken.mc.api.data.IPacket;
import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.data.Direction;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.items.ItemRemoteDetonator;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityExplosive extends TileEntity implements IPacketIDReceiver, IRotatable
{
    /** Is the tile currently exploding */
    public boolean exploding = false;
    /** Explosive ID */
    public Explosives explosive = Explosives.CONDENSED;
    /** Extra explosive data */
    public NBTTagCompound nbtData = new NBTTagCompound();

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.explosive = Explosives.get(par1NBTTagCompound.getInteger("explosiveID"));
        this.nbtData = par1NBTTagCompound.getCompoundTag("data");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setInteger("explosiveID", this.explosive.ordinal());
        par1NBTTagCompound.setTag("data", this.nbtData);
        return super.writeToNBT(par1NBTTagCompound);
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket packet)
    {
        if (id == 1)
        {
            explosive = Explosives.get(data.readInt());
            world.markBlockRangeForRenderUpdate(pos, pos);
            return true;
        }
        else if (id == 2 && !this.world.isRemote)
        {
            // Packet explode command
            if (player.inventory.getCurrentItem().getItem() instanceof ItemRemoteDetonator)
            {
                ItemStack itemStack = player.inventory.getCurrentItem();
                BlockExplosive.triggerExplosive(this.world, pos, this.explosive, 0);
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
    public Direction getDirection()
    {
        return Direction.getOrientation(this.getBlockMetadata());
    }

    @Override
    public void setDirection(Direction facingDirection)
    {
        this.world.setBlockState(pos, getBlockType().getDefaultState().withProperty(BlockExplosive.ROTATION_PROP, facingDirection.getEnumFacing()), 2);
    }

    public Explosive getExplosiveType()
    {
        return this.explosive.handler;
    }

    public NBTTagCompound getTagCompound()
    {
        return this.nbtData;
    }
}
