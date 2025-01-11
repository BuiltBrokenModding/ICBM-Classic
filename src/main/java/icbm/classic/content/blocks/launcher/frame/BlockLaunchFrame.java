package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.TileReg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class BlockLaunchFrame extends Block {
    public BlockLaunchFrame(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(BlockStateProperties.HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileLauncherFrame) {
            if (playerIn.getHeldItem(hand).getItem() == Items.STONE_AXE) {
                if (!worldIn.isRemote) {
                    final LauncherNetwork network = ((TileLauncherFrame) tile).getNetworkNode().getNetwork();
                    playerIn.sendMessage(new StringTextComponent("Network: " + network));
                    playerIn.sendMessage(new StringTextComponent("L: " + network.getLaunchers().size()));
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileReg.LAUNCHER_FRAME.get().create();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof ILauncherComponent) {
            ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
