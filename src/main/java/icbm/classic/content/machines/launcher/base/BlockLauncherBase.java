package icbm.classic.content.machines.launcher.base;

import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/8/2018.
 */
public class BlockLauncherBase extends BlockICBM
{
    public BlockLauncherBase()
    {
        super("launcherBase", Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION_PROP, EnumFacing.NORTH).withProperty(TIER_PROP, EnumTier.ONE));
        this.blockHardness = 10f;
        this.blockResistance = 10f;
        //this.isOpaque = false;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        items.add(new ItemStack(this, 1, EnumTier.ONE.ordinal()));
        items.add(new ItemStack(this, 1, EnumTier.TWO.ordinal()));
        items.add(new ItemStack(this, 1, EnumTier.THREE.ordinal()));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileLauncherBase)
        {
            ((TileLauncherBase)tile).setTier(EnumTier.get(stack.getItemDamage()));
        }
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        return side == EnumFacing.UP && this.canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos)
                && worldIn.getBlockState(pos.up()).getBlock().isReplaceable(worldIn, pos.up())
                && worldIn.getBlockState(pos.up(2)).getBlock().isReplaceable(worldIn, pos.up(2));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ROTATION_PROP, TIER_PROP);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand);
        ItemStack stack = placer.getHeldItem(hand);

        //Set tier
        state = state.withProperty(TIER_PROP, EnumTier.get(stack.getItemDamage()));

        return state;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(TIER_PROP).ordinal();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileLauncherBase();
    }
}
