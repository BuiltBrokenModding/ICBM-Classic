package icbm.classic.content.blocks.launcher.frame;

import com.mojang.serialization.MapCodec;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 * <p>
 * PORT: 1.12 computed FRAME_STATE in getActualState, it is now a stored property updated on placement and
 * whenever the block above or below changes.
 */
public class BlockLaunchFrame extends BlockICBM
{
    public static final MapCodec<BlockLaunchFrame> CODEC = simpleCodec(p -> new BlockLaunchFrame());

    public static final PropertyFrameState FRAME_STATE = new PropertyFrameState();

    public BlockLaunchFrame()
    {
        // 1.12 isOpaqueCube false, cutout layer (render_type in the model json)
        super("launcherframe", machineProperties().noOcclusion());
        registerDefaultState(defaultBlockState().setValue(FRAME_STATE, EnumFrameState.MIDDLE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    /** 1.12 getActualState logic */
    protected BlockState getConnectedState(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        final boolean frameAbove = isConnection(worldIn, pos.relative(Direction.UP));
        final boolean frameUnder = isConnection(worldIn, pos.relative(Direction.DOWN));
        if(frameAbove && frameUnder) {
            return state.setValue(FRAME_STATE, EnumFrameState.MIDDLE);
        }
        else if(frameUnder) {
            return state.setValue(FRAME_STATE, EnumFrameState.TOP);
        }
        else if(frameAbove) {
            return state.setValue(FRAME_STATE, EnumFrameState.BOTTOM);
        }
        return state.setValue(FRAME_STATE, EnumFrameState.MIDDLE);
    }

    private boolean isConnection(BlockGetter worldIn, BlockPos pos) {
        final BlockState state = worldIn.getBlockState(pos);
        return state.getBlock() == this || state.getBlock() == BlockReg.blockLaunchScreen.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return getConnectedState(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        if (facing.getAxis() == Direction.Axis.Y)
        {
            return getConnectedState(state, level, currentPos);
        }
        return state;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit)
    {
        final BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof TileLauncherFrame)
        {
            if(heldItem.getItem() == Items.STONE_AXE) {
                if(!worldIn.isClientSide()) {
                    final LauncherNetwork network = ((TileLauncherFrame) tile).getNetworkNode().getNetwork();
                    playerIn.sendSystemMessage(Component.literal("Network: " + network));
                    // PORT: 1.12 crashed here (NPE) when the component had no network yet, ex. a single block placed alone
                    playerIn.sendSystemMessage(Component.literal("L: " + (network != null ? network.getLaunchers().size() : 0)));
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FRAME_STATE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileLauncherFrame(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock()))
        {
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof ILauncherComponent)
            {
                ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
// PORTED
