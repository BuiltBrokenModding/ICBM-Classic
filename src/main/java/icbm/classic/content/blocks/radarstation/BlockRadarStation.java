package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.content.blocks.launcher.frame.EnumFrameState;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2018.
 */
public class BlockRadarStation extends BlockICBM
{
    public static final PropertyBool REDSTONE_PROPERTY = PropertyBool.create("redstone");
    public static final PropertyRadarState RADAR_STATE = new PropertyRadarState();

    public BlockRadarStation()
    {
        super("radarStation"); //TODO rename to "radar_screen"
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
    {
        final TileEntity tile = worldIn.getTileEntity(pos);
        if(tile instanceof TileRadarStation) {
            return state.withProperty(RADAR_STATE, ((TileRadarStation) tile).getRadarState());
        }
        return state;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ROTATION_PROP, REDSTONE_PROPERTY, RADAR_STATE);
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        final TileEntity tileEntity = world.getTileEntity(pos);
        if(tileEntity instanceof TileRadarStation) {
            return ((TileRadarStation) tileEntity).enableRedstoneOutput;
        }
        return false;
    }

    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return state.getValue(REDSTONE_PROPERTY);
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return getStrongPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        TileEntity tile = blockAccess.getTileEntity(pos);
        if (tile instanceof TileRadarStation)
        {
            return ((TileRadarStation) tile).getStrongRedstonePower(side);
        }
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!world.isRemote)
        {
            //if (WrenchUtility.isUsableWrench(player, player.getHeldItem(hand), pos.getX(), pos.getY(), pos.getZ()))
            if (player.getHeldItem(hand).getItem() == Items.REDSTONE)
            {
                final TileEntity tile = world.getTileEntity(pos);
                if (tile instanceof TileRadarStation)
                {
                    ((TileRadarStation) tile).enableRedstoneOutput = !((TileRadarStation) tile).enableRedstoneOutput;
                    player.sendMessage(new TextComponentTranslation(((TileRadarStation) tile).enableRedstoneOutput ? "message.radar.redstone.on" : "message.radar.redstone.off"));
                }
                else
                {
                    player.sendMessage(new TextComponentString("\u00a7cUnexpected error: Couldn't access radar station tile"));
                }
            }
            else
            {
                player.openGui(ICBMClassic.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileRadarStation();
    }
}
