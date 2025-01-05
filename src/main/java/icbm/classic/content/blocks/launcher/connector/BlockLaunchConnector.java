package icbm.classic.content.blocks.launcher.connector;

import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class BlockLaunchConnector extends Block
{
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    public BlockLaunchConnector(Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockState getExtendedState(BlockState state, IBlockReader worldIn, BlockPos pos)
    {
        final boolean upConnection = isConnection(worldIn, pos, Direction.UP);
        final boolean downConnection = isConnection(worldIn, pos, Direction.DOWN);
        final boolean northConnection = isConnection(worldIn, pos, Direction.NORTH);
        final boolean eastConnection = isConnection(worldIn, pos, Direction.EAST);
        final boolean southConnection = isConnection(worldIn, pos, Direction.SOUTH);
        final boolean westConnection = isConnection(worldIn, pos, Direction.WEST);

        return state
            .with(UP, upConnection)
            .with(DOWN, downConnection)
            .with(NORTH, northConnection)
            .with(EAST, eastConnection)
            .with(SOUTH, southConnection)
            .with(WEST, westConnection);
    }

    private boolean isConnection(IBlockReader worldIn, BlockPos selfPos, Direction side) {
        final BlockPos pos = selfPos.offset(side);
        final TileEntity tile = worldIn.getTileEntity(pos);
        if(tile != null) {
            return tile.getCapability(CapabilityEnergy.ENERGY, side.getOpposite()).isPresent()
                || tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite()).isPresent();
        }
        return false;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof icbm.classic.content.blocks.launcher.frame.TileLauncherFrame)
        {
            if(playerIn.getHeldItem(hand).getItem() == Items.STONE_AXE) {
                if(!worldIn.isRemote) {
                    final LauncherNetwork network = ((icbm.classic.content.blocks.launcher.frame.TileLauncherFrame) tile).getNetworkNode().getNetwork();
                    playerIn.sendMessage(new StringTextComponent("Network: " + network));
                    playerIn.sendMessage(new StringTextComponent("L: " + network.getLaunchers().size()));
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public boolean isOpaqueCube(BlockState state)
    {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, EAST, WEST, SOUTH);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileLauncherConnector();
    }

    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ILauncherComponent)
        {
            ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }
}
