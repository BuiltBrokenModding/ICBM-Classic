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
    private static final AxisAlignedBB ONE_TWO_NORTH = new AxisAlignedBB(3 * px,     0, 6 * px,     13 * px,     9.5F * px,  10.75F * px);
    private static final AxisAlignedBB ONE_TWO_SOUTH = new AxisAlignedBB(13 * px,    0, 5.25F * px, 3 * px,      9.5F * px,  10 * px);
    private static final AxisAlignedBB ONE_TWO_EAST  = new AxisAlignedBB(5.25F * px, 0, 13 * px,    10 * px,     9.5F * px,  3 * px);
    private static final AxisAlignedBB ONE_TWO_WEST  = new AxisAlignedBB(6 * px,     0, 3 * px,     10.75F * px, 9.5F * px,  13 * px);
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
            switch(state.getValue(TIER_PROP))
            {
                case ONE: case TWO:
                {
                    switch(state.getValue(ROTATION_PROP))
                    {
                        case NORTH: return ONE_TWO_NORTH;
                        case SOUTH: return ONE_TWO_SOUTH;
                        case EAST: return ONE_TWO_EAST;
                        case WEST: return ONE_TWO_WEST;
                        default: return super.getBoundingBox(state, source, pos);
                    }
                }
                case THREE:
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
                    if((screen._tier == EnumTier.ONE && !ConfigLauncher.LAUNCHER_REDSTONE_TIER1)
                            || (screen._tier == EnumTier.TWO && !ConfigLauncher.LAUNCHER_REDSTONE_TIER2)
                            || (screen._tier == EnumTier.THREE && !ConfigLauncher.LAUNCHER_REDSTONE_TIER3))
                    {
                        return false;
                    }

                    if (!screen.launch()) //canLaunch is called in launch and launch returns false if cannot launch
                    {
                        player.sendMessage(new TextComponentString(LanguageUtility.getLocal("chat.launcher.failedToFire")));

                        String translation = LanguageUtility.getLocal("chat.launcher.status");
                        translation = translation.replace("%s", screen.getStatus());
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
                if((screen._tier == EnumTier.ONE && !ConfigLauncher.LAUNCHER_REDSTONE_TIER1)
                        || (screen._tier == EnumTier.TWO && !ConfigLauncher.LAUNCHER_REDSTONE_TIER2)
                        || (screen._tier == EnumTier.THREE && !ConfigLauncher.LAUNCHER_REDSTONE_TIER3))
                {
                    return;
                }
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

    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(TIER_PROP).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ROTATION_PROP, TIER_PROP);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        EnumTier tier = EnumTier.ONE;
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileLauncherScreen)
        {
            tier = ((TileLauncherScreen) tile)._tier;
        }
        return state.withProperty(TIER_PROP, tier);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        ItemStack stack = placer.getHeldItem(hand);

        //set tier and horizontal facing. latter seems to be the other way around as for other BlockICBMs, so super is not called and the rotation is set here instead
        return getDefaultState().withProperty(TIER_PROP, EnumTier.get(stack.getItemDamage())).withProperty(ROTATION_PROP, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack stack)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileLauncherScreen)
        {
            ((TileLauncherScreen) tile)._tier = EnumTier.get(stack.getItemDamage());
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
        items.add(new ItemStack(this, 1, 2));
    }
}
