package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/23/2018.
 */
public class BlockEmpTower extends BlockContainer
{
    public static final PropertyTowerStates TOWER_MODELS = new PropertyTowerStates();
    public static IBlockState COIL;

    public BlockEmpTower()
    {
        super(Material.IRON);
        blockHardness = 10f;
        blockResistance = 10f;
        setRegistryName(ICBMConstants.DOMAIN, "emptower");
        setUnlocalizedName(ICBMConstants.PREFIX + "emptower");
        setCreativeTab(ICBMClassic.CREATIVE_TAB);

        COIL = getDefaultState().withProperty(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.COIL);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TOWER_MODELS) {};
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            playerIn.openGui(ICBMClassic.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos)
                && worldIn.getBlockState(pos.up()).getBlock().isReplaceable(worldIn, pos.up());
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
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return false;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos)
    {
        return 0; //TODO output charge amount
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
    {
        return BlockRenderLayer.TRANSLUCENT == layer || BlockRenderLayer.SOLID == layer;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEMPTower();
    }
}
