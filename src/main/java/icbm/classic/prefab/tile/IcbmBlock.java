package icbm.classic.prefab.tile;

import icbm.classic.ICBMClassic;
import icbm.classic.IcbmConstants;
import icbm.classic.lib.InventoryUtility;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public abstract class IcbmBlock extends Block {

    public static final PropertyDirection ROTATION_PROP = PropertyDirection.create("rotation");

    protected boolean dropInventory = false;

    public IcbmBlock(BlockBehaviour.Properties properties, String name) {
        super(properties);
        blockHardness = 10f;
        blockResistance = 10f;
        setRegistryName(IcbmConstants.MOD_ID, name.toLowerCase());
        setUnlocalizedName(IcbmConstants.PREFIX + name.toLowerCase());
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION_PROP);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ROTATION_PROP, Direction.getFront(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(ROTATION_PROP).ordinal();
    }

    @Override
    public BlockState getStateForPlacement(Level level, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, InteractionHand hand) {
        return getDefaultState().withProperty(ROTATION_PROP, placer.getHorizontalFacing());
    }

    @Override
    public void breakBlock(Level level, BlockPos pos, BlockState state) {
        if (dropInventory) {
            InventoryUtility.dropInventory(world, pos);
        }
        super.breakBlock(world, pos, state);
    }
}
