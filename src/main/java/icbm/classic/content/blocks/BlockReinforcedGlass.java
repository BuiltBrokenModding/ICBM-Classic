package icbm.classic.content.blocks;

import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockReinforcedGlass extends Block
{
    public BlockReinforcedGlass()
    {
        super(Material.GLASS);
        this.setRegistryName(ICBMClassic.PREFIX + "reinforcedGlass");
        this.setUnlocalizedName(ICBMClassic.PREFIX + "reinforcedGlass");
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setHardness(10);
        this.setResistance(48);
    }

    @Override
    protected boolean canSilkHarvest()
    {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
        Block block = iblockstate.getBlock();

        if (blockState != iblockstate)
        {
            return true;
        }

        if (block == this)
        {
            return false;
        }

        return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }
}
