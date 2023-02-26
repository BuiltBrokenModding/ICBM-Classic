package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.reg.BlockReg;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class BlockLaunchFrame extends BlockICBM
{
    public static final PropertyFrameState FRAME_STATE = new PropertyFrameState();

    public BlockLaunchFrame()
    {
        super("launcherframe");
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        final boolean frameAbove = isConnection(worldIn, pos.offset(EnumFacing.UP));
        final boolean frameUnder = isConnection(worldIn, pos.offset(EnumFacing.DOWN));
        if(frameAbove && frameUnder) {
            return state.withProperty(FRAME_STATE, EnumFrameState.MIDDLE);
        }
        else if(frameUnder) {
            return state.withProperty(FRAME_STATE, EnumFrameState.TOP);
        }
        else if(frameAbove) {
            return state.withProperty(FRAME_STATE, EnumFrameState.BOTTOM);
        }
        return state.withProperty(FRAME_STATE, EnumFrameState.MIDDLE);
    }

    private boolean isConnection(IBlockAccess worldIn, BlockPos pos) {
        final IBlockState state = worldIn.getBlockState(pos);
        return state.getBlock() == this || state.getBlock() == BlockReg.blockLaunchScreen;
    }

    @Deprecated
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ROTATION_PROP, FRAME_STATE);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileLauncherFrame();
    }
}
