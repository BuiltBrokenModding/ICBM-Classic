package icbm.classic.content.blocks.launcher.base;

import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import icbm.classic.content.reg.TileReg;
import icbm.classic.lib.InventoryUtility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/8/2018.
 */
public class BlockLauncherBase extends ContainerBlock
{
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public BlockLauncherBase(Properties properties)
    {
        super(properties);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Deprecated
    public boolean isOpaqueCube(BlockState state)
    {
        // Needed to prevent render lighting issues for missiles
        return false;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileLauncherBase && !worldIn.isRemote)
        {
            if(playerIn.getHeldItem(hand).getItem() == Items.STONE_AXE) {
                    final LauncherNetwork network = ((TileLauncherBase) tile).getNetworkNode().getNetwork();
                    playerIn.sendMessage(new StringTextComponent("Network: " + network));
                    playerIn.sendMessage(new StringTextComponent("L: " + network.getLaunchers().size()));
                return true;
            }
            if(!((TileLauncherBase) tile).tryInsertMissile(playerIn, hand, playerIn.getHeldItem(hand))) {
                //playerIn.openGui(ICBMClassic.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ()); TODO
            }
            return true;
        }
        return true;
    }

    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileLauncherBase)
            {
                ((TileLauncherBase) tile).getNetworkNode().onTileRemoved();
                InventoryUtility.dropInventory(world, pos);
            }

            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader reader)
    {
        return TileReg.LAUNCHER_BASE.get().create();
    }


    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        return getDefaultState().with(FACING, context.getFace());
    }
}
