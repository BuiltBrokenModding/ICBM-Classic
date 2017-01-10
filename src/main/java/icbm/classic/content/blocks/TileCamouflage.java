package icbm.classic.content.blocks;

import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class TileCamouflage extends Tile implements IPacketReceiver
{
    @SideOnly(Side.CLIENT)
    public static IIcon icon;

    // The block Id this block is trying to mimick
    private Block _blockToMimic = null;
    private int blockMetaToMic = 0;
    private boolean isSolid = true;

    /** Bitmask **/
    private byte renderSides = 0;

    public TileCamouflage()
    {
        super("camouflage", Material.grass);
    }

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
        if (getMimicBlock() != null)
        {
            blockName = Block.blockRegistry.getNameForObject(getMimicBlock());
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

    public Block getMimicBlock()
    {
        return this._blockToMimic;
    }

    public int getMimicBlockMeta()
    {
        return this.blockMetaToMic;
    }

    public void setMimicBlock(Block block, int metadata)
    {
        if (this.getMimicBlock() != block || this.blockMetaToMic != metadata)
        {
            this._blockToMimic = block;
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

        this._blockToMimic = (Block) Block.blockRegistry.getObject(nbt.getString("blockToMimic"));
        this.blockMetaToMic = nbt.getInteger("metaToMimic");
        this.renderSides = nbt.getByte("renderSides");
        this.isSolid = nbt.getBoolean("isSold");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        if (getMimicBlock() != null)
        {
            nbt.setString("blockToMic", Block.blockRegistry.getNameForObject(this._blockToMimic));
            nbt.setInteger("metaToMimic", this.blockMetaToMic);
        }
        nbt.setByte("renderSides", renderSides);
        nbt.setBoolean("isSold", this.isSolid);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side)
    {
        if (getMimicBlock() != null)
        {
            try
            {
                IIcon blockIcon = getMimicBlock().getIcon(side, getMimicBlockMeta());

                if (blockIcon != null)
                {
                    return blockIcon;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon()
    {
        return icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        icon = iconRegister.registerIcon(ICBMClassic.PREFIX + "camouflage");
    }

    @Override
    protected boolean onPlayerRightClick(EntityPlayer par5EntityPlayer, int side, Pos hit)
    {
        try
        {
            if (par5EntityPlayer.getHeldItem() != null)
            {
                if (par5EntityPlayer.getHeldItem().getItem() instanceof ItemBlock)
                {
                    Block block = Block.getBlockFromItem(par5EntityPlayer.getCurrentEquippedItem().getItem());

                    if (block != null && block != getBlockType())
                    {
                        if (block.isNormalCube() && (block.getRenderType() == 0 || block.getRenderType() == 31))
                        {
                            setMimicBlock(block, par5EntityPlayer.getCurrentEquippedItem().getItemDamage());
                            return true;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean onPlayerRightClickWrench(EntityPlayer player, int side, Pos hit)
    {
        if (player.isSneaking())
        {
            toggleCollision();
        }
        else
        {
            toggleRenderSide(ForgeDirection.getOrientation(side));
            markDirty();
        }
        return true;
    }

    /**
     * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color.
     * Note only called when first determining what to render.
     */
    @Override
    public int getColorMultiplier()
    {
        try
        {
            if (getMimicBlock() != null)
            {
                return getMimicBlock().colorMultiplier(world(), xi(), yi(), xi());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 16777215;
    }

    @Override
    public Cube getCollisionBounds()
    {
        if (getCanCollide())
        {
            return super.getCollisionBounds();
        }
        return null;
    }

    @Override
    public boolean shouldSideBeRendered(int side)
    {
        return true;
    }
}
