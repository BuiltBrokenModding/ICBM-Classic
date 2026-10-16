package icbm.classic.content.blocks.radarstation;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 * <p>
 * PORT: RADAR_STATE was computed in getActualState, it is now a stored property set by {@link TileRadarStation}.
 */
public class BlockRadarStation extends BlockICBM
{
    public static final MapCodec<BlockRadarStation> CODEC = simpleCodec(p -> new BlockRadarStation());

    public static final BooleanProperty REDSTONE_PROPERTY = BooleanProperty.create("redstone");
    public static final PropertyRadarState RADAR_STATE = new PropertyRadarState();

    public BlockRadarStation()
    {
        super("radarStation"); //TODO rename to "radar_screen"
        this.dropInventory = true;
        registerDefaultState(defaultBlockState().setValue(REDSTONE_PROPERTY, false).setValue(RADAR_STATE, EnumRadarState.OFF));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(REDSTONE_PROPERTY, RADAR_STATE);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side)
    {
        final BlockEntity tileEntity = world.getBlockEntity(pos);
        if(tileEntity instanceof TileRadarStation) {
            return ((TileRadarStation) tileEntity).isOutputRedstone();
        }
        return false;
    }

    @Override
    protected boolean isSignalSource(BlockState state)
    {
        return state.getValue(REDSTONE_PROPERTY);
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
    {
        return getDirectSignal(blockState, blockAccess, pos, side);
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
    {
        final BlockEntity tile = blockAccess.getBlockEntity(pos);
        if (tile instanceof TileRadarStation)
        {
            return ((TileRadarStation) tile).getStrongRedstonePower(side);
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!world.isClientSide())
        {
            final BlockEntity tile = world.getBlockEntity(pos);
            if (stack.getItem() == Items.REDSTONE) //TODO move to UI
            {
                if (tile instanceof TileRadarStation)
                {
                    ((TileRadarStation) tile).setOutputRedstone(!((TileRadarStation) tile).isOutputRedstone());
                    player.sendSystemMessage(Component.translatable(((TileRadarStation) tile).isOutputRedstone() ? "message.radar.redstone.on" : "message.radar.redstone.off"));
                }
                else
                {
                    player.sendSystemMessage(Component.literal("§cUnexpected error: Couldn't access radar station tile"));
                }
            }
            else if (tile instanceof TileRadarStation)
            {
                ((TileRadarStation) tile).openGui(player);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileRadarStation(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_RADAR_STATION.get(), (l, p, s, tile) -> tile.update());
    }
}
// PORTED
