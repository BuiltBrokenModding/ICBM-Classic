package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.content.blocks.multiblock.MultiBlockHelper;
import icbm.classic.prefab.tile.BlockICBM;
import icbm.classic.api.EnumTier;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/8/2018.
 */
public class BlockLauncherBase extends BlockICBM
{
    public BlockLauncherBase()
    {
        super("launcherbase", Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION_PROP, EnumFacing.NORTH).withProperty(TIER_PROP, EnumTier.ONE));
        this.blockHardness = 10f;
        this.blockResistance = 10f;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileLauncherBase)
        {
            return ((TileLauncherBase) tile).onPlayerRightClick(playerIn, hand, playerIn.getHeldItem(hand));
        }
        return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        items.add(new ItemStack(this, 1, EnumTier.ONE.ordinal()));
        items.add(new ItemStack(this, 1, EnumTier.TWO.ordinal()));
        items.add(new ItemStack(this, 1, EnumTier.THREE.ordinal()));
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side)
    {
        return super.canPlaceBlockOnSide(worldIn, pos, side);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return super.canPlaceBlockAt(worldIn, pos);
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
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        EnumTier tier = EnumTier.ONE;
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileLauncherBase)
        {
            tier = ((TileLauncherBase) tile)._tier;
        }
        return state.withProperty(TIER_PROP, tier);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack stack)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileLauncherBase)
        {
            //Set tier data
            ((TileLauncherBase) tile)._tier = EnumTier.get(stack.getItemDamage());

            //Build multiblock
            MultiBlockHelper.buildMultiBlock(world, (IMultiTileHost) tile, true, true);
            //TODO if can't place, break and drop item
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity tileEntity = world.getTileEntity(pos);

        if(tileEntity instanceof TileLauncherBase)
        {
            switch(((TileLauncherBase)tileEntity)._tier)
            {
                case TWO: return new ItemStack(this, 1, EnumTier.TWO.ordinal());
                case THREE: return new ItemStack(this, 1, EnumTier.THREE.ordinal());
                default: return new ItemStack(this, 1, EnumTier.ONE.ordinal());
            }
        }

        return new ItemStack(this, 1, EnumTier.ONE.ordinal());
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {

    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(TIER_PROP).ordinal();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileLauncherBase();
    }
}
