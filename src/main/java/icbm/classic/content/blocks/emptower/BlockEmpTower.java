package icbm.classic.content.blocks.emptower;

import com.mojang.serialization.MapCodec;
import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/23/2018.
 * <p>
 * PORT: 1.12 meta 1 (extender) is the SPIN state, placed by the emptower_extender item. COIL and ELECTRIC are
 * render only states used by {@link TESREmpTower}; the ELECTRIC model should declare "render_type": "translucent".
 */
public class BlockEmpTower extends BaseEntityBlock
{
    public static final MapCodec<BlockEmpTower> CODEC = simpleCodec(p -> new BlockEmpTower());

    public static final PropertyTowerStates TOWER_MODELS = new PropertyTowerStates();
    public static BlockState COIL;
    public static BlockState ELECTRIC;

    public BlockEmpTower()
    {
        // 1.12 Material.IRON, hardness 10, resistance field 10 (= 2), IRON needed a pickaxe to drop
        super(Properties.of().mapColor(MapColor.METAL).strength(10f, 2f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.BASE));

        COIL = defaultBlockState().setValue(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.COIL);
        ELECTRIC = defaultBlockState().setValue(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.ELECTRIC);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "emptower.0";
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(TOWER_MODELS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        // Extender state is applied by ItemBlockEmpTower
        return defaultBlockState();
    }

    /** True if the state is the extender (1.12 meta 1) */
    public static boolean isExtender(BlockState state)
    {
        return state.getValue(TOWER_MODELS) == PropertyTowerStates.EnumTowerTypes.SPIN;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state)
    {
        return new ItemStack(isExtender(state) ? ItemReg.itemEmpTowerExtender.get() : ItemReg.itemEmpTower.get());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit)
    {
        if (!worldIn.isClientSide())
        {
            // PORT: 1.12 opened GUI id 0 on whichever tile was at the position, only the main tower has a GUI
            final BlockEntity tile = worldIn.getBlockEntity(pos);
            if (tile instanceof TileEMPTower)
            {
                ((TileEMPTower) tile).openGui(playerIn);
            }
            else if (tile instanceof TileEmpTowerFake && ((TileEmpTowerFake) tile).getHost() != null)
            {
                ((TileEmpTowerFake) tile).getHost().openGui(playerIn);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state)
    {
        return false;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos)
    {
        return 0; //TODO output charge amount
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        // 1.12 isOpaqueCube false
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        if (isExtender(state))
        {
            return new TileEmpTowerFake(pos, state);
        }
        return new TileEMPTower(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return createTickerHelper(type, BlockReg.TILE_EMP_TOWER.get(), (l, p, s, tile) -> tile.update());
    }
}
// PORTED
