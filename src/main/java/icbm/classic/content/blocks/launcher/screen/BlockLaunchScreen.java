package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.api.data.IWorldPosition;
import icbm.classic.api.items.IWorldPosItem;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.items.ItemLaserDetonator;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.items.ItemRemoteDetonator;
import icbm.classic.prefab.tile.BlockICBM;
import icbm.classic.api.EnumTier;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2018.
 */
public class BlockLaunchScreen extends BlockICBM
{
    //values by trial and error
    private static final float px = 1.0F / 16.0F; //one pixel
    private static final AxisAlignedBB THREE_NORTH   = new AxisAlignedBB(3 * px,     0, 2 * px,     13 * px,     10.5F * px, 12 * px);
    private static final AxisAlignedBB THREE_SOUTH   = new AxisAlignedBB(13 * px,    0, 14 * px,    3 * px,      10.5F * px, 4 * px);
    private static final AxisAlignedBB THREE_EAST    = new AxisAlignedBB(14 * px,    0, 13 * px,    4 * px,      10.5F * px, 3 * px);
    private static final AxisAlignedBB THREE_WEST    = new AxisAlignedBB(2 * px,     0, 3 * px,     12 * px,     10.5F * px, 13 * px);

    public BlockLaunchScreen()
    {
        super("launcherscreen");
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        if(state.getBlock() instanceof BlockLaunchScreen) //sometimes things happen that make this necessary
        {
            switch(state.getValue(ROTATION_PROP))
            {
                case NORTH: return THREE_NORTH;
                case SOUTH: return THREE_SOUTH;
                case EAST: return THREE_EAST;
                case WEST: return THREE_WEST;
                default: return super.getBoundingBox(state, source, pos);
            }
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos);
            if(tileEntity instanceof TileLauncherScreen)
            {
                TileLauncherScreen screen = (TileLauncherScreen) tileEntity;

                ItemStack stack = player.getHeldItem(hand);
                if (stack.getItem() == Items.REDSTONE)
                {
                    if (!screen.launch()) //canLaunch is called in launch and launch returns false if cannot launch
                    {
                        player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.failedToFire")));

                        String translation = LanguageUtility.getLocal("chat.launcher.status");
                        translation = translation.replace("%s", screen.getStatus()); //TODO remove status being a separate translation
                        player.sendMessage(new TextComponentString(translation));
                    }
                }
                else if (stack.getItem() instanceof ItemRemoteDetonator)
                {
                    ((ItemRemoteDetonator) stack.getItem()).setBroadCastHz(stack, screen.getFrequency());
                    player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolFrequencySet").replace("%s", "" + screen.getFrequency())));
                }
                else if (stack.getItem() instanceof ItemLaserDetonator)
                {
                    ((ItemLaserDetonator) stack.getItem()).setBroadCastHz(stack, screen.getFrequency());
                    player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.toolFrequencySet").replace("%s", "" + screen.getFrequency())));
                }
                else if (stack.getItem() instanceof IWorldPosItem)
                {
                    IWorldPosition location = ((IWorldPosItem) stack.getItem()).getLocation(stack);
                    if (location != null)
                    {
                        if (location.world() == world)
                        {
                            screen.setTarget(new Pos(location.x(), location.y(), location.z()));
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
                else if(screen.launcherBase == null || !screen.launcherBase.tryInsertMissile(player, hand, player.getHeldItem(hand)))
                {
                    player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
        }
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
    {
        if(!world.isRemote)
        {
            TileEntity tileEntity = world.getTileEntity(pos);

            if(tileEntity instanceof TileLauncherScreen && world.isBlockPowered(pos))
            {
                TileLauncherScreen screen = (TileLauncherScreen)tileEntity;
                screen.launch(); //canLaunch gets called by launch
            }
        }
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileLauncherScreen();
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
}
