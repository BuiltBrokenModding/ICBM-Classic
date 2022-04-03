package icbm.classic.content.blocks;

import icbm.classic.ICBMConstants;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockGlassPressurePlate extends BlockPressurePlate
{
    public BlockGlassPressurePlate()
    {
        super(Material.GLASS, Sensitivity.EVERYTHING);
        this.setTickRandomly(true);
        this.setResistance(1F);
        this.setHardness(0.3F);
        this.setSoundType(SoundType.GLASS);
        this.setRegistryName(ICBMConstants.PREFIX + "glassPressurePlate");
        this.setUnlocalizedName(ICBMConstants.PREFIX + "glassPressurePlate");
        this.setCreativeTab(CreativeTabs.REDSTONE);
        this.setDefaultState(getDefaultState().withProperty(POWERED, false));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }
}
