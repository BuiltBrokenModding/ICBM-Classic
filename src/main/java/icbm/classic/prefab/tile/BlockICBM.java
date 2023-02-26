package icbm.classic.prefab.tile;

import icbm.classic.ICBMConstants;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.ICBMClassic;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockICBM extends BlockContainer
{
    public static final PropertyDirection ROTATION_PROP = PropertyDirection.create("rotation");

    public BlockICBM(String name, Material mat)
    {
        super(mat);
        blockHardness = 10f;
        blockResistance = 10f;
        setRegistryName(ICBMConstants.DOMAIN, name.toLowerCase());
        setUnlocalizedName(ICBMConstants.PREFIX + name.toLowerCase());
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    public BlockICBM(String name)
    {
        this(name, Material.IRON);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ROTATION_PROP);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(ROTATION_PROP, EnumFacing.getFront(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(ROTATION_PROP).ordinal();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return getDefaultState().withProperty(ROTATION_PROP, placer.getHorizontalFacing());
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof IInventoryProvider && ((IInventoryProvider) tile).getInventory() != null)
        {
            InventoryHelper.dropInventoryItems(world, pos, ((IInventoryProvider) tile).getInventory());
        }
        super.breakBlock(world, pos, state);
    }
}
