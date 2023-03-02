package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.api.data.IWorldPosition;
import icbm.classic.api.items.IWorldPosItem;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.items.ItemLaserDetonator;
import icbm.classic.content.items.ItemRemoteDetonator;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/15/2018.
 */
public class BlockCruiseLauncher extends BlockICBM
{
    public BlockCruiseLauncher()
    {
        super("cruiseLauncher");
        this.blockHardness = 10f;
        this.blockResistance = 10f;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        return true;
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
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileCruiseLauncher();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof TileCruiseLauncher)
            {
                TileCruiseLauncher launcher = (TileCruiseLauncher) tileEntity;
                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() instanceof ItemRemoteDetonator)
                {
                    ((ItemRemoteDetonator) stack.getItem()).setBroadCastHz(stack, launcher.getFrequency());
                    player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolFrequencySet").replace("%s", "" + launcher.getFrequency())));
                }
                else if (stack.getItem() instanceof ItemLaserDetonator)
                {
                    ((ItemLaserDetonator) stack.getItem()).setBroadCastHz(stack, launcher.getFrequency());
                    player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolFrequencySet").replace("%s", "" + launcher.getFrequency())));
                }
                else if (stack.getItem() instanceof IWorldPosItem)
                {
                    IWorldPosition location = ((IWorldPosItem) stack.getItem()).getLocation(stack);
                    if (location != null)
                    {
                        if (location.world() == world)
                        {
                            launcher.setTarget(new Pos(location.x(), location.y(), location.z()));
                            player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolTargetSet")));
                        }
                        else
                        {
                            player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolWorldNotMatch")));
                        }
                    }
                    else
                    {
                        player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.noTargetInTool")));
                    }
                }
                else
                {
                    player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
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
}
