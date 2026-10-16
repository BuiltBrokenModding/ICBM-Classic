package icbm.classic.prefab.tile;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.InventoryUtility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Prefab for ICBM machine blocks with a block entity and a 6 direction rotation.
 * <p>
 * PORT: name is used for the 1.12 translation key only ({@code tile.icbmclassic:<name lower case>}), registry
 * names live in BlockReg. 1.12 hardness 10 / resistance field 10 maps to explosion resistance 2 (1.12 divided by 5).
 */
public abstract class BlockICBM extends BaseEntityBlock
{
    public static final DirectionProperty ROTATION_PROP = DirectionProperty.create("rotation");

    protected boolean dropInventory = false;

    /** 1.12 unlocalized name, returned as description id */
    private final String descriptionId;

    public BlockICBM(String name, Properties properties)
    {
        super(properties);
        this.descriptionId = "tile." + ICBMConstants.PREFIX + name.toLowerCase();
        this.registerDefaultState(this.stateDefinition.any().setValue(ROTATION_PROP, Direction.NORTH));
    }

    public BlockICBM(String name)
    {
        this(name, machineProperties());
    }

    /** Default properties, 1.12 Material.IRON with hardness 10 */
    public static Properties machineProperties()
    {
        // PORT: 1.12 Material.IRON needed the right tool to drop (and was slow by hand) -> requiresCorrectToolForDrops,
        // the block must also be in the mineable/pickaxe tag so a pickaxe counts as the correct tool
        return Properties.of().mapColor(MapColor.METAL).strength(10f, 2f).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }

    @Override
    public String getDescriptionId()
    {
        return descriptionId;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(ROTATION_PROP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return defaultBlockState().setValue(ROTATION_PROP, context.getHorizontalDirection());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (dropInventory && !state.is(newState.getBlock()))
        {
            InventoryUtility.dropInventory(world, pos);
        }
        super.onRemove(state, world, pos, newState, movedByPiston);
    }
}
// PORTED
