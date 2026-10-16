package icbm.classic.content.blocks.launcher.base;

import com.mojang.serialization.MapCodec;
import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.InventoryUtility;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/8/2018.
 */
public class BlockLauncherBase extends BaseEntityBlock
{
    public static final MapCodec<BlockLauncherBase> CODEC = simpleCodec(p -> new BlockLauncherBase());

    public static final DirectionProperty ROTATION_PROP = DirectionProperty.create("facing");

    public BlockLauncherBase()
    {
        // 1.12 Material.IRON, hardness 10, resistance field 10 (= 2)
        // PORT: 1.12 isOpaqueCube false (prevents missile lighting issues) -> noOcclusion
        // PORT: 1.12 Material.IRON needed a pickaxe to drop -> requiresCorrectToolForDrops (+ mineable/pickaxe tag)
        super(Properties.of().mapColor(MapColor.METAL).strength(10f, 2f).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops());
        // PORT: 1.12 meta 0 (old tiles without rotation) defaulted to UP
        registerDefaultState(stateDefinition.any().setValue(ROTATION_PROP, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "launcherbase";
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit)
    {
        final BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof TileLauncherBase && !worldIn.isClientSide())
        {
            if (heldItem.getItem() == Items.STONE_AXE)
            {
                final LauncherNetwork network = ((TileLauncherBase) tile).getNetworkNode().getNetwork();
                playerIn.sendSystemMessage(Component.literal("Network: " + network));
                // PORT: 1.12 crashed here (NPE) when the component had no network yet, ex. a single block placed alone
                playerIn.sendSystemMessage(Component.literal("L: " + (network != null ? network.getLaunchers().size() : 0)));
                return ItemInteractionResult.SUCCESS;
            }
            if (!((TileLauncherBase) tile).tryInsertMissile(playerIn, hand, heldItem))
            {
                ((TileLauncherBase) tile).openGui(playerIn);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SUCCESS;
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
            InventoryUtility.dropInventory(world, pos);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileLauncherBase(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_LAUNCHER_BASE.get(), (l, p, s, tile) -> tile.update());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ROTATION_PROP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(ROTATION_PROP, context.getClickedFace());
    }
}
// PORTED
