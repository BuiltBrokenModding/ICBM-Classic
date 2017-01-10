package icbm.classic.content.explosive.tile;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.items.ItemRemoteDetonator;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import resonant.api.explosion.IExplosiveContainer;

public class TileEntityExplosive extends TileEntity implements IExplosiveContainer, IPacketReceiver, IRotatable
{
    /** Is the tile currently exploding */
    public boolean exploding = false;
    /** Explosive ID */
    public Explosives explosive = null;
    /** Extra explosive data */
    public NBTTagCompound nbtData = new NBTTagCompound();

    @Override
    public boolean canUpdate()
    {
        return false;
    }

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
    public void writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("explosiveID", this.explosive.ordinal());
        par1NBTTagCompound.setTag("data", this.nbtData);
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType packet)
    {
        try
        {
            final byte ID = data.readByte();

            if (ID == 1)
            {
                explosive = Explosives.get(data.readInt());
                worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
            }
            else if (ID == 2 && !this.worldObj.isRemote)
            {
                // Packet explode command
                if (player.inventory.getCurrentItem().getItem() instanceof ItemRemoteDetonator)
                {
                    ItemStack itemStack = player.inventory.getCurrentItem();
                    BlockExplosive.yinZha(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.explosive, 0);
                    ((ItemRemoteDetonator) ICBMClassic.itemRemoteDetonator).discharge(itemStack, ItemRemoteDetonator.ENERGY, true);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        return Engine.instance.packetHandler.toMCPacket(new PacketTile(this, (byte) 1, this.explosive.ordinal()));
    }

    @Override
    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(this.getBlockMetadata());
    }

    @Override
    public void setDirection(ForgeDirection facingDirection)
    {
        this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, facingDirection.ordinal(), 2);
    }

    @Override
    public Explosive getExplosiveType()
    {
        return this.explosive.handler;
    }

    @Override
    public NBTTagCompound getTagCompound()
    {
        return this.nbtData;
    }
}
