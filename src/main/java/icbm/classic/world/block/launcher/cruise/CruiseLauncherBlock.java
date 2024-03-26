package icbm.classic.world.block.launcher.cruise;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.capability.gps.GPSDataHelpers;
import icbm.classic.prefab.tile.IcbmBlock;
import icbm.classic.world.block.launcher.network.ILauncherComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/15/2018.
 */
public class CruiseLauncherBlock extends IcbmBlock {
    public CruiseLauncherBlock(BlockBehaviour.Properties properties) {
        super(properties, "cruise_launcher");
        this.blockHardness = 10f;
        this.blockResistance = 10f;
        this.dropInventory = true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockAccess world, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canPlaceBlockOnSide(Level levelIn, BlockPos pos, Direction side) {
        return super.canPlaceBlockOnSide(worldIn, pos, side);
    }

    @Override
    public boolean canPlaceBlockAt(Level levelIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos);
    }

    @Nullable
    @Override
    public BlockEntity createNewBlockEntity(Level levelIn, int meta) {
        return new TileCruiseLauncher();
    }

    @Override
    public boolean onBlockActivated(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide()) {
            final BlockEntity blockEntityEntity = world.getBlockEntity(pos);
            if (tileEntity instanceof TileCruiseLauncher) {
                final TileCruiseLauncher launcher = (TileCruiseLauncher) tileEntity;
                final ItemStack stack = player.getHeldItem(hand);
                final IGPSData gpsData = ICBMClassicHelpers.getGPSData(stack);
                if (!GPSDataHelpers.handlePlayerInteraction(gpsData, player, launcher::setTarget)) {
                    player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
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
