package icbm.classic.world.block.launcher.frame;

import icbm.classic.prefab.tile.IcbmBlock;
import icbm.classic.world.block.launcher.network.ILauncherComponent;
import icbm.classic.world.block.launcher.network.LauncherNetwork;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.init.Items;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class LaunchFrameBlock extends IcbmBlock {
    public static final PropertyFrameState FRAME_STATE = new PropertyFrameState();

    public LaunchFrameBlock(BlockBehaviour.Properties properties) {
        super(properties, "launcherframe");
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        final boolean frameAbove = isConnection(worldIn, pos.offset(Direction.UP));
        final boolean frameUnder = isConnection(worldIn, pos.offset(Direction.DOWN));
        if (frameAbove && frameUnder) {
            return state.withProperty(FRAME_STATE, EnumFrameState.MIDDLE);
        } else if (frameUnder) {
            return state.withProperty(FRAME_STATE, EnumFrameState.TOP);
        } else if (frameAbove) {
            return state.withProperty(FRAME_STATE, EnumFrameState.BOTTOM);
        }
        return state.withProperty(FRAME_STATE, EnumFrameState.MIDDLE);
    }

    private boolean isConnection(IBlockAccess worldIn, BlockPos pos) {
        final BlockState state = worldIn.getBlockState(pos);
        return state.getBlock() == this || state.getBlock() == BlockReg.blockLaunchScreen;
    }

    @Override
    public boolean onBlockActivated(Level levelIn, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (tile instanceof LaunchFrameBlockEntity) {
            if (playerIn.getHeldItem(hand).getItem() == Items.STONE_AXE) {
                if (!worldIn.isClientSide()) {
                    final LauncherNetwork network = ((LaunchFrameBlockEntity) tile).getNetworkNode().getNetwork();
                    playerIn.sendMessage(new TextComponentString("Network: " + network));
                    playerIn.sendMessage(new TextComponentString("L: " + network.getLaunchers().size()));
                }
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION_PROP, FRAME_STATE);
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public BlockEntity createNewBlockEntity(Level levelIn, int meta) {
        return new LaunchFrameBlockEntity();
    }

    @Override
    public void breakBlock(Level level, BlockPos pos, BlockState state) {
        final BlockEntity blockEntity = world.getBlockEntity(pos);
        if (tile instanceof ILauncherComponent) {
            ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
        }
        super.breakBlock(world, pos, state);
    }
}
