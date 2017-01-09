package icbm.classic.content.blocks;

import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

//@BlockInfo(tileEntity = "icbm.core.blocks.TileCamouflage")
public class BlockCamouflage extends BlockICBM
{
    public BlockCamouflage()
    {
        super("camouflage", Material.cloth);
        this.setHardness(0.3F);
        this.setResistance(1F);
        this.setStepSound(soundTypeCloth);
    }

    /**
     * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z,
     * side
     */
    @Override
    public IIcon getIcon(IBlockAccess par1IBlockAccess, int x, int y, int z, int side)
    {
        TileEntity t = par1IBlockAccess.getTileEntity(x, y, z);

        if (t != null)
        {
            if (t instanceof TileCamouflage)
            {
                TileCamouflage tileEntity = (TileCamouflage) t;

                if (tileEntity.canRenderSide(ForgeDirection.getOrientation(side)))
                {
                    return Blocks.glass.getBlockTextureFromSide(side);
                }

                Block block = tileEntity.getMimicBlockID();

                if (block != null)
                {
                    try
                    {
                        IIcon blockIcon = block.getIcon(side, tileEntity.getMimicBlockMeta());

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
            }
        }

        return this.blockIcon;
    }

    public boolean onMachineActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        try
        {
            if (par5EntityPlayer.getHeldItem() != null)
            {
                if (par5EntityPlayer.getHeldItem().getItem() instanceof ItemBlock)
                {
                    Block block = Block.getBlockFromItem(par5EntityPlayer.getCurrentEquippedItem().getItem());

                    if (block != null && block != this)
                    {
                        if (block.isNormalCube() && (block.getRenderType() == 0 || block.getRenderType() == 31))
                        {
                            ((TileCamouflage) par1World.getTileEntity(x, y, z)).setMimicBlock(block, par5EntityPlayer.getCurrentEquippedItem().getItemDamage());
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

    public boolean onUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity t = par1World.getTileEntity(x, y, z);

        if (t != null)
        {
            if (t instanceof TileCamouflage)
            {
                ((TileCamouflage) par1World.getTileEntity(x, y, z)).toggleRenderSide(ForgeDirection.getOrientation(side));
                t.markDirty();
            }
        }

        return true;
    }

    public boolean onSneakUseWrench(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int side, float hitX, float hitY, float hitZ)
    {
        TileEntity t = par1World.getTileEntity(x, y, z);

        if (t != null)
        {
            if (t instanceof TileCamouflage)
            {
                ((TileCamouflage) par1World.getTileEntity(x, y, z)).toggleCollision();
            }
        }

        return true;
    }

    /**
     * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color.
     * Note only called when first determining what to render.
     */
    @Override
    public int colorMultiplier(IBlockAccess par1IBlockAccess, int x, int y, int z)
    {
        try
        {
            TileEntity tileEntity = par1IBlockAccess.getTileEntity(x, y, z);

            if (tileEntity instanceof TileCamouflage)
            {
                Block block = ((TileCamouflage) tileEntity).getMimicBlockID();

                if (block != null)
                {
                    return block.colorMultiplier(par1IBlockAccess, x, y, x);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 16777215;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int x, int y, int z)
    {
        TileEntity t = par1World.getTileEntity(x, y, z);

        if (t != null)
        {
            if (t instanceof TileCamouflage)
            {
                if (((TileCamouflage) t).getCanCollide())
                {
                    return super.getCollisionBoundingBoxFromPool(par1World, x, y, z);
                }
            }
        }
        return null;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
        return par1IBlockAccess.getBlock(par2, par3, par4) == this ? false : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
    }
}
