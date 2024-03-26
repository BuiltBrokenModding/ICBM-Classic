package icbm.classic.world.block.launcher.connector;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.world.IcbmBlockEntityTypes;
import icbm.classic.world.block.launcher.frame.LaunchFrameBlockEntity;
import icbm.classic.world.block.launcher.network.ILauncherComponent;
import icbm.classic.world.block.launcher.network.LauncherNetwork;
import net.minecraft.block.properties.PropertyBool;
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
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.energy.CapabilityEnergy;
import net.neoforged.items.CapabilityItemHandler;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class LaunchConnectorBlock extends AbstractChestBlock<LaunchConnectorBlockEntity> {
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");
    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");

    public LaunchConnectorBlock(BlockBehaviour.Properties properties) {
        super(properties, IcbmBlockEntityTypes.LAUNCH_CONNECTOR::get);
        blockHardness = 10f;
        blockResistance = 10f;
        setRegistryName(IcbmConstants.MOD_ID, "launcher_connector");
        setUnlocalizedName(IcbmConstants.PREFIX + "launcher_connector");
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        final boolean upConnection = isConnection(worldIn, pos, Direction.UP);
        final boolean downConnection = isConnection(worldIn, pos, Direction.DOWN);
        final boolean northConnection = isConnection(worldIn, pos, Direction.NORTH);
        final boolean eastConnection = isConnection(worldIn, pos, Direction.EAST);
        final boolean southConnection = isConnection(worldIn, pos, Direction.SOUTH);
        final boolean westConnection = isConnection(worldIn, pos, Direction.WEST);

        return state
            .withProperty(UP, upConnection)
            .withProperty(DOWN, downConnection)
            .withProperty(NORTH, northConnection)
            .withProperty(EAST, eastConnection)
            .withProperty(SOUTH, southConnection)
            .withProperty(WEST, westConnection);
    }

    private boolean isConnection(IBlockAccess worldIn, BlockPos selfPos, Direction side) {
        final BlockPos pos = selfPos.offset(side);
        final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (tile != null) {
            return tile.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite())
                || tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side.getOpposite());
        }
        return false;
    }


    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
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
        return new BlockStateContainer(this, UP, DOWN, NORTH, EAST, WEST, SOUTH);
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
        return new LaunchConnectorBlockEntity();
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
