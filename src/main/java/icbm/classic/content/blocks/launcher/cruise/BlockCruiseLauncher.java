package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.reg.TileReg;
import icbm.classic.lib.capability.gps.GPSDataHelpers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/15/2018.
 */
public class BlockCruiseLauncher extends Block {
    private static final VoxelShape SHAPE = VoxelShapes.combineAndSimplify(
        Block.makeCuboidShape(0, 0, 0, 16, 4, 16),
        Block.makeCuboidShape(2, 4, 2, 14, 16, 14),
        IBooleanFunction.OR
    );

    public BlockCruiseLauncher(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
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
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileReg.LAUNCHER_CRUISE.get().create();
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            final TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileCruiseLauncher) {
                final TileCruiseLauncher launcher = (TileCruiseLauncher) tileEntity;
                final ItemStack stack = player.getHeldItem(hand);
                if (stack.getCapability(ICBMClassicAPI.GPS_CAPABILITY).isPresent()) {
                    final IGPSData gpsData = stack.getCapability(ICBMClassicAPI.GPS_CAPABILITY).orElseThrow(IllegalStateException::new);
                    if (!GPSDataHelpers.handlePlayerInteraction(gpsData, player, launcher::setTarget)) {
                        // TODO player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ILauncherComponent) {
            ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
        }
        super.onReplaced(state, world, pos, newState, isMoving);
        //TODO drop inventory
    }
}
