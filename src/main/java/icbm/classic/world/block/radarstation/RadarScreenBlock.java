package icbm.classic.world.block.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.prefab.tile.IcbmBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2018.
 */
public class RadarScreenBlock extends IcbmBlock {
    public static final PropertyBool REDSTONE_PROPERTY = PropertyBool.create("redstone");
    public static final PropertyRadarState RADAR_STATE = new PropertyRadarState();

    public RadarScreenBlock(BlockBehaviour.Properties properties) {
        super(properties, "radar_screen");
        this.dropInventory = true;
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (tile instanceof TileRadarStation) {
            return state.withProperty(RADAR_STATE, ((TileRadarStation) tile).getRadarState());
        }
        return state;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION_PROP, REDSTONE_PROPERTY, RADAR_STATE);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockAccess world, BlockPos pos, @Nullable Direction side) {
        final BlockEntity blockEntityEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof TileRadarStation) {
            return ((TileRadarStation) tileEntity).isOutputRedstone();
        }
        return false;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return state.getValue(REDSTONE_PROPERTY);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        return getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockAccess blockAccess, BlockPos pos, Direction side) {
        BlockEntity blockEntity = blockAccess.getBlockEntity(pos);
        if (tile instanceof TileRadarStation) {
            return ((TileRadarStation) tile).getStrongRedstonePower(side);
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(Level level, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide()) {
            //if (WrenchUtility.isUsableWrench(player, player.getHeldItem(hand), pos.getX(), pos.getY(), pos.getZ()))
            if (player.getHeldItem(hand).getItem() == Items.REDSTONE) //TODO move to UI
            {
                final BlockEntity blockEntity = world.getBlockEntity(pos);
                if (tile instanceof TileRadarStation) {
                    ((TileRadarStation) tile).setOutputRedstone(!((TileRadarStation) tile).isOutputRedstone());
                    player.sendMessage(new TextComponentTranslation(((TileRadarStation) tile).isOutputRedstone() ? "message.radar.redstone.on" : "message.radar.redstone.off"));
                } else {
                    player.sendMessage(new TextComponentString("\u00a7cUnexpected error: Couldn't access radar station tile"));
                }
            } else {
                player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createNewBlockEntity(Level levelIn, int meta) {
        return new TileRadarStation();
    }
}
