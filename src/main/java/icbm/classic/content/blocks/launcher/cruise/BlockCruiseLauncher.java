package icbm.classic.content.blocks.launcher.cruise;

import com.mojang.serialization.MapCodec;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.gps.GPSDataHelpers;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/15/2018.
 */
public class BlockCruiseLauncher extends BlockICBM
{
    public static final MapCodec<BlockCruiseLauncher> CODEC = simpleCodec(p -> new BlockCruiseLauncher());

    public BlockCruiseLauncher()
    {
        // 1.12 hardness 10, resistance field 10 (= 2), isOpaqueCube/isFullCube false
        super("cruiseLauncher", machineProperties().noOcclusion());
        this.dropInventory = true;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @Nullable Direction side)
    {
        return true;
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
        return new TileCruiseLauncher(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_CRUISE_LAUNCHER.get(), (l, p, s, tile) -> tile.update());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!world.isClientSide())
        {
            final BlockEntity tileEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof TileCruiseLauncher)
            {
                final TileCruiseLauncher launcher = (TileCruiseLauncher) tileEntity;
                final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
                if (!GPSDataHelpers.handlePlayerInteraction(gpsData, player, launcher::setTarget))
                {
                    launcher.openGui(player);
                }
            }
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
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
// PORTED
