package icbm.classic.content.blocks.launcher.base;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNetwork;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        this.blockHardness = 10f;
        this.blockResistance = 10f;
        this.fullBlock = true;
        setRegistryName(ICBMConstants.DOMAIN, "launcherbase");
        setUnlocalizedName(ICBMConstants.PREFIX + "launcherbase");
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    @Deprecated
    public boolean isOpaqueCube(IBlockState state)
    {
        // Needed to prevent render lighting issues for missiles
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileLauncherBase)
        {
            if(playerIn.getHeldItem(hand).getItem() == Items.STONE_AXE) {
                if(!worldIn.isRemote) {
                    final LauncherNetwork network = ((TileLauncherBase) tile).getNetworkNode().getNetwork();
                    playerIn.sendMessage(new TextComponentString("Network: " + network));
                    playerIn.sendMessage(new TextComponentString("L: " + network.getLaunchers().size()));
                }
                return true;
            }
            return ((TileLauncherBase) tile).tryInsertMissile(playerIn, hand, playerIn.getHeldItem(hand));
        }
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ILauncherComponent)
        {
            ((ILauncherComponent) tile).getNetworkNode().onTileRemoved();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.SOLID;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileLauncherBase();
    }
}
