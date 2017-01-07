package icbm.classic.content.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.TabICBM;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

public class BlockReinforcedGlass extends BlockICBM
{
    public BlockReinforcedGlass()
    {
        super("glassReinforced", Material.glass);
        this.setResistance(48);
        this.setCreativeTab(TabICBM.INSTANCE);
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
