package icbm.classic.content.blocks.launcher.screen;

import com.mojang.serialization.MapCodec;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.InventoryUtility;
import icbm.classic.lib.capability.gps.GPSDataHelpers;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class BlockLaunchScreen extends BlockICBM
{
    public static final MapCodec<BlockLaunchScreen> CODEC = simpleCodec(p -> new BlockLaunchScreen());

    public BlockLaunchScreen()
    {
        super("launcherscreen");
        // PORT: 1.12 relied on dropInventory, but the screen only exposes the network's item handler (cleared by
        //  onTileRemoved before BlockICBM drops), so its own battery slot was deleted. Dropped in onRemove instead.
        this.dropInventory = false;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!world.isClientSide())
        {
            final BlockEntity tileEntity = world.getBlockEntity(pos);
            if(tileEntity instanceof TileLauncherScreen)
            {
                final TileLauncherScreen screen = (TileLauncherScreen) tileEntity;
                final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
                if (GPSDataHelpers.handlePlayerInteraction(gpsData, player, screen::setTarget))
                {
                    return ItemInteractionResult.SUCCESS;
                }
                else if(stack.getItem() == Items.STONE_AXE) {
                    final LauncherNetwork network = screen.getNetworkNode().getNetwork();
                    player.sendSystemMessage(Component.literal("Network: " + network));
                    // PORT: 1.12 crashed here (NPE) when the component had no network yet, ex. a single block placed alone
                    player.sendSystemMessage(Component.literal("L: " + (network != null ? network.getLaunchers().size() : 0)));
                }
                else
                {
                    screen.openGui(player);
                }
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TileLauncherScreen(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_LAUNCHER_SCREEN.get(), (l, p, s, tile) -> tile.update());
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!state.is(newState.getBlock()))
        {
            final BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileLauncherScreen screen)
            {
                // Drop the screen's own slots (battery), not the connected launchers' items
                for (int slot = 0; slot < screen.inventory.getSlots(); slot++)
                {
                    final ItemStack slotStack = screen.inventory.getStackInSlot(slot);
                    if (!slotStack.isEmpty())
                    {
                        screen.inventory.setStackInSlot(slot, ItemStack.EMPTY);
                        InventoryUtility.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, slotStack, 0, 0);
                    }
                }
            }
            if (tile instanceof ILauncherComponent)
            {
                ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
            }
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
// PORTED
