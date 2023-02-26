package icbm.classic.content.blocks.launcher.base;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/8/2018.
 */
public class BlockLauncherBase extends BlockContainer
{
    public BlockLauncherBase()
    {
        super(Material.IRON);
        blockHardness = 10f;
        blockResistance = 10f;
        setRegistryName(ICBMConstants.DOMAIN, "launcherbase");
        setUnlocalizedName(ICBMConstants.PREFIX + "launcherbase");
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileLauncherBase)
        {
            return ((TileLauncherBase) tile).onPlayerRightClick(playerIn, hand, playerIn.getHeldItem(hand));
        }
        return false;
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
