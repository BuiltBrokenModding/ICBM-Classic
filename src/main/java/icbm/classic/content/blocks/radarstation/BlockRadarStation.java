package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/16/2018.
 */
public class BlockRadarStation extends Block {
    public static final BooleanProperty REDSTONE_PROPERTY = BooleanProperty.create("redstone");
    public static final EnumProperty<EnumRadarState> RADAR_STATE = EnumProperty.create("type", EnumRadarState.class);
    public static final EnumProperty<Direction> ROTATION_PROP = BlockStateProperties.FACING;

    public BlockRadarStation() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(10, 10));
    }

    @Override
    public BlockState getExtendedState(BlockState state, IBlockReader world, BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileRadarStation) {
            return state.with(RADAR_STATE, ((TileRadarStation) tile).getRadarState());
        }
        return state;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ROTATION_PROP, REDSTONE_PROPERTY, RADAR_STATE);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileRadarStation) {
            return ((TileRadarStation) tileEntity).isOutputRedstone();
        }
        return false;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return state.get(REDSTONE_PROPERTY);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        final TileEntity tile = blockAccess.getTileEntity(pos);
        if (tile instanceof TileRadarStation) {
            return ((TileRadarStation) tile).getStrongRedstonePower(side);
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            final TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileRadarStation) {
                //if (WrenchUtility.isUsableWrench(player, player.getHeldItem(hand), pos.getX(), pos.getY(), pos.getZ()))
                if (player.getHeldItem(hand).getItem() == Items.REDSTONE) //TODO move to UI
                {
                    ((TileRadarStation) tile).setOutputRedstone(!((TileRadarStation) tile).isOutputRedstone());
                    player.sendMessage(new TranslationTextComponent(((TileRadarStation) tile).isOutputRedstone() ? "message.radar.redstone.on" : "message.radar.redstone.off"));
                } else if (player instanceof ServerPlayerEntity) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, (TileRadarStation)tile, pos);
                    //https://github.com/Up-Mods/Cammies-Wearable-Backpacks/blob/1.20.1/NeoForge/src/main/java/dev/cammiescorner/camsbackpacks/neoforge/services/NFMenuHelper.java
                    //player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
                }
            } else {
                player.sendMessage(new StringTextComponent("\u00a7cUnexpected error: Couldn't access radar station tile"));
            }
        }
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileRadarStation();
    }
}
