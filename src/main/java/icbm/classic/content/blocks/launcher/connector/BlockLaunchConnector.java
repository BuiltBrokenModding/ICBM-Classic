package icbm.classic.content.blocks.launcher.connector;

import com.mojang.serialization.MapCodec;
import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 * <p>
 * PORT: 1.12 computed the connection booleans in getActualState, they are now stored properties updated on
 * placement, on neighbour shape updates and on neighbour block updates.
 */
public class BlockLaunchConnector extends BaseEntityBlock
{
    public static final MapCodec<BlockLaunchConnector> CODEC = simpleCodec(p -> new BlockLaunchConnector());

    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    public BlockLaunchConnector()
    {
        // 1.12 Material.IRON, hardness 10, resistance field 10 (= 2), isOpaqueCube false, cutout (model json render_type)
        // PORT: 1.12 Material.IRON needed a pickaxe to drop -> requiresCorrectToolForDrops (+ mineable/pickaxe tag)
        super(Properties.of().mapColor(MapColor.METAL).strength(10f, 2f).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any()
            .setValue(UP, false).setValue(DOWN, false)
            .setValue(NORTH, false).setValue(EAST, false)
            .setValue(SOUTH, false).setValue(WEST, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "launcher_connector";
    }

    public static BooleanProperty getProperty(Direction side)
    {
        switch (side)
        {
            case UP: return UP;
            case DOWN: return DOWN;
            case NORTH: return NORTH;
            case EAST: return EAST;
            case SOUTH: return SOUTH;
            default: return WEST;
        }
    }

    /** 1.12 getActualState logic */
    protected BlockState getConnectedState(BlockState state, BlockGetter worldIn, BlockPos pos)
    {
        for (Direction side : Direction.values())
        {
            state = state.setValue(getProperty(side), isConnection(worldIn, pos, side));
        }
        return state;
    }

    private boolean isConnection(BlockGetter worldIn, BlockPos selfPos, Direction side) {
        final BlockPos pos = selfPos.relative(side);
        final BlockEntity tile = worldIn.getBlockEntity(pos);
        if(tile != null) {
            // PORT: 1.12 recomputed this every render (getActualState), now the state is stored. A launcher component
            //  placed next to us has no network (so no caps) until its onLoad runs on the next tick, and nothing
            //  refreshes the state afterwards. Any adjacent component always ends up in our network, which in the
            //  1.12 steady state exposes energy + item caps, so count it as connected directly.
            if (tile instanceof ILauncherComponent) {
                return true;
            }
            return getCap(worldIn, tile, Capabilities.EnergyStorage.BLOCK, side.getOpposite()) != null
                || getCap(worldIn, tile, Capabilities.ItemHandler.BLOCK, side.getOpposite()) != null;
        }
        return false;
    }

    @Nullable
    private static <T> T getCap(BlockGetter worldIn, BlockEntity tile, BlockCapability<T, Direction> cap, Direction side)
    {
        if (tile instanceof ICapabilityHolderTile)
        {
            return ((ICapabilityHolderTile) tile).getCapability(cap, side);
        }
        if (worldIn instanceof Level)
        {
            return ((Level) worldIn).getCapability(cap, tile.getBlockPos(), tile.getBlockState(), tile, side);
        }
        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return getConnectedState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos)
    {
        return state.setValue(getProperty(facing), isConnection(level, currentPos, facing));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
    {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        // PORT: launcher components count as connected without checking caps (see isConnection), so a network join
        //  doesn't need a refresh. Other mods' tiles gaining caps without a block update still won't refresh the state
        final BlockState newState = getConnectedState(state, level, pos);
        if (newState != state)
        {
            level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
        }
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
        builder.add(UP, DOWN, NORTH, EAST, WEST, SOUTH);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileLauncherConnector(pos, state);
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
