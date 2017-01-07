package icbm.classic.content.blocks;

import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileCamouflage extends Tile implements IPacketReceiver
{
    // The block Id this block is trying to mimick
    private Block blockToMimic = null;
    private int blockMetaToMic = 0;
    private boolean isSolid = true;

    /** Bitmask **/
    private byte renderSides = 0;

    @Override
    public Tile newTile()
    {
        return new TileCamouflage();
    }

    @Override
    public boolean canUpdate()
    {
        return false;
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType packet)
    {
        String blockName = ByteBufUtils.readUTF8String(data);
        this.setMimicBlock(blockName.isEmpty() ? null : (Block) Block.blockRegistry.getObject(blockName), data.readInt());
        this.renderSides = data.readByte();
        this.isSolid = data.readBoolean();
        markDirty();
    }

    @Override
    public PacketTile getDescPacket()
    {
        String blockName = "";
        if (blockToMimic != null)
        {
            blockName = Block.blockRegistry.getNameForObject(blockToMimic);
            if (blockName == null)
            {
                blockName = "";
            }
        }
        return new PacketTile(this, blockName, this.blockMetaToMic, this.renderSides, this.isSolid);
    }

    public boolean getCanCollide()
    {
        return this.isSolid;
    }

    public void setCanCollide(boolean isSolid)
    {
        this.isSolid = isSolid;

        if (!this.worldObj.isRemote)
        {
            sendDescPacket();
        }
    }

    public void toggleCollision()
    {
        this.setCanCollide(!this.isSolid);
    }

    public Block getMimicBlockID()
    {
        return this.blockToMimic;
    }

    public int getMimicBlockMeta()
    {
        return this.blockMetaToMic;
    }

    public void setMimicBlock(Block block, int metadata)
    {
        if (this.blockToMimic != block || this.blockMetaToMic != metadata)
        {
            this.blockToMimic = block;
            this.blockMetaToMic = Math.max(metadata, 0);

            if (!this.worldObj.isRemote)
            {
                sendDescPacket();
            }
        }
    }

    public boolean canRenderSide(ForgeDirection direction)
    {
        return (renderSides & (1 << direction.ordinal())) != 0;
    }

    public void setRenderSide(ForgeDirection direction, boolean isClear)
    {
        if (isClear)
        {
            renderSides = (byte) (renderSides | (1 << direction.ordinal()));
        }
        else
        {
            renderSides = (byte) (renderSides & ~(1 << direction.ordinal()));

        }

        if (!this.worldObj.isRemote)
        {
            sendDescPacket();
        }
    }

    public void toggleRenderSide(ForgeDirection direction)
    {
        this.setRenderSide(direction, !canRenderSide(direction));
    }

    public void setRenderSide(boolean isClear)
    {
        if (isClear)
        {
            this.renderSides = 63;
        }
        else
        {
            this.renderSides = 0;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        this.blockToMimic = (Block) Block.blockRegistry.getObject(nbt.getString("blockToMimic"));
        this.blockMetaToMic = nbt.getInteger("metaToMimic");
        this.renderSides = nbt.getByte("renderSides");
        this.isSolid = nbt.getBoolean("isSold");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (blockToMimic != null)
        {
            nbt.setString("blockToMic", Block.blockRegistry.getNameForObject(this.blockToMimic));
            nbt.setInteger("metaToMimic", this.blockMetaToMic);
        }
        nbt.setByte("renderSides", renderSides);
        nbt.setBoolean("isSold", this.isSolid);
    }
}
