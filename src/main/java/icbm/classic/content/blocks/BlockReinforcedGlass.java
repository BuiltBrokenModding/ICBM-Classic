package icbm.classic.content.blocks;

import net.minecraftforge.fml.relauncher.Side;import net.minecraftforge.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class BlockReinforcedGlass extends BlockICBM
{

    public static IIcon[] textures = new IIcon[47];
    public static int[] textureRefByID = {0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 16, 16, 20, 20, 16, 16, 28, 28, 21, 21, 46, 42, 21, 21, 43, 38, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 16, 16, 20, 20, 16, 16, 28, 28, 25, 25, 45, 37, 25, 25, 40, 32, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 0, 0, 6, 6, 0, 0, 6, 6, 3, 3, 19, 15, 3, 3, 19, 15, 1, 1, 18, 18, 1, 1, 13, 13, 2, 2, 23, 31, 2, 2, 27, 14, 4, 4, 5, 5, 4, 4, 5, 5, 17, 17, 22, 26, 17, 17, 22, 26, 7, 7, 24, 24, 7, 7, 10, 10, 29, 29, 44, 41, 29, 29, 39, 33, 4, 4, 5, 5, 4, 4, 5, 5, 9, 9, 30, 12, 9, 9, 30, 12, 7, 7, 24, 24, 7, 7, 10, 10, 8, 8, 36, 35, 8, 8, 34, 11};


    public BlockReinforcedGlass()
    {
        super("reinforcedGlass", Material.glass);
        this.setHardness(10);
        this.setResistance(48);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegistry)
    {
        for (int i = 0; i < 47; i++)
        {
            textures[i] = iconRegistry.registerIcon(ICBMClassic.PREFIX + "reinforced_glass/reinforced_glass_" + (i + 1));
        }
    }

    @Override
    public IIcon getIcon(int side, int meta)
    {
        return textures[0];
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
    {
        boolean[] bitMatrix = new boolean[8];

        if (side == 0 || side == 1)
        {
            bitMatrix[0] = world.getBlock(x - 1, y, z - 1) == this;
            bitMatrix[1] = world.getBlock(x, y, z - 1) == this;
            bitMatrix[2] = world.getBlock(x + 1, y, z - 1) == this;
            bitMatrix[3] = world.getBlock(x - 1, y, z) == this;
            bitMatrix[4] = world.getBlock(x + 1, y, z) == this;
            bitMatrix[5] = world.getBlock(x - 1, y, z + 1) == this;
            bitMatrix[6] = world.getBlock(x, y, z + 1) == this;
            bitMatrix[7] = world.getBlock(x + 1, y, z + 1) == this;
        }
        else if (side == 2 || side == 3)
        {
            bitMatrix[0] = world.getBlock(x + (side == 2 ? 1 : -1), y + 1, z) == this;
            bitMatrix[1] = world.getBlock(x, y + 1, z) == this;
            bitMatrix[2] = world.getBlock(x + (side == 3 ? 1 : -1), y + 1, z) == this;
            bitMatrix[3] = world.getBlock(x + (side == 2 ? 1 : -1), y, z) == this;
            bitMatrix[4] = world.getBlock(x + (side == 3 ? 1 : -1), y, z) == this;
            bitMatrix[5] = world.getBlock(x + (side == 2 ? 1 : -1), y - 1, z) == this;
            bitMatrix[6] = world.getBlock(x, y - 1, z) == this;
            bitMatrix[7] = world.getBlock(x + (side == 3 ? 1 : -1), y - 1, z) == this;
        }
        else if (side == 4 || side == 5)
        {
            bitMatrix[0] = world.getBlock(x, y + 1, z + (side == 5 ? 1 : -1)) == this;
            bitMatrix[1] = world.getBlock(x, y + 1, z) == this;
            bitMatrix[2] = world.getBlock(x, y + 1, z + (side == 4 ? 1 : -1)) == this;
            bitMatrix[3] = world.getBlock(x, y, z + (side == 5 ? 1 : -1)) == this;
            bitMatrix[4] = world.getBlock(x, y, z + (side == 4 ? 1 : -1)) == this;
            bitMatrix[5] = world.getBlock(x, y - 1, z + (side == 5 ? 1 : -1)) == this;
            bitMatrix[6] = world.getBlock(x, y - 1, z) == this;
            bitMatrix[7] = world.getBlock(x, y - 1, z + (side == 4 ? 1 : -1)) == this;
        }
        int idBuilder = 0;

        for (int i = 0; i <= 7; i++)
        {
            idBuilder = idBuilder + (bitMatrix[i] ? (i == 0 ? 1 : (i == 1 ? 2 : (i == 2 ? 4 : (i == 3 ? 8 : (i == 4 ? 16 : (i == 5 ? 32 : (i == 6 ? 64 : 128))))))) : 0);
        }

        return idBuilder > 255 || idBuilder < 0 ? textures[0] : textures[textureRefByID[idBuilder]];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderBlockPass()
    {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int par2, int par3, int par4, int par5)
    {
        return world.getBlock(par2, par3, par4) == this ? false : super.shouldSideBeRendered(world, par2, par3, par4, par5);
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
    protected boolean canSilkHarvest()
    {
        return true;
    }

    @Override
    public boolean hasTileEntity(int metadata)
    {
        return false;
    }
}
