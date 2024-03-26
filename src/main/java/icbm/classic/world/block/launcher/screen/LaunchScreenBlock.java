package icbm.classic.world.block.launcher.screen;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.capability.gps.GPSDataHelpers;
import icbm.classic.prefab.tile.IcbmBlock;
import icbm.classic.world.block.launcher.network.ILauncherComponent;
import icbm.classic.world.block.launcher.network.LauncherNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.init.Items;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2018.
 */
public class LaunchScreenBlock extends IcbmBlock {
    public LaunchScreenBlock(BlockBehaviour.Properties properties) {
        super(properties, "launcherscreen");
        this.dropInventory = true;
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide()) {
            final BlockEntity blockEntityEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof TileLauncherScreen) {
                final TileLauncherScreen screen = (TileLauncherScreen) tileEntity;
                final ItemStack stack = player.getHeldItem(hand);
                final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
                if (GPSDataHelpers.handlePlayerInteraction(gpsData, player, screen::setTarget)) {
                    return true;
                } else if (stack.getItem() == Items.STONE_AXE) {
                    final LauncherNetwork network = screen.getNetworkNode().getNetwork();
                    player.sendMessage(new TextComponentString("Network: " + network));
                    player.sendMessage(new TextComponentString("L: " + network.getLaunchers().size()));
                } else {
                    player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createNewBlockEntity(Level levelIn, int meta) {
        return new TileLauncherScreen();
    }

    @Override
    public void breakBlock(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (tile instanceof ILauncherComponent) {
            ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
        }
        super.breakBlock(world, pos, state);
    }
}
