package icbm.classic.world.block.emptower;

import com.mojang.serialization.MapCodec;
import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.world.IcbmBlockEntityTypes;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/23/2018.
 */
public class EmpTowerBlock extends AbstractChestBlock<EmpTowerBlockEntity> {
    public static final PropertyTowerStates TOWER_MODELS = new PropertyTowerStates();
    public static BlockState COIL;
    public static BlockState ELECTRIC;

    public EmpTowerBlock(BlockBehaviour.Properties properties) {
        super(properties, IcbmBlockEntityTypes.EMP_TOWER::get);
        blockHardness = 10f;
        blockResistance = 10f;
        setRegistryName(IcbmConstants.MOD_ID, "emptower");
        setUnlocalizedName(IcbmConstants.PREFIX + "emptower");
        setCreativeTab(ICBMClassic.CREATIVE_TAB);

        COIL = getDefaultState().withProperty(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.COIL);
        ELECTRIC = getDefaultState().withProperty(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.ELECTRIC);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TOWER_MODELS) {
        };
    }

    @Override
    public int damageDropped(BlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public BlockState getStateForPlacement(Level level, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, InteractionHand hand) {
        return getStateFromMeta(meta);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        if (meta == 1) {
            return getDefaultState().withProperty(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.SPIN);
        }
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(BlockState state) {
        if (state.getValue(TOWER_MODELS) == PropertyTowerStates.EnumTowerTypes.SPIN) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(Level levelIn, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isClientSide()) {
            playerIn.openGui(ICBMClassic.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
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
    public boolean hasComparatorInputOverride(BlockState state) {
        return false;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, Level levelIn, BlockPos pos) {
        return 0; //TODO output charge amount
    }

    @Override
    public EnumBlockRenderType getRenderType(BlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        if (state == ELECTRIC) {
            return BlockRenderLayer.TRANSLUCENT == layer;
        }
        return BlockRenderLayer.SOLID == layer;
    }

    @Nullable
    @Override
    public BlockEntity createNewBlockEntity(Level levelIn, int meta) {
        if (meta == 1) {
            return new TileEmpTowerFake();
        }
        return new EmpTowerBlockEntity();
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
    }

    @Override
    protected MapCodec<? extends AbstractChestBlock<EmpTowerBlockEntity>> codec() {
        return null;
    }

    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState pState, Level pLevel, BlockPos pPos, boolean pOverride) {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return null;
    }
}
