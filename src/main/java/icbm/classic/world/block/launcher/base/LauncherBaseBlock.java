package icbm.classic.world.block.launcher.base;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.lib.InventoryUtility;
import icbm.classic.world.IcbmBlockEntityTypes;
import icbm.classic.world.block.launcher.network.ILauncherComponent;
import icbm.classic.world.block.launcher.network.LauncherNetwork;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.init.Items;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/8/2018.
 */
public class LauncherBaseBlock extends AbstractChestBlock<LauncherBaseBlockEntity> {
    public static final PropertyDirection ROTATION_PROP = PropertyDirection.create("facing");

    public LauncherBaseBlock(BlockBehaviour.Properties properties) {
        super(properties, IcbmBlockEntityTypes.LAUNCHER_BASE::get);
        this.blockHardness = 10f;
        this.blockResistance = 10f;
        this.fullBlock = true;
        setRegistryName(IcbmConstants.MOD_ID, "launcherbase");
        setUnlocalizedName(IcbmConstants.PREFIX + "launcherbase");
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    @Deprecated
    public boolean isOpaqueCube(BlockState state) {
        // Needed to prevent render lighting issues for missiles
        return false;
    }

    @Override
    public boolean onBlockActivated(Level levelIn, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if (tile instanceof LauncherBaseBlockEntity && !worldIn.isClientSide()) {
            if (playerIn.getHeldItem(hand).getItem() == Items.STONE_AXE) {
                final LauncherNetwork network = ((LauncherBaseBlockEntity) tile).getNetworkNode().getNetwork();
                playerIn.sendMessage(new TextComponentString("Network: " + network));
                playerIn.sendMessage(new TextComponentString("L: " + network.getLaunchers().size()));
                return true;
            }
            if (!((LauncherBaseBlockEntity) tile).tryInsertMissile(playerIn, hand, playerIn.getHeldItem(hand))) {
                playerIn.openGui(ICBMClassic.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return true;
    }

    @Override
    public void breakBlock(Level level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (tile instanceof ILauncherComponent) {
            ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
        }
        InventoryUtility.dropInventory(world, pos);
        super.breakBlock(world, pos, state);
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.SOLID;
    }

    @Nullable
    @Override
    public BlockEntity createNewBlockEntity(Level levelIn, int meta) {
        return new LauncherBaseBlockEntity();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION_PROP);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        if (meta == 0) {
            return getDefaultState().withProperty(ROTATION_PROP, Direction.UP);
        }
        return getDefaultState().withProperty(ROTATION_PROP, Direction.getFront(meta - 1));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        // Shifting by one due to older tiles not having rotation, default should be UP
        return state.getValue(ROTATION_PROP).ordinal() + 1;
    }

    @Override
    public BlockState getStateForPlacement(Level level, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, InteractionHand hand) {
        return getDefaultState().withProperty(ROTATION_PROP, facing);
    }
}
